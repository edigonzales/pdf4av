package ch.so.agi.av.webservice;

public interface PdfConverter {
    ConversionResult convert(ConversionRequest request) throws ConversionException;
}
