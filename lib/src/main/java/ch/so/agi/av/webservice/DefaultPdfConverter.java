package ch.so.agi.av.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.s9api.Processor;

/**
 * Standardimplementierung fuer die Konvertierung eines AV-Extrakt-XML nach PDF oder XSL-FO.
 * Die Klasse verwaltet zur Laufzeit benoetigte Ressourcen (XSLT, Fonts, FOP-Konfiguration),
 * setzt den Locale-Parameter und kuemmert sich um Caching expliziter XSLT-Dateien.
 */
public class DefaultPdfConverter implements PdfConverter {
    private static final Logger log = LoggerFactory.getLogger(DefaultPdfConverter.class);

    private static final String DEFAULT_XSLT_RESOURCE = "xslt/xml2pdf_V1_0.xsl";
    private static final String FOP_CONFIG_RESOURCE = "fop/fop.xconf";
    private static final List<String> FONT_RESOURCES = List.of(
            "fop/Cadastra.ttf",
            "fop/CadastraBd.ttf",
            "fop/CadastraBI.ttf",
            "fop/CadastraIt.ttf"
    );
    private static final Map<String, String> LOCALE_RESOURCES = Map.of(
            "de", "xslt/Resources.de.resx",
            "fr", "xslt/Resources.fr.resx",
            "it", "xslt/Resources.it.resx",
            "rm", "xslt/Resources.rm.resx"
    );
    private static final Processor PROCESSOR = createProcessor();
    private static final QName LOCALE_URL_PARAMETER = new QName("localeUrl");
    private static final QName DEBUG_TABLE_GRID_PARAMETER = new QName("debugTableGrid");
    private static final RuntimeResources RUNTIME = initializeRuntimeResources();

    /**
     * Cache fuer explizit angegebene XSLT-Dateien nach normalisiertem Dateipfad.
     * Der Cache wird ueber den Last-Modified-Zeitstempel invalidiert.
     */
    private final Map<Path, CachedXsltExecutable> explicitXsltCache = new ConcurrentHashMap<>();

    /**
     * Erstellt einen Konverter mit standardmaessig initialisierten Runtime-Ressourcen.
     */
    public DefaultPdfConverter() {
    }

    /**
     * Fuehrt die Konvertierung anhand der Anfrageparameter aus.
     * Bei Fehlern wird eine unvollstaendige Ausgabedatei nach Moeglichkeit entfernt.
     *
     * @param request Konvertierungsanfrage.
     * @return Ergebnis mit Ausgabedatei, Format und effektiv verwendetem XSLT.
     * @throws ConversionException wenn Validierung oder Konvertierung fehlschlaegt.
     */
    @Override
    public ConversionResult convert(ConversionRequest request) throws ConversionException {
        Objects.requireNonNull(request, "request must not be null");

        Path xmlFile = validateXmlFile(request.xmlFile());
        Path outputDirectory = validateOutputDirectory(request.outputDirectory());
        Path xsltFile = validateXsltFile(request.xsltFile());
        Path outputFile = outputDirectory.resolve(baseName(xmlFile) + "." + request.outputFormat().fileExtension());

        try {
            if (request.outputFormat() == OutputFormat.PDF) {
                transformToPdf(xmlFile, outputFile, xsltFile, request.locale(), request.debugTableGrid());
            } else {
                transformToFo(xmlFile, outputFile, xsltFile, request.locale(), request.debugTableGrid());
            }
            return new ConversionResult(outputFile, request.outputFormat(), effectiveXsltPath(xsltFile));
        } catch (ConversionException e) {
            deleteIncompleteOutput(outputFile);
            throw e;
        } catch (Exception e) {
            deleteIncompleteOutput(outputFile);
            throw new ConversionException("Conversion failed for XML file " + xmlFile, e);
        }
    }

    /**
     * Transformiert die XML-Datei in ein PDF.
     *
     * @param xmlFile validierte XML-Datei.
     * @param outputFile Zieldatei fuer das PDF.
     * @param explicitXsltFile optionales explizites XSLT.
     * @param locale gewuenschte Sprache.
     * @param debugTableGrid aktiviert ein sichtbares Debug-Gitter fuer Tabellen.
     * @throws Exception wenn Lesen, XSLT-Auswertung oder PDF-Erzeugung fehlschlaegt.
     */
    private void transformToPdf(
            Path xmlFile,
            Path outputFile,
            Path explicitXsltFile,
            Locale locale,
            boolean debugTableGrid
    ) throws Exception {
        XdmNode source = loadXml(xmlFile);
        XsltExecutable xsltExecutable = xsltExecutable(explicitXsltFile);

        try (OutputStream outputStream = Files.newOutputStream(
                outputFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {
            XsltTransformer transformer = configureTransformer(xsltExecutable, source, locale, debugTableGrid);
            FopFactory fopFactory = FopFactory.newInstance(RUNTIME.fopConfigPath().toFile());
            FOUserAgent userAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, outputStream);
            transformer.setDestination(new SAXDestination(fop.getDefaultHandler()));
            transformer.transform();
        }
    }

    /**
     * Transformiert die XML-Datei in ein XSL-FO-Dokument.
     *
     * @param xmlFile validierte XML-Datei.
     * @param outputFile Zieldatei fuer das FO-Dokument.
     * @param explicitXsltFile optionales explizites XSLT.
     * @param locale gewuenschte Sprache.
     * @param debugTableGrid aktiviert ein sichtbares Debug-Gitter fuer Tabellen.
     * @throws Exception wenn Lesen, XSLT-Auswertung oder Schreiben fehlschlaegt.
     */
    private void transformToFo(
            Path xmlFile,
            Path outputFile,
            Path explicitXsltFile,
            Locale locale,
            boolean debugTableGrid
    ) throws Exception {
        XdmNode source = loadXml(xmlFile);
        XsltExecutable xsltExecutable = xsltExecutable(explicitXsltFile);

        XsltTransformer transformer = configureTransformer(xsltExecutable, source, locale, debugTableGrid);
        Serializer serializer = PROCESSOR.newSerializer(outputFile.toFile());
        transformer.setDestination(serializer);
        transformer.transform();
    }

    /**
     * Konfiguriert einen Transformer mit Quellknoten und Locale-Ressource.
     *
     * @param xsltExecutable kompiliertes XSLT.
     * @param source geladenes XML als Kontextknoten.
     * @param locale gewuenschte Sprache.
     * @param debugTableGrid aktiviert ein sichtbares Debug-Gitter fuer Tabellen.
     * @return konfigurierter Transformer.
     */
    private XsltTransformer configureTransformer(
            XsltExecutable xsltExecutable,
            XdmNode source,
            Locale locale,
            boolean debugTableGrid
    ) {
        XsltTransformer transformer = xsltExecutable.load();
        transformer.setInitialContextNode(source);
        transformer.setParameter(LOCALE_URL_PARAMETER, new XdmAtomicValue(resolveLocaleResource(locale).toUri().toString()));
        transformer.setParameter(DEBUG_TABLE_GRID_PARAMETER, new XdmAtomicValue(debugTableGrid));
        return transformer;
    }

    /**
     * Laedt ein XML-Dokument in den Saxon-Dokumentbaum.
     *
     * @param xmlFile Pfad zur XML-Datei.
     * @return geladener XDM-Knoten.
     * @throws Exception wenn das XML nicht gelesen oder geparst werden kann.
     */
    private XdmNode loadXml(Path xmlFile) throws Exception {
        return PROCESSOR.newDocumentBuilder().build(new StreamSource(xmlFile.toFile()));
    }

    /**
     * Liefert ein kompiliertes XSLT fuer die Anfrage.
     * Ohne explizites XSLT wird das vorab kompilierte Standard-XSLT genutzt.
     * Mit explizitem XSLT wird nach Last-Modified gecacht.
     *
     * @param explicitXsltFile optionales explizites XSLT.
     * @return kompiliertes XSLT.
     * @throws Exception wenn das XSLT nicht kompiliert werden kann.
     */
    private XsltExecutable xsltExecutable(Path explicitXsltFile) throws Exception {
        if (explicitXsltFile == null) {
            return RUNTIME.defaultXsltExecutable();
        }

        Path normalizedPath = explicitXsltFile.toAbsolutePath().normalize();
        long lastModifiedMillis = Files.getLastModifiedTime(normalizedPath).toMillis();
        CachedXsltExecutable cached = explicitXsltCache.get(normalizedPath);
        if (cached != null && cached.lastModifiedMillis() == lastModifiedMillis) {
            return cached.executable();
        }

        XsltExecutable compiled = compileXslt(normalizedPath);
        explicitXsltCache.put(normalizedPath, new CachedXsltExecutable(lastModifiedMillis, compiled));
        return compiled;
    }

    /**
     * Kompiliert ein XSLT-Dokument zur Laufzeit.
     *
     * @param xsltFile Pfad zur XSLT-Datei.
     * @return kompiliertes XSLT.
     * @throws Exception wenn das XSLT nicht geladen oder kompiliert werden kann.
     */
    private XsltExecutable compileXslt(Path xsltFile) throws Exception {
        XsltCompiler compiler = PROCESSOR.newXsltCompiler();
        return compiler.compile(new StreamSource(xsltFile.toFile()));
    }

    /**
     * Validiert eine XML-Datei auf Existenz und Lesbarkeit.
     *
     * @param xmlFile zu pruefender Pfad.
     * @return absoluter, normalisierter Pfad.
     * @throws ConversionException wenn der Pfad fehlt oder nicht lesbar ist.
     */
    private Path validateXmlFile(Path xmlFile) {
        Path normalized = requirePath(xmlFile, "xmlFile").toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalized) || !Files.isReadable(normalized)) {
            throw new ConversionException("XML file does not exist or is not readable: " + normalized);
        }
        return normalized;
    }

    /**
     * Validiert eine optionale XSLT-Datei auf Existenz und Lesbarkeit.
     *
     * @param xsltFile optionaler XSLT-Pfad.
     * @return absoluter, normalisierter Pfad oder {@code null}.
     * @throws ConversionException wenn ein gesetzter Pfad nicht lesbar ist.
     */
    private Path validateXsltFile(Path xsltFile) {
        if (xsltFile == null) {
            return null;
        }

        Path normalized = xsltFile.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalized) || !Files.isReadable(normalized)) {
            throw new ConversionException("XSLT file does not exist or is not readable: " + normalized);
        }
        return normalized;
    }

    /**
     * Validiert das Ausgabeverzeichnis und erstellt es bei Bedarf.
     *
     * @param outputDirectory zu pruefendes Zielverzeichnis.
     * @return absoluter, normalisierter Verzeichnispfad.
     * @throws ConversionException wenn Erstellen, Verzeichnispruefung oder Schreibpruefung fehlschlaegt.
     */
    private Path validateOutputDirectory(Path outputDirectory) {
        Path normalized = requirePath(outputDirectory, "outputDirectory").toAbsolutePath().normalize();
        try {
            Files.createDirectories(normalized);
        } catch (IOException e) {
            throw new ConversionException("Failed to create output directory: " + normalized, e);
        }
        if (!Files.isDirectory(normalized)) {
            throw new ConversionException("Output path is not a directory: " + normalized);
        }
        if (!Files.isWritable(normalized)) {
            throw new ConversionException("Output directory is not writable: " + normalized);
        }
        return normalized;
    }

    /**
     * Ermittelt den effektiv verwendeten XSLT-Pfad fuer das Ergebnisobjekt.
     *
     * @param explicitXsltFile optionales explizites XSLT.
     * @return explizites XSLT oder Standard-XSLT.
     */
    private Path effectiveXsltPath(Path explicitXsltFile) {
        return explicitXsltFile == null ? RUNTIME.defaultXsltPath() : explicitXsltFile;
    }

    /**
     * Waehlt die Lokalisierungsressource anhand der Sprache.
     * Fallback-Reihenfolge: gewuenschte Sprache, danach Deutsch.
     *
     * @param locale gewuenschte Locale, optional.
     * @return Pfad zur passenden Ressourcen-Datei.
     */
    private Path resolveLocaleResource(Locale locale) {
        String language = locale == null ? Locale.GERMAN.getLanguage() : locale.getLanguage();
        Path localePath = RUNTIME.localeResources().get(language);
        if (localePath != null) {
            return localePath;
        }
        return RUNTIME.localeResources().get(Locale.GERMAN.getLanguage());
    }

    /**
     * Extrahiert den Dateinamen ohne Endung.
     *
     * @param xmlFile Eingabedatei.
     * @return Dateibasisname ohne Dateiendung.
     */
    private String baseName(Path xmlFile) {
        String fileName = xmlFile.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName;
    }

    /**
     * Erzwingt einen gesetzten Pfadwert.
     *
     * @param path zu pruefender Pfad.
     * @param label Bezeichner fuer die Fehlermeldung.
     * @return unveraenderter Pfad.
     * @throws ConversionException wenn {@code path} {@code null} ist.
     */
    private Path requirePath(Path path, String label) {
        if (path == null) {
            throw new ConversionException(label + " must not be null");
        }
        return path;
    }

    /**
     * Entfernt eine moeglicherweise unvollstaendige Ausgabedatei.
     *
     * @param outputFile zu loeschende Datei.
     */
    private void deleteIncompleteOutput(Path outputFile) {
        try {
            Files.deleteIfExists(outputFile);
        } catch (IOException e) {
            log.debug("Failed to delete incomplete output file {}", outputFile, e);
        }
    }

    /**
     * Initialisiert Runtime-Ressourcen aus dem Classpath in ein Temp-Verzeichnis.
     * Dabei werden Standard-XSLT, FOP-Konfiguration, Fonts und Locale-Dateien kopiert
     * und das Standard-XSLT einmalig vorkompiliert.
     *
     * @return zusammengefasste Runtime-Ressourcen.
     */
    private static RuntimeResources initializeRuntimeResources() {
        try {
            Path runtimeDirectory = Files.createTempDirectory("pdf4av-runtime-");
            Path defaultXsltPath = copyResource(DEFAULT_XSLT_RESOURCE, runtimeDirectory);
            Path fopConfigPath = copyResource(FOP_CONFIG_RESOURCE, runtimeDirectory);

            for (String fontResource : FONT_RESOURCES) {
                copyResource(fontResource, runtimeDirectory);
            }

            Map<String, Path> localeResources = new ConcurrentHashMap<>();
            for (Map.Entry<String, String> entry : LOCALE_RESOURCES.entrySet()) {
                localeResources.put(entry.getKey(), copyResource(entry.getValue(), runtimeDirectory));
            }

            XsltExecutable defaultXsltExecutable = compileXsltStatic(defaultXsltPath);
            return new RuntimeResources(runtimeDirectory, defaultXsltPath, fopConfigPath, Map.copyOf(localeResources), defaultXsltExecutable);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Kompiliert das Standard-XSLT fuer die statische Initialisierung.
     *
     * @param xsltFile XSLT-Datei.
     * @return kompiliertes XSLT.
     * @throws Exception wenn die Kompilierung fehlschlaegt.
     */
    private static XsltExecutable compileXsltStatic(Path xsltFile) throws Exception {
        XsltCompiler compiler = PROCESSOR.newXsltCompiler();
        return compiler.compile(new StreamSource(xsltFile.toFile()));
    }

    /**
     * Kopiert eine Classpath-Ressource in ein Zielverzeichnis.
     *
     * @param resourceName Name der Classpath-Ressource.
     * @param targetDirectory Zielverzeichnis.
     * @return Pfad zur kopierten Datei.
     * @throws IOException wenn Ressource fehlt oder nicht kopiert werden kann.
     */
    private static Path copyResource(String resourceName, Path targetDirectory) throws IOException {
        Path targetPath = targetDirectory.resolve(resourceName.substring(resourceName.lastIndexOf('/') + 1));
        try (InputStream inputStream = DefaultPdfConverter.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IOException("Classpath resource not found: " + resourceName);
            }
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return targetPath;
    }

    /**
     * Wertobjekt fuer ein gecachtes XSLT zusammen mit dessen Last-Modified-Zeitstempel.
     *
     * @param lastModifiedMillis Last-Modified in Millisekunden.
     * @param executable kompiliertes XSLT.
     */
    private record CachedXsltExecutable(long lastModifiedMillis, XsltExecutable executable) {
    }

    /**
     * Gebuendelte Laufzeitressourcen fuer Konvertierung und PDF-Erstellung.
     *
     * @param runtimeDirectory Temp-Verzeichnis mit kopierten Ressourcen.
     * @param defaultXsltPath kopiertes Standard-XSLT.
     * @param fopConfigPath kopierte FOP-Konfiguration.
     * @param localeResources Sprache zu Ressourcen-Datei.
     * @param defaultXsltExecutable vorkompiliertes Standard-XSLT.
     */
    private record RuntimeResources(
            Path runtimeDirectory,
            Path defaultXsltPath,
            Path fopConfigPath,
            Map<String, Path> localeResources,
            XsltExecutable defaultXsltExecutable
    ) {
    }

    /**
     * Erstellt die Saxon-Instanz und registriert projektspezifische Extensionfunktionen.
     *
     * @return konfigurierte Saxon-Processor-Instanz.
     */
    private static Processor createProcessor() {
        Processor processor = new Processor(false);
        processor.getUnderlyingConfiguration().registerExtensionFunction(new NormalizeImageFunction());
        processor.getUnderlyingConfiguration().registerExtensionFunction(new TitlePagePlanImageFunction());
        processor.getUnderlyingConfiguration().registerExtensionFunction(new PlanForProjectedObjectsImageFunction());
        processor.getUnderlyingConfiguration().registerExtensionFunction(new PlanForLandDescriptionImageFunction());
        return processor;
    }
}
