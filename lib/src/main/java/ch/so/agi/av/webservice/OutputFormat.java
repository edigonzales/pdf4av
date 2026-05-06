package ch.so.agi.av.webservice;

public enum OutputFormat {
    PDF("pdf"),
    FO("fo");

    private final String fileExtension;

    OutputFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String fileExtension() {
        return fileExtension;
    }
}
