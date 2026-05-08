package ch.so.agi.av.webservice;

/**
 * Legacy-Adapter, der aus Kompatibilitaetsgruenden weiterhin vorhanden ist und
 * die Implementierung von {@link DefaultPdfConverter} unveraendert uebernimmt.
 *
 * @deprecated Verwenden Sie direkt {@link DefaultPdfConverter}.
 */
@Deprecated(forRemoval = false)
public class SkeletonPdfConverter extends DefaultPdfConverter {
    /**
     * Erstellt einen Legacy-Adapter, der die Standardimplementierung verwendet.
     */
    public SkeletonPdfConverter() {
    }
}
