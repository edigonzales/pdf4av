package ch.so.agi.av.webservice;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SkeletonPdfConverterTest {
    @Test
    void convertThrowsUntilImplementationExists() {
        PdfConverter converter = new SkeletonPdfConverter();
        ConversionRequest request = new ConversionRequest(Path.of("input.xml"), Path.of("out"), false);
        assertThrows(ConversionException.class, () -> converter.convert(request));
    }
}
