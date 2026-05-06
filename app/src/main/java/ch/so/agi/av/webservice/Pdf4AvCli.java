package ch.so.agi.av.webservice;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(
        name = "pdf4av",
        mixinStandardHelpOptions = true,
        description = "Converts a simple XML document into either PDF or XSL-FO."
)
public class Pdf4AvCli implements Callable<Integer> {
    @Option(names = "--xml", required = true, description = "Input XML file.")
    private Path xmlFile;

    @Option(names = "--out", required = true, description = "Output directory.")
    private Path outputDirectory;

    @Option(names = "--format", defaultValue = "pdf", description = "Output format: pdf or fo.")
    private String outputFormat;

    @Option(names = "--xslt", description = "Optional custom XSLT file.")
    private Path xsltFile;

    @Option(names = "--locale", defaultValue = "de", description = "Locale used during transformation, e.g. de or fr.")
    private String localeTag;

    @Spec
    private CommandSpec spec;

    private final PdfConverter converter;

    public Pdf4AvCli() {
        this(new DefaultPdfConverter());
    }

    Pdf4AvCli(PdfConverter converter) {
        this.converter = converter;
    }

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        PrintWriter err = spec.commandLine().getErr();

        try {
            ConversionRequest request = new ConversionRequest(
                    xmlFile,
                    outputDirectory,
                    parseOutputFormat(outputFormat),
                    xsltFile,
                    Locale.forLanguageTag(localeTag)
            );
            ConversionResult result = converter.convert(request);
            out.println(result.outputFile().toAbsolutePath());
            return 0;
        } catch (ConversionException e) {
            err.println(e.getMessage());
            return 1;
        }
    }

    private OutputFormat parseOutputFormat(String value) {
        try {
            return OutputFormat.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ConversionException("Unsupported output format: " + value);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Pdf4AvCli()).execute(args);
        System.exit(exitCode);
    }
}
