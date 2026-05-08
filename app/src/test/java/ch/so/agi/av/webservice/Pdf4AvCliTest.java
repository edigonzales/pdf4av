package ch.so.agi.av.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

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
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input.xml"));
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
        Path xmlFile = writeSampleAvXml(tempDir.resolve("input.xml"));
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

    @Test
    void cliForwardsDebugTableGridFlag() throws IOException {
        Path xmlFile = writeSampleXml(tempDir.resolve("input-debug.xml"));
        AtomicReference<ConversionRequest> requestRef = new AtomicReference<>();
        PdfConverter converter = request -> {
            try {
                requestRef.set(request);
                Files.createDirectories(request.outputDirectory());
                Path outputFile = request.outputDirectory().resolve("captured." + request.outputFormat().fileExtension());
                Files.writeString(outputFile, "ok", StandardCharsets.UTF_8);
                return new ConversionResult(outputFile, request.outputFormat(), request.outputDirectory().resolve("captured.xsl"));
            } catch (IOException e) {
                throw new ConversionException("Failed to capture CLI request.", e);
            }
        };
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();

        CommandLine commandLine = new CommandLine(new Pdf4AvCli(converter));
        commandLine.setOut(new PrintWriter(stdout, true, StandardCharsets.UTF_8));

        int exitCode = commandLine.execute(
                "--xml", xmlFile.toString(),
                "--out", tempDir.resolve("debug-out").toString(),
                "--debug-table-grid",
                "--locale", "fr"
        );

        assertEquals(0, exitCode);
        assertTrue(stdout.toString(StandardCharsets.UTF_8).contains("captured.pdf"));
        assertTrue(requestRef.get().debugTableGrid());
        assertEquals(Locale.FRENCH.getLanguage(), requestRef.get().locale().getLanguage());
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

    private Path writeSampleAvXml(Path path) throws IOException {
        String tinyPng = tinyPngBase64();
        String planPng = planPngBase64();
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <GetExtractByIdResponse xmlns="http://schemas.geo.admin.ch/V_D/AV/1.0/Extract"
                        xmlns:ns2="http://schemas.geo.admin.ch/V_D/AV/1.0/ExtractData"
                        xmlns:ns3="http://www.interlis.ch/geometry/1.0">
                  <ns2:Extract>
                    <ns2:CreationDate>2026-05-07T09:30:15.000+02:00</ns2:CreationDate>
                    <ns2:FederalLogo>%1$s</ns2:FederalLogo>
                    <ns2:CantonalLogo>%1$s</ns2:CantonalLogo>
                    <ns2:MunicipalityLogo>%1$s</ns2:MunicipalityLogo>
                    <ns2:PropertyInformationLogo>%1$s</ns2:PropertyInformationLogo>
                    <ns2:ExtractIdentifier>cli-test-extract-123</ns2:ExtractIdentifier>
                    <ns2:UpdateDateCS>2026-03-26T00:00:00.000+02:00</ns2:UpdateDateCS>
                    <ns2:Disclaimer>
                      <ns2:Content>
                        <ns2:LocalisedText>
                          <ns2:Language>de</ns2:Language>
                          <ns2:Text>Dieser Auszug dient als CLI-Testdokument.</ns2:Text>
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
                      <ns2:OfficeAtWeb>
                        <ns2:LocalisedText>
                          <ns2:Language>de</ns2:Language>
                          <ns2:Text>https://geo.example.ch</ns2:Text>
                        </ns2:LocalisedText>
                      </ns2:OfficeAtWeb>
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
                      <ns2:PlanForMainPage>
                        <ns2:Image>
                          <ns2:LocalisedBlob>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Blob>%2$s</ns2:Blob>
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
                            <ns2:Blob>%2$s</ns2:Blob>
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
                      </ns2:PlanForProjectedObjects>
                      <ns2:PlanForLandDescription>
                        <ns2:Image>
                          <ns2:LocalisedBlob>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Blob>%2$s</ns2:Blob>
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
                      </ns2:PlanForLandDescription>
                    </ns2:RealEstate_DPR>
                  </ns2:Extract>
                </GetExtractByIdResponse>
                """.formatted(tinyPng, planPng);
        Files.writeString(path, xml, StandardCharsets.UTF_8);
        return path;
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
