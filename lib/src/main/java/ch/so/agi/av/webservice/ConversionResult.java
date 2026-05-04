package ch.so.agi.av.webservice;

import java.nio.file.Path;

public record ConversionResult(Path generatedPdf, Path generatedFo) {
}
