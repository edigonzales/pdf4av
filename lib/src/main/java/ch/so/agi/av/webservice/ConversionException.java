package ch.so.agi.av.webservice;

/**
 * Signalisiert einen fachlichen oder technischen Fehler waehrend der Konvertierung.
 */
public class ConversionException extends RuntimeException {
    /**
     * Erstellt eine neue Exception mit Fehlermeldung.
     *
     * @param message beschreibende Fehlermeldung.
     */
    public ConversionException(String message) {
        super(message);
    }

    /**
     * Erstellt eine neue Exception mit Fehlermeldung und Ursache.
     *
     * @param message beschreibende Fehlermeldung.
     * @param cause zugrunde liegende Ursache.
     */
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
