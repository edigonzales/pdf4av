package ch.so.agi.av.webservice;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Beschreibt eine Konvertierungsanfrage fuer XML nach PDF oder XSL-FO.
 *
 * @param xmlFile Pfad zur Eingabe-XML.
 * @param outputDirectory Zielverzeichnis fuer die erzeugte Ausgabe.
 * @param outputFormat gewuenschtes Ausgabeformat.
 * @param xsltFile optionales explizites XSLT; {@code null} bedeutet Standard-XSLT.
 * @param locale gewuenschte Sprache; {@code null} wird als Deutsch behandelt.
 * @param debugTableGrid aktiviert ein sichtbares Debug-Gitter fuer Tabellen in der XSL-Ausgabe.
 */
public record ConversionRequest(
        Path xmlFile,
        Path outputDirectory,
        OutputFormat outputFormat,
        Path xsltFile,
        Locale locale,
        boolean debugTableGrid
) {
    /**
     * Erstellt eine Konvertierungsanfrage ohne Tabellen-Debug-Gitter.
     *
     * @param xmlFile Pfad zur Eingabe-XML.
     * @param outputDirectory Zielverzeichnis fuer die Ausgabe.
     * @param outputFormat gewuenschtes Ausgabeformat.
     * @param xsltFile optionaler Pfad zu einem expliziten XSLT.
     * @param locale gewuenschte Sprache oder {@code null}.
     */
    public ConversionRequest(
            Path xmlFile,
            Path outputDirectory,
            OutputFormat outputFormat,
            Path xsltFile,
            Locale locale
    ) {
        this(xmlFile, outputDirectory, outputFormat, xsltFile, locale, false);
    }

    /**
     * Normalisiert und validiert die uebergebenen Anfrageparameter.
     * {@code locale} faellt auf Deutsch zurueck, und {@code xsltFile} wird bei Belegung
     * auf einen absoluten, normalisierten Pfad gesetzt.
     *
     * @param xmlFile Pfad zur Eingabe-XML.
     * @param outputDirectory Zielverzeichnis fuer die Ausgabe.
     * @param outputFormat gewuenschtes Ausgabeformat.
     * @param xsltFile optionaler Pfad zu einem expliziten XSLT.
     * @param locale gewuenschte Sprache oder {@code null}.
     * @param debugTableGrid aktiviert ein sichtbares Debug-Gitter fuer Tabellen.
     */
    public ConversionRequest {
        xmlFile = Objects.requireNonNull(xmlFile, "xmlFile must not be null");
        outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory must not be null");
        outputFormat = Objects.requireNonNull(outputFormat, "outputFormat must not be null");
        locale = locale == null ? Locale.GERMAN : locale;
        xsltFile = xsltFile == null ? null : xsltFile.toAbsolutePath().normalize();
    }
}
