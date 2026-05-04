package ch.so.agi.av.webservice;

public class SkeletonPdfConverter implements PdfConverter {
    @Override
    public ConversionResult convert(ConversionRequest request) throws ConversionException {
        throw new ConversionException("PDF conversion is not implemented yet.");
    }
}
