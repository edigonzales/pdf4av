package ch.so.agi.av.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import picocli.CommandLine;

class Pdf4AvCliTest {
    @TempDir
    Path tempDir;

    @Test
    void helpReturnsExitCodeZero() {
        int exitCode = new CommandLine(new Pdf4AvCli()).execute("--help");
        assertEquals(0, exitCode);
    }

    @Test
    void cliCreatesPdfByDefault() throws IOException {
        Path xmlFile = writeSampleXml(tempDir.resolve("input.xml"));
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        CommandLine commandLine = new CommandLine(new Pdf4AvCli());
        commandLine.setOut(new PrintWriter(stdout, true, StandardCharsets.UTF_8));
        commandLine.setErr(new PrintWriter(stderr, true, StandardCharsets.UTF_8));

        int exitCode = commandLine.execute("--xml", xmlFile.toString(), "--out", tempDir.resolve("pdf-out").toString());

        assertEquals(0, exitCode);
        assertTrue(stderr.toString(StandardCharsets.UTF_8).isBlank());
        Path outputFile = Path.of(stdout.toString(StandardCharsets.UTF_8).trim());
        assertTrue(Files.exists(outputFile));
        assertTrue(outputFile.getFileName().toString().endsWith(".pdf"));
    }

    @Test
    void cliCreatesFoWhenRequested() throws IOException {
        Path xmlFile = writeSampleXml(tempDir.resolve("input.xml"));
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();

        CommandLine commandLine = new CommandLine(new Pdf4AvCli());
        commandLine.setOut(new PrintWriter(stdout, true, StandardCharsets.UTF_8));

        int exitCode = commandLine.execute(
                "--xml", xmlFile.toString(),
                "--out", tempDir.resolve("fo-out").toString(),
                "--format", "fo"
        );

        assertEquals(0, exitCode);
        Path outputFile = Path.of(stdout.toString(StandardCharsets.UTF_8).trim());
        assertTrue(Files.exists(outputFile));
        assertTrue(outputFile.getFileName().toString().endsWith(".fo"));
    }

    @Test
    void cliSupportsCustomXslt() throws IOException {
        Path xmlFile = writeSampleXml(tempDir.resolve("input.xml"));
        Path xsltFile = writeCustomXslt(tempDir.resolve("custom.xsl"));
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();

        CommandLine commandLine = new CommandLine(new Pdf4AvCli());
        commandLine.setOut(new PrintWriter(stdout, true, StandardCharsets.UTF_8));

        int exitCode = commandLine.execute(
                "--xml", xmlFile.toString(),
                "--out", tempDir.resolve("custom-out").toString(),
                "--format", "fo",
                "--xslt", xsltFile.toString(),
                "--locale", "fr"
        );

        assertEquals(0, exitCode);
        Path outputFile = Path.of(stdout.toString(StandardCharsets.UTF_8).trim());
        String fo = Files.readString(outputFile, StandardCharsets.UTF_8);
        assertTrue(fo.contains("CLI Custom XSLT"));
    }

    private Path writeSampleXml(Path path) throws IOException {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <document>
                  <title>Ein einfacher Titel</title>
                  <content>
                    <paragraph>Ein Absatz für die CLI.</paragraph>
                  </content>
                </document>
                """;
        Files.writeString(path, xml, StandardCharsets.UTF_8);
        return path;
    }

    private Path writeCustomXslt(Path path) throws IOException {
        String xslt = """
                <?xml version="1.0" encoding="UTF-8"?>
                <xsl:stylesheet version="3.0"
                        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                        xmlns:fo="http://www.w3.org/1999/XSL/Format">
                    <xsl:param name="localeUrl"/>
                    <xsl:template match="/document">
                        <fo:root font-family="Cadastra">
                            <fo:layout-master-set>
                                <fo:simple-page-master master-name="main" page-height="297mm" page-width="210mm" margin="18mm">
                                    <fo:region-body/>
                                </fo:simple-page-master>
                            </fo:layout-master-set>
                            <fo:page-sequence master-reference="main">
                                <fo:flow flow-name="xsl-region-body">
                                    <fo:block>CLI Custom XSLT</fo:block>
                                </fo:flow>
                            </fo:page-sequence>
                        </fo:root>
                    </xsl:template>
                </xsl:stylesheet>
                """;
        Files.writeString(path, xslt, StandardCharsets.UTF_8);
        return path;
    }
}
