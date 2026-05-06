package ch.so.agi.av.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultPdfConverterTest {
    @TempDir
    Path tempDir;

    @Test
    void xmlToPdfCreatesPdfFile() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleXml(tempDir.resolve("input.xml"));
        Path outputDirectory = tempDir.resolve("out-pdf");

        ConversionResult result = converter.xmlToPdf(xmlFile, outputDirectory, Locale.GERMAN);

        assertEquals(OutputFormat.PDF, result.outputFormat());
        assertTrue(Files.exists(result.outputFile()));
        assertTrue(Files.size(result.outputFile()) > 0);

        try (PDDocument pdfDocument = Loader.loadPDF(result.outputFile().toFile())) {
            assertEquals(1, pdfDocument.getNumberOfPages());
        }
    }

    @Test
    void xmlToFoCreatesFoFile() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleXml(tempDir.resolve("input.xml"));
        Path outputDirectory = tempDir.resolve("out-fo");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        assertEquals(OutputFormat.FO, result.outputFormat());
        assertTrue(Files.exists(result.outputFile()));

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(fo.contains("<fo:root"));
        assertTrue(fo.contains("Ein einfacher Titel"));
        assertTrue(fo.contains("Erster Absatz."));
    }

    @Test
    void explicitXsltOverridesDefaultStylesheet() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleXml(tempDir.resolve("input.xml"));
        Path outputDirectory = tempDir.resolve("out-custom-fo");
        Path xsltFile = writeCustomXslt(tempDir.resolve("custom.xsl"));

        ConversionResult result = converter.xmlToFo(xmlFile, xsltFile, outputDirectory, Locale.FRENCH);

        assertEquals(xsltFile.toAbsolutePath().normalize(), result.xsltFileUsed());
        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(fo.contains("Custom Formatter"));
        assertTrue(fo.contains("fr"));
    }

    @Test
    void convertSupportsParallelRequests() throws Exception {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleXml(tempDir.resolve("parallel.xml"));

        try (var executorService = Executors.newFixedThreadPool(4)) {
            List<Callable<ConversionResult>> tasks = List.of(
                    () -> converter.xmlToPdf(xmlFile, tempDir.resolve("parallel-pdf-1"), Locale.GERMAN),
                    () -> converter.xmlToPdf(xmlFile, tempDir.resolve("parallel-pdf-2"), Locale.FRENCH),
                    () -> converter.xmlToFo(xmlFile, tempDir.resolve("parallel-fo-1"), Locale.ITALIAN),
                    () -> converter.xmlToFo(xmlFile, tempDir.resolve("parallel-fo-2"), Locale.forLanguageTag("rm"))
            );

            List<Future<ConversionResult>> futures = executorService.invokeAll(tasks);
            for (Future<ConversionResult> future : futures) {
                ConversionResult result = future.get();
                assertTrue(Files.exists(result.outputFile()));
                assertTrue(Files.size(result.outputFile()) > 0);
            }
        }
    }

    @Test
    void convertFailsWhenXmlFileDoesNotExist() {
        PdfConverter converter = new DefaultPdfConverter();
        ConversionRequest request = new ConversionRequest(
                tempDir.resolve("missing.xml"),
                tempDir.resolve("out"),
                OutputFormat.PDF,
                null,
                Locale.GERMAN
        );

        assertThrows(ConversionException.class, () -> converter.convert(request));
    }

    @Test
    void convertFailsWhenXsltIsInvalid() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleXml(tempDir.resolve("input.xml"));
        Path invalidXslt = tempDir.resolve("invalid.xsl");
        Files.writeString(invalidXslt, "<not xslt>", StandardCharsets.UTF_8);

        assertThrows(ConversionException.class, () -> converter.xmlToFo(xmlFile, invalidXslt, tempDir.resolve("out"), Locale.GERMAN));
    }

    @Test
    void convertFailsWhenOutputPathIsAFile() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleXml(tempDir.resolve("input.xml"));
        Path outputFile = tempDir.resolve("not-a-directory");
        Files.writeString(outputFile, "blocking file", StandardCharsets.UTF_8);

        assertThrows(ConversionException.class, () -> converter.xmlToPdf(xmlFile, outputFile, Locale.GERMAN));
    }

    private Path writeSampleXml(Path path) throws IOException {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <document>
                  <title>Ein einfacher Titel</title>
                  <meta>
                    <author>Max Muster</author>
                    <subject>Testdokument</subject>
                    <keywords>pdf,fo,av</keywords>
                  </meta>
                  <content>
                    <paragraph>Erster Absatz.</paragraph>
                    <paragraph>Zweiter Absatz.</paragraph>
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
                    <xsl:variable name="localeCode" select="substring(tokenize($localeUrl, '[./]')[last() - 1], string-length(tokenize($localeUrl, '[./]')[last() - 1]) - 1)"/>
                    <xsl:template match="/document">
                        <fo:root font-family="Cadastra">
                            <fo:layout-master-set>
                                <fo:simple-page-master master-name="main" page-height="297mm" page-width="210mm" margin="18mm">
                                    <fo:region-body/>
                                </fo:simple-page-master>
                            </fo:layout-master-set>
                            <fo:page-sequence master-reference="main">
                                <fo:flow flow-name="xsl-region-body">
                                    <fo:block>Custom Formatter</fo:block>
                                    <fo:block><xsl:value-of select="$localeCode"/></fo:block>
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
