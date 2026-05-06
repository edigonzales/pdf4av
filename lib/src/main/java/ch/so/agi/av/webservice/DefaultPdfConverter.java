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

public class DefaultPdfConverter implements PdfConverter {
    private static final Logger log = LoggerFactory.getLogger(DefaultPdfConverter.class);

    private static final String DEFAULT_XSLT_RESOURCE = "xslt/default-document-to-fo.xsl";
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
    private static final Processor PROCESSOR = new Processor(false);
    private static final QName LOCALE_URL_PARAMETER = new QName("localeUrl");
    private static final RuntimeResources RUNTIME = initializeRuntimeResources();

    private final Map<Path, CachedXsltExecutable> explicitXsltCache = new ConcurrentHashMap<>();

    @Override
    public ConversionResult convert(ConversionRequest request) throws ConversionException {
        Objects.requireNonNull(request, "request must not be null");

        Path xmlFile = validateXmlFile(request.xmlFile());
        Path outputDirectory = validateOutputDirectory(request.outputDirectory());
        Path xsltFile = validateXsltFile(request.xsltFile());
        Path outputFile = outputDirectory.resolve(baseName(xmlFile) + "." + request.outputFormat().fileExtension());

        try {
            if (request.outputFormat() == OutputFormat.PDF) {
                transformToPdf(xmlFile, outputFile, xsltFile, request.locale());
            } else {
                transformToFo(xmlFile, outputFile, xsltFile, request.locale());
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

    private void transformToPdf(Path xmlFile, Path outputFile, Path explicitXsltFile, Locale locale) throws Exception {
        XdmNode source = loadXml(xmlFile);
        XsltExecutable xsltExecutable = xsltExecutable(explicitXsltFile);

        try (OutputStream outputStream = Files.newOutputStream(
                outputFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {
            XsltTransformer transformer = configureTransformer(xsltExecutable, source, locale);
            FOUserAgent userAgent = RUNTIME.fopFactory().newFOUserAgent();
            Fop fop = RUNTIME.fopFactory().newFop(MimeConstants.MIME_PDF, userAgent, outputStream);
            transformer.setDestination(new SAXDestination(fop.getDefaultHandler()));
            transformer.transform();
        }
    }

    private void transformToFo(Path xmlFile, Path outputFile, Path explicitXsltFile, Locale locale) throws Exception {
        XdmNode source = loadXml(xmlFile);
        XsltExecutable xsltExecutable = xsltExecutable(explicitXsltFile);

        XsltTransformer transformer = configureTransformer(xsltExecutable, source, locale);
        Serializer serializer = PROCESSOR.newSerializer(outputFile.toFile());
        transformer.setDestination(serializer);
        transformer.transform();
    }

    private XsltTransformer configureTransformer(XsltExecutable xsltExecutable, XdmNode source, Locale locale) {
        XsltTransformer transformer = xsltExecutable.load();
        transformer.setInitialContextNode(source);
        transformer.setParameter(LOCALE_URL_PARAMETER, new XdmAtomicValue(resolveLocaleResource(locale).toUri().toString()));
        return transformer;
    }

    private XdmNode loadXml(Path xmlFile) throws Exception {
        return PROCESSOR.newDocumentBuilder().build(new StreamSource(xmlFile.toFile()));
    }

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

    private XsltExecutable compileXslt(Path xsltFile) throws Exception {
        XsltCompiler compiler = PROCESSOR.newXsltCompiler();
        return compiler.compile(new StreamSource(xsltFile.toFile()));
    }

    private Path validateXmlFile(Path xmlFile) {
        Path normalized = requirePath(xmlFile, "xmlFile").toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalized) || !Files.isReadable(normalized)) {
            throw new ConversionException("XML file does not exist or is not readable: " + normalized);
        }
        return normalized;
    }

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

    private Path effectiveXsltPath(Path explicitXsltFile) {
        return explicitXsltFile == null ? RUNTIME.defaultXsltPath() : explicitXsltFile;
    }

    private Path resolveLocaleResource(Locale locale) {
        String language = locale == null ? Locale.GERMAN.getLanguage() : locale.getLanguage();
        Path localePath = RUNTIME.localeResources().get(language);
        if (localePath != null) {
            return localePath;
        }
        return RUNTIME.localeResources().get(Locale.GERMAN.getLanguage());
    }

    private String baseName(Path xmlFile) {
        String fileName = xmlFile.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName;
    }

    private Path requirePath(Path path, String label) {
        if (path == null) {
            throw new ConversionException(label + " must not be null");
        }
        return path;
    }

    private void deleteIncompleteOutput(Path outputFile) {
        try {
            Files.deleteIfExists(outputFile);
        } catch (IOException e) {
            log.debug("Failed to delete incomplete output file {}", outputFile, e);
        }
    }

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

            FopFactory fopFactory = FopFactory.newInstance(fopConfigPath.toFile());
            XsltExecutable defaultXsltExecutable = compileXsltStatic(defaultXsltPath);
            return new RuntimeResources(runtimeDirectory, defaultXsltPath, Map.copyOf(localeResources), fopFactory, defaultXsltExecutable);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static XsltExecutable compileXsltStatic(Path xsltFile) throws Exception {
        XsltCompiler compiler = PROCESSOR.newXsltCompiler();
        return compiler.compile(new StreamSource(xsltFile.toFile()));
    }

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

    private record CachedXsltExecutable(long lastModifiedMillis, XsltExecutable executable) {
    }

    private record RuntimeResources(
            Path runtimeDirectory,
            Path defaultXsltPath,
            Map<String, Path> localeResources,
            FopFactory fopFactory,
            XsltExecutable defaultXsltExecutable
    ) {
    }
}
