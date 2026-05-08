package ch.so.agi.av.webservice;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Definiert die Konvertierung eines AV-Extrakt-XML in PDF oder XSL-FO.
 */
public interface PdfConverter {
    /**
     * Konvertiert eine Anfrage gemaess {@link ConversionRequest}.
     *
     * @param request konfiguriertes Konvertierungs-Request mit Eingabe, Ausgabe, Format, optionalem XSLT und Locale.
     * @return Ergebnis der Konvertierung inklusive erzeugter Datei und effektiv verwendetem XSLT.
     * @throws ConversionException wenn Validierung, Transformation oder Ausgabeschreiben fehlschlaegt.
     */
    ConversionResult convert(ConversionRequest request) throws ConversionException;

    /**
     * Konvertiert XML mit Standard-XSLT direkt nach PDF.
     *
     * @param xmlFile Eingabe-XML.
     * @param outputDirectory Zielverzeichnis.
     * @param locale gewuenschte Sprache fuer lokalisierte Inhalte.
     * @return Konvertierungsergebnis.
     * @throws ConversionException wenn Validierung oder Konvertierung fehlschlaegt.
     */
    default ConversionResult xmlToPdf(Path xmlFile, Path outputDirectory, Locale locale) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.PDF, null, locale));
    }

    /**
     * Konvertiert XML mit Standard-XSLT direkt nach PDF und schaltet optional das Tabellen-Debug-Gitter ein.
     *
     * @param xmlFile Eingabe-XML.
     * @param outputDirectory Zielverzeichnis.
     * @param locale gewuenschte Sprache fuer lokalisierte Inhalte.
     * @param debugTableGrid {@code true}, wenn Tabellen mit sichtbarem Debug-Gitter gerendert werden sollen.
     * @return Konvertierungsergebnis.
     * @throws ConversionException wenn Validierung oder Konvertierung fehlschlaegt.
     */
    default ConversionResult xmlToPdf(Path xmlFile, Path outputDirectory, Locale locale, boolean debugTableGrid) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.PDF, null, locale, debugTableGrid));
    }

    /**
     * Konvertiert XML mit explizitem XSLT direkt nach PDF.
     *
     * @param xmlFile Eingabe-XML.
     * @param xsltFile explizit zu verwendendes XSLT.
     * @param outputDirectory Zielverzeichnis.
     * @param locale gewuenschte Sprache fuer lokalisierte Inhalte.
     * @return Konvertierungsergebnis.
     * @throws ConversionException wenn Validierung oder Konvertierung fehlschlaegt.
     */
    default ConversionResult xmlToPdf(Path xmlFile, Path xsltFile, Path outputDirectory, Locale locale) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.PDF, xsltFile, locale));
    }

    /**
     * Konvertiert XML mit explizitem XSLT direkt nach PDF und schaltet optional das Tabellen-Debug-Gitter ein.
     *
     * @param xmlFile Eingabe-XML.
     * @param xsltFile explizit zu verwendendes XSLT.
     * @param outputDirectory Zielverzeichnis.
     * @param locale gewuenschte Sprache fuer lokalisierte Inhalte.
     * @param debugTableGrid {@code true}, wenn Tabellen mit sichtbarem Debug-Gitter gerendert werden sollen.
     * @return Konvertierungsergebnis.
     * @throws ConversionException wenn Validierung oder Konvertierung fehlschlaegt.
     */
    default ConversionResult xmlToPdf(Path xmlFile, Path xsltFile, Path outputDirectory, Locale locale, boolean debugTableGrid) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.PDF, xsltFile, locale, debugTableGrid));
    }

    /**
     * Konvertiert XML mit Standard-XSLT nach XSL-FO.
     *
     * @param xmlFile Eingabe-XML.
     * @param outputDirectory Zielverzeichnis.
     * @param locale gewuenschte Sprache fuer lokalisierte Inhalte.
     * @return Konvertierungsergebnis.
     * @throws ConversionException wenn Validierung oder Konvertierung fehlschlaegt.
     */
    default ConversionResult xmlToFo(Path xmlFile, Path outputDirectory, Locale locale) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.FO, null, locale));
    }

    /**
     * Konvertiert XML mit Standard-XSLT nach XSL-FO und schaltet optional das Tabellen-Debug-Gitter ein.
     *
     * @param xmlFile Eingabe-XML.
     * @param outputDirectory Zielverzeichnis.
     * @param locale gewuenschte Sprache fuer lokalisierte Inhalte.
     * @param debugTableGrid {@code true}, wenn Tabellen mit sichtbarem Debug-Gitter gerendert werden sollen.
     * @return Konvertierungsergebnis.
     * @throws ConversionException wenn Validierung oder Konvertierung fehlschlaegt.
     */
    default ConversionResult xmlToFo(Path xmlFile, Path outputDirectory, Locale locale, boolean debugTableGrid) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.FO, null, locale, debugTableGrid));
    }

    /**
     * Konvertiert XML mit explizitem XSLT nach XSL-FO.
     *
     * @param xmlFile Eingabe-XML.
     * @param xsltFile explizit zu verwendendes XSLT.
     * @param outputDirectory Zielverzeichnis.
     * @param locale gewuenschte Sprache fuer lokalisierte Inhalte.
     * @return Konvertierungsergebnis.
     * @throws ConversionException wenn Validierung oder Konvertierung fehlschlaegt.
     */
    default ConversionResult xmlToFo(Path xmlFile, Path xsltFile, Path outputDirectory, Locale locale) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.FO, xsltFile, locale));
    }

    /**
     * Konvertiert XML mit explizitem XSLT nach XSL-FO und schaltet optional das Tabellen-Debug-Gitter ein.
     *
     * @param xmlFile Eingabe-XML.
     * @param xsltFile explizit zu verwendendes XSLT.
     * @param outputDirectory Zielverzeichnis.
     * @param locale gewuenschte Sprache fuer lokalisierte Inhalte.
     * @param debugTableGrid {@code true}, wenn Tabellen mit sichtbarem Debug-Gitter gerendert werden sollen.
     * @return Konvertierungsergebnis.
     * @throws ConversionException wenn Validierung oder Konvertierung fehlschlaegt.
     */
    default ConversionResult xmlToFo(Path xmlFile, Path xsltFile, Path outputDirectory, Locale locale, boolean debugTableGrid) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.FO, xsltFile, locale, debugTableGrid));
    }
}
