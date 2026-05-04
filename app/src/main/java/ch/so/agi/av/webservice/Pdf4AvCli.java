package ch.so.agi.av.webservice;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "pdf4av",
        mixinStandardHelpOptions = true,
        description = "Skeleton CLI for XML/XSLT/XSL-FO to PDF conversion."
)
public class Pdf4AvCli implements Callable<Integer> {
    @Option(names = "--xml", required = true, description = "Input XML file.")
    private Path xmlFile;

    @Option(names = "--out", required = true, description = "Output directory.")
    private Path outputDirectory;

    @Option(names = "--fo", description = "Generate FO only.")
    private boolean writeFoOnly;

    private final PdfConverter converter;

    public Pdf4AvCli() {
        this(new SkeletonPdfConverter());
    }

    Pdf4AvCli(PdfConverter converter) {
        this.converter = converter;
    }

    @Override
    public Integer call() {
        ConversionRequest request = new ConversionRequest(xmlFile, outputDirectory, writeFoOnly);
        converter.convert(request);
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Pdf4AvCli()).execute(args);
        System.exit(exitCode);
    }
}
