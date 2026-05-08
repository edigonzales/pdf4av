package ch.so.agi.av.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

class PlanImageRendererTest {
    private static final Processor PROCESSOR = new Processor(false);

    @Test
    void renderPlanImageDrawsRubberbandScaleBarAndNorthArrow() throws Exception {
        ParsedNodes nodes = parsedNodes(sampleXml(planPngBase64(), true, true));

        String rendered = PlanImageRenderer.renderPlanImage(nodes.planForMainPage(), nodes.limit(), "de", 174.0, 99.0);
        BufferedImage image = decodeImage(rendered);

        assertTrue(hasRedPixelNear(image, 348, 198, 24));
        assertTrue(hasRedPixelNear(image, 696, 396, 24));
        assertFalse(hasRedPixelNear(image, 870, 495, 12));

        BufferedImage scaleBarImage = expectedScaleBarImage(image.getWidth());
        assertNotNull(scaleBarImage);
        int scaleBarX = image.getWidth() / 20;
        int scaleBarY = (int) Math.round(image.getHeight() - (image.getHeight() / 20.0) - scaleBarImage.getHeight());
        int expectedArrowCenterX = scaleBarX + scaleBarImage.getWidth() / 2;

        Rectangle northArrowBounds = boundingBoxOfNonWhitePixels(
                image,
                new Rectangle(scaleBarX, Math.max(0, scaleBarY - 90), scaleBarImage.getWidth(), 90)
        );
        assertNotNull(northArrowBounds);
        assertTrue(Math.abs(expectedArrowCenterX - centerX(northArrowBounds)) <= 2);
    }

    @Test
    void renderPlanImageUsesBoundingBoxOfSelectedPlanNode() throws Exception {
        ParsedNodes nodes = parsedNodes(sampleXml(planPngBase64(), true, true));

        BufferedImage projectedObjectsImage = decodeImage(
                PlanImageRenderer.renderPlanImage(nodes.planForProjectedObjects(), nodes.limit(), "de", 174.0, 99.0)
        );
        BufferedImage landDescriptionImage = decodeImage(
                PlanImageRenderer.renderPlanImage(nodes.planForLandDescription(), nodes.limit(), "de", 174.0, 99.0)
        );

        assertTrue(hasRedPixelNear(projectedObjectsImage, 174, 198, 24));
        assertFalse(hasRedPixelNear(projectedObjectsImage, 1566, 198, 20));

        assertTrue(hasRedPixelNear(landDescriptionImage, 696, 198, 24));
        assertFalse(hasRedPixelNear(landDescriptionImage, 174, 198, 20));
    }

    @Test
    void renderPlanImageReturnsBaseImageWhenBoundingBoxMissing() throws Exception {
        ParsedNodes nodes = parsedNodes(sampleXml(planPngBase64(), false, true));

        BufferedImage image = decodeImage(
                PlanImageRenderer.renderPlanImage(nodes.planForMainPage(), nodes.limit(), "de", 174.0, 99.0)
        );

        assertEquals(0xFFFFFF, image.getRGB(348, 198) & 0xFFFFFF);
        assertNull(boundingBoxOfNonWhitePixels(image, new Rectangle(0, image.getHeight() - 120, 650, 120)));
    }

    @Test
    void renderPlanImageIgnoresReferenceWmsWhenNoBlobExists() throws Exception {
        ParsedNodes nodes = parsedNodes(sampleXml(planPngBase64(), true, false));

        String rendered = PlanImageRenderer.renderPlanImage(nodes.planForProjectedObjects(), nodes.limit(), "de", 174.0, 99.0);
        assertTrue(rendered.isBlank());
    }

    private ParsedNodes parsedNodes(String xml) throws Exception {
        XdmNode documentNode = PROCESSOR.newDocumentBuilder().build(new StreamSource(new StringReader(xml)));

        net.sf.saxon.s9api.XPathCompiler xpathCompiler = PROCESSOR.newXPathCompiler();
        xpathCompiler.declareNamespace("extract", "http://schemas.geo.admin.ch/V_D/AV/1.0/Extract");
        xpathCompiler.declareNamespace("data", "http://schemas.geo.admin.ch/V_D/AV/1.0/ExtractData");

        XdmNode limit = (XdmNode) xpathCompiler.evaluateSingle("/extract:GetExtractByIdResponse/data:Extract/data:RealEstate_DPR/data:Limit", documentNode);
        XdmNode planForMainPage = (XdmNode) xpathCompiler.evaluateSingle("/extract:GetExtractByIdResponse/data:Extract/data:RealEstate_DPR/data:PlanForMainPage", documentNode);
        XdmNode planForProjectedObjects = (XdmNode) xpathCompiler.evaluateSingle("/extract:GetExtractByIdResponse/data:Extract/data:RealEstate_DPR/data:PlanForProjectedObjects", documentNode);
        XdmNode planForLandDescription = (XdmNode) xpathCompiler.evaluateSingle("/extract:GetExtractByIdResponse/data:Extract/data:RealEstate_DPR/data:PlanForLandDescription", documentNode);

        return new ParsedNodes(
                planForMainPage.getUnderlyingNode(),
                planForProjectedObjects.getUnderlyingNode(),
                planForLandDescription.getUnderlyingNode(),
                limit.getUnderlyingNode()
        );
    }

    private BufferedImage decodeImage(String base64) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
    }

    private boolean hasRedPixelNear(BufferedImage image, int expectedX, int expectedY, int radius) {
        int minX = Math.max(0, expectedX - radius);
        int maxX = Math.min(image.getWidth() - 1, expectedX + radius);
        int minY = Math.max(0, expectedY - radius);
        int maxY = Math.min(image.getHeight() - 1, expectedY + radius);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                if (red > 200 && green < 180 && blue < 180) {
                    return true;
                }
            }
        }
        return false;
    }

    private Rectangle boundingBoxOfNonWhitePixels(BufferedImage image, Rectangle searchArea) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        int startX = Math.max(0, searchArea.x);
        int startY = Math.max(0, searchArea.y);
        int endX = Math.min(image.getWidth(), searchArea.x + searchArea.width);
        int endY = Math.min(image.getHeight(), searchArea.y + searchArea.height);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                if ((image.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (minX == Integer.MAX_VALUE) {
            return null;
        }

        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private int centerX(Rectangle rectangle) {
        return rectangle.x + rectangle.width / 2;
    }

    private BufferedImage expectedScaleBarImage(int imageWidthPx) throws IOException {
        double actualDpi = (imageWidthPx / 174.0) * 25.4;
        double dpiRatio = actualDpi / 300.0;

        ScalebarGenerator scalebarGenerator = new ScalebarGenerator();
        scalebarGenerator.setColorText(java.awt.Color.BLACK);
        scalebarGenerator.setDrawScaleText(false);
        scalebarGenerator.setHeight(50);
        scalebarGenerator.setTopMargin(15);
        scalebarGenerator.setLrbMargin(25);
        scalebarGenerator.setNumberOfSegments(2);
        if (actualDpi < 100) {
            scalebarGenerator.setHeight(40);
            scalebarGenerator.setLrbMargin(20);
            scalebarGenerator.setTextFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, (int) Math.round(20 * 0.5)));
        }

        double scale = 100.0 / (174.0 / 1000.0);
        byte[] imageBytes = scalebarGenerator.getImageAsByte(scale, 400 * dpiRatio, actualDpi);
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }

    private String sampleXml(String planPngBase64, boolean includeBoundingBox, boolean includeProjectedObjectBlob) {
        String mainPageBounds = includeBoundingBox
                ? """
                        <ns2:min>
                          <ns3:c1>0</ns3:c1>
                          <ns3:c2>0</ns3:c2>
                        </ns2:min>
                        <ns2:max>
                          <ns3:c1>100</ns3:c1>
                          <ns3:c2>100</ns3:c2>
                        </ns2:max>
                        """
                : "";
        String projectedObjectBlob = includeProjectedObjectBlob
                ? "<ns2:Blob>%1$s</ns2:Blob>"
                : "";

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <GetExtractByIdResponse xmlns="http://schemas.geo.admin.ch/V_D/AV/1.0/Extract"
                        xmlns:ns2="http://schemas.geo.admin.ch/V_D/AV/1.0/ExtractData"
                        xmlns:ns3="http://www.interlis.ch/geometry/1.0">
                  <ns2:Extract>
                    <ns2:RealEstate_DPR>
                      <ns2:Limit>
                        <ns3:surface>
                          <ns3:exterior>
                            <ns3:polyline>
                              <ns3:coord><ns3:c1>20</ns3:c1><ns3:c2>20</ns3:c2></ns3:coord>
                              <ns3:coord><ns3:c1>80</ns3:c1><ns3:c2>20</ns3:c2></ns3:coord>
                              <ns3:coord><ns3:c1>80</ns3:c1><ns3:c2>80</ns3:c2></ns3:coord>
                              <ns3:coord><ns3:c1>20</ns3:c1><ns3:c2>80</ns3:c2></ns3:coord>
                              <ns3:coord><ns3:c1>20</ns3:c1><ns3:c2>20</ns3:c2></ns3:coord>
                            </ns3:polyline>
                          </ns3:exterior>
                          <ns3:interior>
                            <ns3:polyline>
                              <ns3:coord><ns3:c1>40</ns3:c1><ns3:c2>40</ns3:c2></ns3:coord>
                              <ns3:coord><ns3:c1>60</ns3:c1><ns3:c2>40</ns3:c2></ns3:coord>
                              <ns3:coord><ns3:c1>60</ns3:c1><ns3:c2>60</ns3:c2></ns3:coord>
                              <ns3:coord><ns3:c1>40</ns3:c1><ns3:c2>60</ns3:c2></ns3:coord>
                              <ns3:coord><ns3:c1>40</ns3:c1><ns3:c2>40</ns3:c2></ns3:coord>
                            </ns3:polyline>
                          </ns3:interior>
                        </ns3:surface>
                      </ns2:Limit>
                      <ns2:PlanForMainPage>
                        <ns2:Image>
                          <ns2:LocalisedBlob>
                            <ns2:Language>de</ns2:Language>
                            <ns2:Blob>%1$s</ns2:Blob>
                          </ns2:LocalisedBlob>
                        </ns2:Image>
                %2$s
                      </ns2:PlanForMainPage>
                      <ns2:PlanForProjectedObjects>
                        <ns2:Image>
                          <ns2:LocalisedBlob>
                            <ns2:Language>de</ns2:Language>
                            %3$s
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
                            <ns2:Blob>%1$s</ns2:Blob>
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
                """.formatted(planPngBase64, mainPageBounds, projectedObjectBlob.formatted(planPngBase64));
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

    private record ParsedNodes(
            net.sf.saxon.om.NodeInfo planForMainPage,
            net.sf.saxon.om.NodeInfo planForProjectedObjects,
            net.sf.saxon.om.NodeInfo planForLandDescription,
            net.sf.saxon.om.NodeInfo limit
    ) {
    }
}
