package ch.so.agi.av.webservice;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public record ConversionRequest(
        Path xmlFile,
        Path outputDirectory,
        OutputFormat outputFormat,
        Path xsltFile,
        Locale locale
) {
    public ConversionRequest {
        xmlFile = Objects.requireNonNull(xmlFile, "xmlFile must not be null");
        outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory must not be null");
        outputFormat = Objects.requireNonNull(outputFormat, "outputFormat must not be null");
        locale = locale == null ? Locale.GERMAN : locale;
        xsltFile = xsltFile == null ? null : xsltFile.toAbsolutePath().normalize();
    }
}
