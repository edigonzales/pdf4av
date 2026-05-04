package ch.so.agi.av.webservice;

import java.nio.file.Path;

public record ConversionRequest(Path xmlFile, Path outputDirectory, boolean writeFoOnly) {
}
