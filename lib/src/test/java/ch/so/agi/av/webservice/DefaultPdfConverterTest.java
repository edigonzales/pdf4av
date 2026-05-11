package ch.so.agi.av.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

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
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input.xml"), true, true);
        Path outputDirectory = tempDir.resolve("out-pdf");

        ConversionResult result = converter.xmlToPdf(xmlFile, outputDirectory, Locale.GERMAN);

        assertEquals(OutputFormat.PDF, result.outputFormat());
        assertTrue(Files.exists(result.outputFile()));
        assertTrue(Files.size(result.outputFile()) > 0);

        try (PDDocument pdfDocument = Loader.loadPDF(result.outputFile().toFile())) {
            assertEquals(3, pdfDocument.getNumberOfPages());
        }
    }

    @Test
    void xmlToFoCreatesFoFile() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input.xml"), true, true);
        Path outputDirectory = tempDir.resolve("out-fo");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        assertEquals(OutputFormat.FO, result.outputFormat());
        assertTrue(Files.exists(result.outputFile()));

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(fo.contains("<fo:root"));
        assertTrue(fo.contains("Auszug aus der amtlichen Vermessung mit"));
        assertTrue(fo.contains("CH123456789012"));
        assertTrue(fo.contains("Bleichmatt"));
    }

    @Test
    void xmlToFoUsesFixedTitleAndHeaderGeometry() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input-geometry.xml"), true, true);
        Path outputDirectory = tempDir.resolve("out-geometry");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(fo.contains("height=\"136mm\""));
        assertTrue(fo.contains("margin-bottom=\"0mm\""));
        assertTrue(fo.contains("<fo:region-body margin-top=\"16mm\" margin-bottom=\"21mm\"/>"));
        assertTrue(fo.contains("<fo:region-after extent=\"10mm\"/>"));
        assertTrue(fo.contains("top=\"8mm\""));
        assertTrue(fo.contains("height=\"42pt\""));
        assertTrue(fo.contains("top=\"29mm\""));
        assertTrue(fo.contains("width=\"174mm\""));
        assertTrue(fo.contains("height=\"99mm\""));
        assertTrue(fo.contains("top=\"17.93mm\""));
        assertTrue(fo.contains("top=\"0mm\""));
        assertTrue(fo.contains("top=\"0.9mm\""));
        assertTrue(fo.contains("absolute-position=\"absolute\" top=\"0mm\" left=\"0mm\" width=\"100%\""));
        assertTrue(fo.contains("absolute-position=\"absolute\"\n                                top=\"0.9mm\""));
        assertEquals(0, countOccurrences(fo, "margin-top=\"2mm\""));
    }

    @Test
    void xmlToFoKeepsDebugTableGridDisabledByDefault() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input-default-grid.xml"), true, true);
        Path outputDirectory = tempDir.resolve("out-default-grid");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertFalse(fo.contains("rgb(226, 0, 122)"));
        assertFalse(fo.contains("rgb(0, 132, 214)"));
        assertFalse(fo.contains("rgb(0, 153, 51)"));
    }

    @Test
    void xmlToFoRendersDebugTableGridWhenRequested() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input-debug-grid.xml"), true, true);
        Path outputDirectory = tempDir.resolve("out-debug-grid");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN, true);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(fo.contains("rgb(226, 0, 122)"));
        assertTrue(fo.contains("rgb(0, 132, 214)"));
        assertTrue(fo.contains("rgb(0, 153, 51)"));
        assertTrue(fo.contains("display-align=\"before\""));
    }

    @Test
    void xmlToFoRendersExtractTableWithIdentifierValueAndAdjustedTopSpacing() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input-extract-table.xml"), true, true);
        Path outputDirectory = tempDir.resolve("out-extract-table");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(fo.contains("Auszugsnummer"));
        assertFalse(fo.contains("Auszugsidentifikator"));
        assertEquals(2, countOccurrences(fo, "test-extract-123"));
        assertTrue(fo.contains("<fo:block font-weight=\"700\">test-extract-123</fo:block>"));
        assertTrue(fo.contains("<fo:inline padding-left=\"4mm\">test-extract-123</fo:inline>"));
        assertTrue(fo.contains("<fo:block-container margin-top=\"4.5mm\" font-size=\"8pt\">"));
        assertFalse(fo.contains("<fo:block-container margin-top=\"8mm\" font-size=\"8pt\">"));
        assertTrue(fo.contains("Erstellungsdatum des Auszugs"));
        assertTrue(fo.contains("Allgemeine Informationen"));
    }

    @Test
    void xmlToFoRendersLandDescriptionSectionWithGroupedLandCoversAndResponsibleOffice() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input-land-description.xml"), true, true);
        Path outputDirectory = tempDir.resolve("out-land-description");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(fo.contains("Grundstückbeschreibung"));
        assertTrue(fo.contains("Eigentumsauskunft"));
        assertTrue(fo.contains("PlanForLandDescription"));
        assertTrue(fo.contains("Bodenbedeckungsanteile"));
        assertTrue(fo.contains("Gebäude und Bauten"));
        assertTrue(fo.contains("Zuständige Stelle"));
        assertTrue(fo.contains("Amtschreiberei Olten-Gösgen, Amthausquai 23, 4601 Olten"));
        assertTrue(fo.contains("https://geo.so.ch/standortkarte/index.html?egid=376404"));
        assertTrue(fo.contains("url('https://geo.so.ch/standortkarte/index.html?egid=376404')"));
        assertEquals(1, countOccurrences(fo, "2'000 m²"));
        assertTrue(fo.contains("&lt; 1%"));
        assertTrue(fo.contains("unterirdisches Gebäude"));
        assertEquals(1, countOccurrences(fo, "Unterstand"));
        assertEquals(1, countOccurrences(fo, "2355731"));
        assertTrue(fo.contains("999999999"));
        assertTrue(fo.contains("Mühlemattstrasse 36"));
        assertTrue(fo.contains("Rheinstrasse 42a"));
        assertTrue(fo.contains("Jermann Ingenieure und Geometer AG, Gerbegässlein 5, 4450 Sissach"));
        assertTrue(fo.contains("https://www.jermann-ag.ch"));
        assertTrue(fo.contains("url('https://www.jermann-ag.ch')"));
        assertTrue(fo.indexOf("Eigentumsauskunft") < fo.indexOf("Grundstückbeschreibung"));
        assertTrue(fo.indexOf("420760") < fo.indexOf("2355661"));
        assertTrue(fo.indexOf("2355661") < fo.indexOf("2355731"));
        assertTrue(fo.indexOf("2355731") < fo.indexOf("999999999"));
    }

    @Test
    void xmlToFoUsesLandDescriptionGeometryAndColumnWidths() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input-land-description-geometry.xml"), true, true);
        Path outputDirectory = tempDir.resolve("out-land-description-geometry");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertEquals(2, countOccurrences(fo, "break-before=\"page\""));
        assertTrue(fo.contains("Eigentumsauskunft"));
        assertTrue(fo.contains("height=\"119.62mm\""));
        assertTrue(fo.contains("height=\"20.62mm\""));
        assertTrue(fo.contains("top=\"20.62mm\""));
        assertTrue(fo.contains("font-size=\"15pt\""));
        assertTrue(fo.contains("line-height=\"18pt\""));
        assertTrue(fo.contains("fox:alt-text=\"PlanForLandDescription\""));
        assertTrue(fo.contains("border=\"0.25pt solid black\""));
        assertTrue(fo.contains("column-width=\"131mm\""));
        assertTrue(fo.contains("column-width=\"15mm\""));
        assertTrue(fo.contains("column-width=\"28mm\""));
        assertTrue(fo.contains("column-width=\"40mm\""));
        assertTrue(fo.contains("column-width=\"36mm\""));
        assertTrue(fo.contains("column-width=\"52mm\""));
        assertTrue(fo.contains("column-width=\"11mm\""));
        assertTrue(fo.contains("column-width=\"35mm\""));
        assertTrue(fo.contains("space-after=\"4.51mm\""));
        assertTrue(fo.contains("font-size=\"6pt\""));
        assertTrue(fo.contains("line-height=\"8pt\""));
//        assertFalse(fo.contains("background-color=\"orange\""));
//        assertFalse(fo.contains("background-color=\"green\""));
//        assertFalse(fo.contains("background-color=\"yellow\""));
        assertEquals(1, countOccurrences(fo, "Grundstückbeschreibung"));
        assertTrue(fo.contains("Eigentumsauskunft"));
    }

    @Test
    void xmlToFoOmitsEmptyLandDescriptionSubsections() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(
                tempDir.resolve("input-land-description-empty.xml"),
                new SampleAvOptions(true, true, false, false, false, false, false, false, false, false, false)
        );
        Path outputDirectory = tempDir.resolve("out-land-description-empty");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(fo.contains("Grundstückbeschreibung"));
        assertEquals(1, countOccurrences(fo, "break-before=\"page\""));
        assertFalse(fo.contains("Bodenbedeckungsanteile"));
        assertFalse(fo.contains("Gebäude und Bauten"));
        assertFalse(fo.contains("Zuständige Stelle"));
    }

    @Test
    void xmlToFoRendersOwnershipInformationPageBetweenTitlePageAndLandDescription() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input-ownership-information.xml"), true, true);
        Path outputDirectory = tempDir.resolve("out-ownership-information");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(fo.contains("Eigentumsauskunft"));
        assertEquals(1, countOccurrences(fo, "Grundstückbeschreibung"));
        assertEquals(2, countOccurrences(fo, "break-before=\"page\""));
        assertTrue(fo.indexOf("Eigentumsauskunft") < fo.indexOf("Grundstückbeschreibung"));
        assertTrue(fo.contains("Amtschreiberei Olten-Gösgen, Amthausquai 23, 4601 Olten"));
        assertTrue(fo.contains("keep-with-next.within-page=\"always\">Zuständige Stelle</fo:block>"));
    }

    @Test
    void xmlToFoOmitsOwnershipInformationPageWhenLandRegisterOfficeIsMissing() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(
                tempDir.resolve("input-no-land-register-office.xml"),
                new SampleAvOptions(true, true, true, true, true, true, true, true, false, false, false)
        );
        Path outputDirectory = tempDir.resolve("out-no-land-register-office");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertEquals(1, countOccurrences(fo, "break-before=\"page\""));
        assertFalse(fo.contains("Amtschreiberei Olten-Gösgen"));
        assertTrue(fo.contains("Grundstückbeschreibung"));
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
        Path xmlFile = writeSampleAvXml(tempDir.resolve("parallel.xml"), true, true);

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
    void xmlToFoHandlesMissingOptionalFields() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(
                tempDir.resolve("missing-optional.xml"),
                new SampleAvOptions(false, false, true, true, true, false, true, true, false, true, false)
        );
        Path outputDirectory = tempDir.resolve("out-missing-optional");

        ConversionResult result = converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN);

        String fo = Files.readString(result.outputFile(), StandardCharsets.UTF_8);
        assertTrue(Files.exists(result.outputFile()));
        assertFalse(fo.contains("null"));
        assertFalse(fo.contains("url('')"));
    }

    @Test
    void xmlToFoFailsWhenBuildingTypeMatchIsAmbiguous() throws IOException {
        PdfConverter converter = new DefaultPdfConverter();
        Path xmlFile = writeSampleAvXml(
                tempDir.resolve("input-ambiguous-building.xml"),
                new SampleAvOptions(true, true, true, true, true, true, true, true, true, true, true)
        );
        Path outputDirectory = tempDir.resolve("out-ambiguous-building");

        ConversionException exception = assertThrows(ConversionException.class,
                () -> converter.xmlToFo(xmlFile, outputDirectory, Locale.GERMAN));

        assertTrue(exception.getMessage().contains("input-ambiguous-building.xml"));
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

    private Path writeSampleAvXml(Path path, boolean includeMunicipalityLogo, boolean includeWebsite) throws IOException {
        return writeSampleAvXml(path, new SampleAvOptions(includeMunicipalityLogo, includeWebsite, true, true, true, true, true, true, false, true, true));
    }

    private Path writeSampleAvXml(Path path, SampleAvOptions options) throws IOException {
        String tinyPng = tinyPngBase64();
        String planPng = planPngBase64();
        String municipalityLogo = options.includeMunicipalityLogo()
                ? "    <ns2:MunicipalityLogo>" + tinyPng + "</ns2:MunicipalityLogo>\n"
                : "";
        String propertyInfoWebsite = options.includePropertyInfoWebsite()
                ? """
                                <ns2:OfficeAtWeb>
                                  <ns2:LocalisedText>
                                    <ns2:Language>de</ns2:Language>
                                    <ns2:Text>https://geo.example.ch</ns2:Text>
                                  </ns2:LocalisedText>
                                </ns2:OfficeAtWeb>
                        """
                : "";
        String buildings = buildBuildingsXml(options);
        String landCovers = buildLandCoversXml(options);
        String singleObjects = buildSingleObjectsXml(options);
        String responsibleOffice = buildResponsibleOfficeXml(options);
        String landRegisterOffice = buildLandRegisterOfficeXml(options);

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <GetExtractByIdResponse xmlns="http://schemas.geo.admin.ch/V_D/AV/1.0/Extract"
                        xmlns:ns2="http://schemas.geo.admin.ch/V_D/AV/1.0/ExtractData"
                        xmlns:ns3="http://www.interlis.ch/geometry/1.0">
                  <ns2:Extract>
                    <ns2:CreationDate>2026-05-07T09:30:15.000+02:00</ns2:CreationDate>
                    <ns2:FederalLogo>%1$s</ns2:FederalLogo>
                    <ns2:CantonalLogo>%2$s</ns2:CantonalLogo>
                %3$s
                    <ns2:PropertyInformationLogo>%4$s</ns2:PropertyInformationLogo>
                    <ns2:ExtractIdentifier>test-extract-123</ns2:ExtractIdentifier>
                    <ns2:UpdateDateCS>2026-03-26T00:00:00.000+02:00</ns2:UpdateDateCS>
                    <ns2:Disclaimer>
                      <ns2:Content>
                        <ns2:LocalisedText>
                          <ns2:Language>de</ns2:Language>
                          <ns2:Text>Dieser Auszug dient als Testdokument.</ns2:Text>
                        </ns2:LocalisedText>
                      </ns2:Content>
                    </ns2:Disclaimer>
                    <ns2:PropertyInformationAuthority>
                      <ns2:Name>
                        <ns2:LocalisedText>
                          <ns2:Language>de</ns2:Language>
                          <ns2:Text>Amt für Geoinformation</ns2:Text>
                        </ns2:LocalisedText>
                      </ns2:Name>
                %5$s
                      <ns2:Street>Werkhofstrasse</ns2:Street>
                      <ns2:Number>65</ns2:Number>
                      <ns2:PostalCode>4509</ns2:PostalCode>
                      <ns2:City>Solothurn</ns2:City>
                    </ns2:PropertyInformationAuthority>
                    <ns2:RealEstate_DPR>
                      <ns2:Number>1000</ns2:Number>
                      <ns2:EGRID>CH123456789012</ns2:EGRID>
                      <ns2:Type>
                        <ns2:Text>
                          <ns2:LocalisedText>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Text>Liegenschaft</ns2:Text>
                          </ns2:LocalisedText>
                        </ns2:Text>
                      </ns2:Type>
                      <ns2:MunicipalityName>Liestal</ns2:MunicipalityName>
                      <ns2:MunicipalityCode>2829</ns2:MunicipalityCode>
                      <ns2:LandRegistryArea>11171</ns2:LandRegistryArea>
                      <ns2:Toponym>Bleichmatt</ns2:Toponym>
                      <ns2:Limit>
                        <ns3:surface>
                          <ns3:exterior>
                            <ns3:polyline>
                              <ns3:coord>
                                <ns3:c1>20</ns3:c1>
                                <ns3:c2>20</ns3:c2>
                              </ns3:coord>
                              <ns3:coord>
                                <ns3:c1>80</ns3:c1>
                                <ns3:c2>20</ns3:c2>
                              </ns3:coord>
                              <ns3:coord>
                                <ns3:c1>80</ns3:c1>
                                <ns3:c2>80</ns3:c2>
                              </ns3:coord>
                              <ns3:coord>
                                <ns3:c1>20</ns3:c1>
                                <ns3:c2>80</ns3:c2>
                              </ns3:coord>
                              <ns3:coord>
                                <ns3:c1>20</ns3:c1>
                                <ns3:c2>20</ns3:c2>
                              </ns3:coord>
                            </ns3:polyline>
                          </ns3:exterior>
                          <ns3:interior>
                            <ns3:polyline>
                              <ns3:coord>
                                <ns3:c1>40</ns3:c1>
                                <ns3:c2>40</ns3:c2>
                              </ns3:coord>
                              <ns3:coord>
                                <ns3:c1>60</ns3:c1>
                                <ns3:c2>40</ns3:c2>
                              </ns3:coord>
                              <ns3:coord>
                                <ns3:c1>60</ns3:c1>
                                <ns3:c2>60</ns3:c2>
                              </ns3:coord>
                              <ns3:coord>
                                <ns3:c1>40</ns3:c1>
                                <ns3:c2>60</ns3:c2>
                              </ns3:coord>
                              <ns3:coord>
                                <ns3:c1>40</ns3:c1>
                                <ns3:c2>40</ns3:c2>
                              </ns3:coord>
                            </ns3:polyline>
                          </ns3:interior>
                        </ns3:surface>
                      </ns2:Limit>
                %6$s
                %7$s
                %8$s
                      <ns2:PlanForMainPage>
                        <ns2:Image>
                          <ns2:LocalisedBlob>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Blob>%9$s</ns2:Blob>
                          </ns2:LocalisedBlob>
                        </ns2:Image>
                        <ns2:min>
                          <ns3:c1>0</ns3:c1>
                          <ns3:c2>0</ns3:c2>
                        </ns2:min>
                        <ns2:max>
                          <ns3:c1>100</ns3:c1>
                          <ns3:c2>100</ns3:c2>
                        </ns2:max>
                      </ns2:PlanForMainPage>
                      <ns2:PlanForProjectedObjects>
                        <ns2:Image>
                          <ns2:LocalisedBlob>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Blob>%10$s</ns2:Blob>
                          </ns2:LocalisedBlob>
                        </ns2:Image>
                        <ns2:min>
                          <ns3:c1>0</ns3:c1>
                          <ns3:c2>0</ns3:c2>
                        </ns2:min>
                        <ns2:max>
                          <ns3:c1>200</ns3:c1>
                          <ns3:c2>100</ns3:c2>
                        </ns2:max>
                        <ns2:ReferenceWMS>
                          <ns2:LocalisedText>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Text>https://example.invalid/projected</ns2:Text>
                          </ns2:LocalisedText>
                        </ns2:ReferenceWMS>
                      </ns2:PlanForProjectedObjects>
                      <ns2:PlanForLandDescription>
                        <ns2:Image>
                          <ns2:LocalisedBlob>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Blob>%11$s</ns2:Blob>
                          </ns2:LocalisedBlob>
                        </ns2:Image>
                        <ns2:min>
                          <ns3:c1>0</ns3:c1>
                          <ns3:c2>0</ns3:c2>
                        </ns2:min>
                        <ns2:max>
                          <ns3:c1>50</ns3:c1>
                          <ns3:c2>100</ns3:c2>
                        </ns2:max>
                        <ns2:ReferenceWMS>
                          <ns2:LocalisedText>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Text>https://example.invalid/land-description</ns2:Text>
                          </ns2:LocalisedText>
                        </ns2:ReferenceWMS>
                      </ns2:PlanForLandDescription>
                %12$s
                %13$s
                    </ns2:RealEstate_DPR>
                  </ns2:Extract>
                </GetExtractByIdResponse>
                """.formatted(
                tinyPng,
                tinyPng,
                municipalityLogo,
                tinyPng,
                propertyInfoWebsite,
                buildings,
                landCovers,
                singleObjects,
                planPng,
                planPng,
                planPng,
                responsibleOffice,
                landRegisterOffice
        );
        Files.writeString(path, xml, StandardCharsets.UTF_8);
        return path;
    }

    private String buildBuildingsXml(SampleAvOptions options) {
        if (!options.includeBuildings()) {
            return "";
        }

        StringBuilder xml = new StringBuilder();
        xml.append("""
                      <ns2:Building>
                        <ns2:EGID>420760</ns2:EGID>
                        <ns2:Area>150</ns2:Area>
                        <ns2:AreaShare>150</ns2:AreaShare>
                        <ns2:BuildingEntrance>
                          <ns2:EDID>0</ns2:EDID>
                          <ns2:Street>Mühlemattstrasse</ns2:Street>
                          <ns2:Number>32</ns2:Number>
                          <ns2:PostalCode>4410</ns2:PostalCode>
                          <ns2:City>Liestal</ns2:City>
                        </ns2:BuildingEntrance>
                      </ns2:Building>
                      <ns2:Building>
                        <ns2:EGID>2355661</ns2:EGID>
                        <ns2:Area>200</ns2:Area>
                        <ns2:AreaShare>200</ns2:AreaShare>
                        <ns2:BuildingEntrance>
                          <ns2:EDID>0</ns2:EDID>
                          <ns2:Street>Rheinstrasse</ns2:Street>
                          <ns2:Number>44</ns2:Number>
                          <ns2:PostalCode>4410</ns2:PostalCode>
                          <ns2:City>Liestal</ns2:City>
                        </ns2:BuildingEntrance>
                      </ns2:Building>
                """);

        if (options.includeBuildingWithoutEntrance()) {
            xml.append("""
                      <ns2:Building>
                        <ns2:EGID>999999999</ns2:EGID>
                        <ns2:Area>75</ns2:Area>
                        <ns2:AreaShare>75</ns2:AreaShare>
                      </ns2:Building>
                    """);
        }

        if (options.includeBuildingWithMultipleEntrances()) {
            xml.append("""
                      <ns2:Building>
                        <ns2:EGID>2355731</ns2:EGID>
                        <ns2:Area>225</ns2:Area>
                        <ns2:AreaShare>225</ns2:AreaShare>
                        <ns2:BuildingEntrance>
                          <ns2:EDID>0</ns2:EDID>
                          <ns2:Street>Mühlemattstrasse</ns2:Street>
                          <ns2:Number>36</ns2:Number>
                          <ns2:PostalCode>4410</ns2:PostalCode>
                          <ns2:City>Liestal</ns2:City>
                        </ns2:BuildingEntrance>
                        <ns2:BuildingEntrance>
                          <ns2:EDID>1</ns2:EDID>
                          <ns2:Street>Rheinstrasse</ns2:Street>
                          <ns2:Number>42a</ns2:Number>
                          <ns2:PostalCode>4410</ns2:PostalCode>
                          <ns2:City>Liestal</ns2:City>
                        </ns2:BuildingEntrance>
                      </ns2:Building>
                    """);
        }

        return xml.toString();
    }

    private String buildLandCoversXml(SampleAvOptions options) {
        if (!options.includeLandCovers()) {
            return "";
        }

        return """
                      <ns2:LandCover>
                        <ns2:Type>
                          <ns2:Code>buildings</ns2:Code>
                          <ns2:Text>
                            <ns2:LocalisedText>
                              <ns2:Language>de</ns2:Language>
                              <ns2:Text>Gebäude</ns2:Text>
                            </ns2:LocalisedText>
                          </ns2:Text>
                        </ns2:Type>
                        <ns2:Area>2000</ns2:Area>
                        <ns2:AreaShare>2000</ns2:AreaShare>
                        <ns2:EGID>420760</ns2:EGID>
                      </ns2:LandCover>
                      <ns2:LandCover>
                        <ns2:Type>
                          <ns2:Code>hard_surfaced.roads_tracks</ns2:Code>
                          <ns2:Text>
                            <ns2:LocalisedText>
                              <ns2:Language>de</ns2:Language>
                              <ns2:Text>Strasse, Weg</ns2:Text>
                            </ns2:LocalisedText>
                          </ns2:Text>
                        </ns2:Type>
                        <ns2:Area>30</ns2:Area>
                        <ns2:AreaShare>30</ns2:AreaShare>
                      </ns2:LandCover>
                      <ns2:LandCover>
                        <ns2:Type>
                          <ns2:Code>vegetated.garden</ns2:Code>
                          <ns2:Text>
                            <ns2:LocalisedText>
                              <ns2:Language>de</ns2:Language>
                              <ns2:Text>Gartenanlage</ns2:Text>
                            </ns2:LocalisedText>
                          </ns2:Text>
                        </ns2:Type>
                        <ns2:Area>7470</ns2:Area>
                        <ns2:AreaShare>7470</ns2:AreaShare>
                      </ns2:LandCover>
                """;
    }

    private String buildSingleObjectsXml(SampleAvOptions options) {
        if (!options.includeBuildings()) {
            return "";
        }

        StringBuilder xml = new StringBuilder("""
                      <ns2:SingleObject>
                        <ns2:Type>
                          <ns2:Code>underground_structure</ns2:Code>
                          <ns2:Text>
                            <ns2:LocalisedText>
                              <ns2:Language>de</ns2:Language>
                              <ns2:Text>unterirdisches Gebäude</ns2:Text>
                            </ns2:LocalisedText>
                          </ns2:Text>
                        </ns2:Type>
                        <ns2:EGID>2355661</ns2:EGID>
                      </ns2:SingleObject>
                      <ns2:SingleObject>
                        <ns2:Type>
                          <ns2:Code>wall</ns2:Code>
                          <ns2:Text>
                            <ns2:LocalisedText>
                              <ns2:Language>de</ns2:Language>
                              <ns2:Text>Mauer</ns2:Text>
                            </ns2:LocalisedText>
                          </ns2:Text>
                        </ns2:Type>
                      </ns2:SingleObject>
                """);

        if (options.includeBuildingWithMultipleEntrances()) {
            xml.append("""
                      <ns2:SingleObject>
                        <ns2:Type>
                          <ns2:Code>shelter</ns2:Code>
                          <ns2:Text>
                            <ns2:LocalisedText>
                              <ns2:Language>de</ns2:Language>
                              <ns2:Text>Unterstand</ns2:Text>
                            </ns2:LocalisedText>
                          </ns2:Text>
                        </ns2:Type>
                        <ns2:EGID>2355731</ns2:EGID>
                      </ns2:SingleObject>
                    """);
        }

        if (options.includeAmbiguousBuildingMatch()) {
            xml.append("""
                      <ns2:SingleObject>
                        <ns2:Type>
                          <ns2:Code>other_portion_of_building</ns2:Code>
                          <ns2:Text>
                            <ns2:LocalisedText>
                              <ns2:Language>de</ns2:Language>
                              <ns2:Text>übriger Gebäudeteil</ns2:Text>
                            </ns2:LocalisedText>
                          </ns2:Text>
                        </ns2:Type>
                        <ns2:EGID>420760</ns2:EGID>
                      </ns2:SingleObject>
                    """);
        }

        return xml.toString();
    }

    private String buildResponsibleOfficeXml(SampleAvOptions options) {
        if (!options.includeResponsibleOffice()) {
            return "";
        }

        String website = options.includeResponsibleOfficeWebsite()
                ? """
                        <ns2:OfficeAtWeb>
                          <ns2:LocalisedText>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Text>https://www.jermann-ag.ch</ns2:Text>
                          </ns2:LocalisedText>
                        </ns2:OfficeAtWeb>
                """
                : "";

        return """
                      <ns2:ResponsibleOffice>
                        <ns2:Name>
                          <ns2:LocalisedText>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Text>Jermann Ingenieure und Geometer AG</ns2:Text>
                          </ns2:LocalisedText>
                        </ns2:Name>
                %s
                        <ns2:Street>Gerbegässlein</ns2:Street>
                        <ns2:Number>5</ns2:Number>
                        <ns2:PostalCode>4450</ns2:PostalCode>
                        <ns2:City>Sissach</ns2:City>
                      </ns2:ResponsibleOffice>
                """.formatted(website);
    }

    private String buildLandRegisterOfficeXml(SampleAvOptions options) {
        if (!options.includeLandRegisterOffice()) {
            return "";
        }

        String website = options.includeLandRegisterOfficeWebsite()
                ? """
                        <ns2:OfficeAtWeb>
                          <ns2:LocalisedText>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Text>https://geo.so.ch/standortkarte/index.html?egid=376404</ns2:Text>
                          </ns2:LocalisedText>
                        </ns2:OfficeAtWeb>
                """
                : "";

        return """
                      <ns2:LandRegisterOffice>
                        <ns2:Name>
                          <ns2:LocalisedText>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Text>Amtschreiberei Olten-Gösgen</ns2:Text>
                          </ns2:LocalisedText>
                        </ns2:Name>
                %s
                        <ns2:Street>Amthausquai</ns2:Street>
                        <ns2:Number>23</ns2:Number>
                        <ns2:PostalCode>4601</ns2:PostalCode>
                        <ns2:City>Olten</ns2:City>
                      </ns2:LandRegisterOffice>
                """.formatted(website);
    }

    private record SampleAvOptions(
            boolean includeMunicipalityLogo,
            boolean includePropertyInfoWebsite,
            boolean includeLandCovers,
            boolean includeBuildings,
            boolean includeResponsibleOffice,
            boolean includeResponsibleOfficeWebsite,
            boolean includeBuildingWithoutEntrance,
            boolean includeBuildingWithMultipleEntrances,
            boolean includeAmbiguousBuildingMatch,
            boolean includeLandRegisterOffice,
            boolean includeLandRegisterOfficeWebsite
    ) {
    }

    private String tinyPngBase64() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xFFFFFF);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private String planPngBase64() throws IOException {
        BufferedImage image = new BufferedImage(1740, 990, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, 0xFFFFFF);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private Path writeCustomXslt(Path path) throws IOException {
        String xslt = """
                <?xml version="1.0" encoding="UTF-8"?>
                <xsl:stylesheet version="3.0"
                        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                        xmlns:fo="http://www.w3.org/1999/XSL/Format"
                        xmlns:xs="http://www.w3.org/2001/XMLSchema">
                    <xsl:param name="localeUrl"/>
                    <xsl:param name="debugTableGrid" as="xs:boolean" select="false()"/>
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

    private int countOccurrences(String value, String token) {
        return value.split(java.util.regex.Pattern.quote(token), -1).length - 1;
    }
}
