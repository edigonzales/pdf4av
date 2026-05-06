package ch.so.agi.av.webservice;

import java.nio.file.Path;
import java.util.Objects;

public record ConversionResult(Path outputFile, OutputFormat outputFormat, Path xsltFileUsed) {
    public ConversionResult {
        outputFile = Objects.requireNonNull(outputFile, "outputFile must not be null");
        outputFormat = Objects.requireNonNull(outputFormat, "outputFormat must not be null");
        xsltFileUsed = Objects.requireNonNull(xsltFileUsed, "xsltFileUsed must not be null");
    }
}
