package ch.so.agi.av.webservice;

/**
 * Definiert die unterstuetzten Zielformate der Konvertierung.
 */
public enum OutputFormat {
    /** Erzeugt ein PDF-Dokument. */
    PDF("pdf"),
    /** Erzeugt ein XSL-FO-Dokument. */
    FO("fo");

    private final String fileExtension;

    /**
     * Erstellt einen Enum-Eintrag mit der Dateiendung.
     *
     * @param fileExtension Dateiendung ohne Punkt.
     */
    OutputFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Liefert die Dateiendung des Ausgabeformats ohne fuehrenden Punkt.
     *
     * @return Dateiendung des Formats.
     */
    public String fileExtension() {
        return fileExtension;
    }
}
