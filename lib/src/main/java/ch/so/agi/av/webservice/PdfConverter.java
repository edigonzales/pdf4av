package ch.so.agi.av.webservice;

import java.nio.file.Path;
import java.util.Locale;

public interface PdfConverter {
    ConversionResult convert(ConversionRequest request) throws ConversionException;

    default ConversionResult xmlToPdf(Path xmlFile, Path outputDirectory, Locale locale) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.PDF, null, locale));
    }

    default ConversionResult xmlToPdf(Path xmlFile, Path xsltFile, Path outputDirectory, Locale locale) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.PDF, xsltFile, locale));
    }

    default ConversionResult xmlToFo(Path xmlFile, Path outputDirectory, Locale locale) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.FO, null, locale));
    }

    default ConversionResult xmlToFo(Path xmlFile, Path xsltFile, Path outputDirectory, Locale locale) {
        return convert(new ConversionRequest(xmlFile, outputDirectory, OutputFormat.FO, xsltFile, locale));
    }
}
