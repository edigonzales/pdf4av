package ch.so.agi.av.webservice;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Beschreibt das Ergebnis einer erfolgreich ausgefuehrten Konvertierung.
 *
 * @param outputFile erzeugte Ausgabedatei.
 * @param outputFormat effektiv erzeugtes Ausgabeformat.
 * @param xsltFileUsed effektiv verwendetes XSLT (Standard oder explizit).
 */
public record ConversionResult(Path outputFile, OutputFormat outputFormat, Path xsltFileUsed) {
    /**
     * Validiert die Ergebniswerte auf Pflichtfelder.
     *
     * @param outputFile erzeugte Ausgabedatei.
     * @param outputFormat effektiv erzeugtes Ausgabeformat.
     * @param xsltFileUsed effektiv verwendetes XSLT.
     */
    public ConversionResult {
        outputFile = Objects.requireNonNull(outputFile, "outputFile must not be null");
        outputFormat = Objects.requireNonNull(outputFormat, "outputFormat must not be null");
        xsltFileUsed = Objects.requireNonNull(xsltFileUsed, "xsltFileUsed must not be null");
    }
}
