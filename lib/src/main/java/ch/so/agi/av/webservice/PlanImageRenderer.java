package ch.so.agi.av.webservice;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;

/**
 * Gemeinsame Renderlogik fuer georeferenzierte Planbilder.
 */
final class PlanImageRenderer {
    private static final double REFERENCE_DPI = 300.0;
    private static final float HIGHLIGHTING_STROKE_OPACITY = 0.4F;
    private static final Color HIGHLIGHTING_STROKE_COLOR = new Color(230 / 255F, 0F, 0F, HIGHLIGHTING_STROKE_OPACITY);
    private static final int HIGHLIGHTING_STROKE_WIDTH = (int) (5 * REFERENCE_DPI / 72.0);
    private static final String IMAGE_FORMAT = "png";
    private static final double NORTH_ARROW_SIZE_MM = 6.0;

    private PlanImageRenderer() {
    }

    static String renderPlanImage(NodeInfo planNode, NodeInfo limitNode, String locale, double mapWidthMm, double mapHeightMm) {
        String imageData = extractLocalizedBlob(planNode, locale);
        if (imageData.isBlank()) {
            return "";
        }

        BufferedImage sourceImage = decodeBase64Image(imageData);
        if (sourceImage == null) {
            return NormalizeImageFunction.normalizeImage(imageData);
        }

        Envelope worldEnvelope = calculateBoundingBox(planNode);
        MultiPolygon geometry = multiSurfaceToJts(limitNode);
        BufferedImage combinedImage = composePlanImage(sourceImage, worldEnvelope, geometry, mapWidthMm, mapHeightMm);
        return encodeBase64Png(combinedImage);
    }

    private static BufferedImage composePlanImage(
            BufferedImage sourceImage,
            Envelope worldEnvelope,
            MultiPolygon geometry,
            double mapWidthMm,
            double mapHeightMm
    ) {
        BufferedImage combinedImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = combinedImage.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, combinedImage.getWidth(), combinedImage.getHeight());
            graphics.drawImage(sourceImage, 0, 0, null);

            if (worldEnvelope == null) {
                return combinedImage;
            }

            BufferedImage overlayImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics2D overlayGraphics = overlayImage.createGraphics();
            try {
                configureOverlayGraphics(overlayGraphics, overlayImage);
                drawRubberband(overlayGraphics, geometry, worldEnvelope, sourceImage, mapWidthMm, mapHeightMm);
                drawScaleBarAndNorthArrow(overlayGraphics, sourceImage, worldEnvelope, mapWidthMm, mapHeightMm);
            } finally {
                overlayGraphics.dispose();
            }

            graphics.drawImage(overlayImage, 0, 0, null);
            return combinedImage;
        } finally {
            graphics.dispose();
        }
    }

    private static void configureOverlayGraphics(Graphics2D graphics, BufferedImage overlayImage) {
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        graphics.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        graphics.setBackground(Color.WHITE);
        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, overlayImage.getWidth(), overlayImage.getHeight());
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
    }

    private static void drawRubberband(
            Graphics2D graphics,
            MultiPolygon geometry,
            Envelope worldEnvelope,
            BufferedImage sourceImage,
            double mapWidthMm,
            double mapHeightMm
    ) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        Envelope pixelEnvelope = new Envelope(new Coordinate(0, 0), new Coordinate(sourceImage.getWidth(), sourceImage.getHeight()));
        AffinePointTransformation transformation = new AffinePointTransformation(pixelEnvelope, worldEnvelope);
        double actualDpi = calculateActualDpi(sourceImage, mapWidthMm);

        graphics.setColor(HIGHLIGHTING_STROKE_COLOR);
        graphics.setStroke(new BasicStroke(
                (float) (HIGHLIGHTING_STROKE_WIDTH / (REFERENCE_DPI / actualDpi)),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL
        ));

        ShapeWriter shapeWriter = new ShapeWriter(transformation);
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) geometry.getGeometryN(i);
            Shape polygonShape = shapeWriter.toShape(polygon);
            graphics.draw(polygonShape);
        }
    }

    private static void drawScaleBarAndNorthArrow(
            Graphics2D graphics,
            BufferedImage sourceImage,
            Envelope worldEnvelope,
            double mapWidthMm,
            double mapHeightMm
    ) {
        double actualDpi = calculateActualDpi(sourceImage, mapWidthMm);
        double dpiRatio = actualDpi / REFERENCE_DPI;
        double scale = worldEnvelope.getWidth() / (mapWidthMm / 1000.0);

        BufferedImage scaleBarImage = createScaleBarImage(scale, actualDpi, dpiRatio);
        if (scaleBarImage == null) {
            return;
        }

        int imageWidthPx = sourceImage.getWidth();
        int imageHeightPx = sourceImage.getHeight();
        int scaleBarX = imageWidthPx / 20;
        int scaleBarY = (int) Math.round(imageHeightPx - (imageHeightPx / 20.0) - scaleBarImage.getHeight());

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        graphics.drawImage(scaleBarImage, scaleBarX, scaleBarY, null);

        BufferedImage northArrowImage = loadNorthArrowImage();
        if (northArrowImage == null) {
            return;
        }

        int northArrowBoxSizePx = Math.max(1, (int) Math.round((NORTH_ARROW_SIZE_MM / 25.4) * actualDpi));
        int[] scaledArrowSize = scaleToFit(northArrowImage.getWidth(), northArrowImage.getHeight(), northArrowBoxSizePx, northArrowBoxSizePx);
        Image scaledNorthArrowImage = northArrowImage.getScaledInstance(scaledArrowSize[0], scaledArrowSize[1], Image.SCALE_SMOOTH);

        int northArrowX = scaleBarX + (scaleBarImage.getWidth() / 2) - (scaledArrowSize[0] / 2);
        int northArrowY = scaleBarY - scaledArrowSize[1];
        graphics.drawImage(scaledNorthArrowImage, northArrowX, northArrowY, null);
    }

    private static BufferedImage createScaleBarImage(double scale, double actualDpi, double dpiRatio) {
        ScalebarGenerator scalebarGenerator = new ScalebarGenerator();
        scalebarGenerator.setColorText(Color.BLACK);
        scalebarGenerator.setDrawScaleText(false);
        scalebarGenerator.setHeight(50);
        scalebarGenerator.setTopMargin(15);
        scalebarGenerator.setLrbMargin(25);
        scalebarGenerator.setNumberOfSegments(2);
        if (actualDpi < 100) {
            scalebarGenerator.setHeight(40);
            scalebarGenerator.setLrbMargin(20);
            scalebarGenerator.setTextFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) Math.round(20 * 0.5)));
        }

        double scaleBarWidthPx = 400 * dpiRatio;
        byte[] scaleBarImageBytes;
        try {
            scaleBarImageBytes = scalebarGenerator.getImageAsByte(scale, scaleBarWidthPx, actualDpi);
        } catch (IOException e) {
            return null;
        }

        if (scaleBarImageBytes.length == 0) {
            return null;
        }

        try (InputStream inputStream = new ByteArrayInputStream(scaleBarImageBytes)) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedImage loadNorthArrowImage() {
        try (InputStream inputStream = PlanImageRenderer.class.getResourceAsStream("/fop/north_arrow_small_V1.png")) {
            if (inputStream == null) {
                return null;
            }
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    private static double calculateActualDpi(BufferedImage image, double mapWidthMm) {
        return (image.getWidth() / mapWidthMm) * 25.4;
    }

    private static int[] scaleToFit(int width, int height, int maxWidth, int maxHeight) {
        double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
        return new int[]{
                Math.max(1, (int) Math.round(width * ratio)),
                Math.max(1, (int) Math.round(height * ratio))
        };
    }

    private static BufferedImage decodeBase64Image(String imageData) {
        try (InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(imageData.trim()))) {
            return ImageIO.read(inputStream);
        } catch (IllegalArgumentException | IOException e) {
            return null;
        }
    }

    private static String encodeBase64Png(BufferedImage image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, IMAGE_FORMAT, outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode plan image.", e);
        }
    }

    private static String extractLocalizedBlob(NodeInfo planNode, String locale) {
        if (planNode == null) {
            return "";
        }

        String normalizedLocale = normalize(locale);
        String firstBlob = "";
        AxisIterator iterator = planNode.iterateAxis(AxisInfo.DESCENDANT);
        for (Item item = iterator.next(); item != null; item = iterator.next()) {
            if (!(item instanceof NodeInfo nodeInfo) || !"LocalisedBlob".equals(nodeInfo.getLocalPart())) {
                continue;
            }

            String blob = childValue(nodeInfo, "Blob");
            if (blob.isBlank()) {
                continue;
            }

            if (firstBlob.isBlank()) {
                firstBlob = blob;
            }

            String language = childValue(nodeInfo, "Language");
            if (!normalizedLocale.isBlank() && normalizedLocale.equals(normalize(language))) {
                return blob;
            }
        }

        if (!firstBlob.isBlank()) {
            return firstBlob;
        }

        return descendantValue(planNode, "Blob");
    }

    private static Envelope calculateBoundingBox(NodeInfo planNode) {
        if (planNode == null) {
            return null;
        }

        Coordinate minCoordinate = coordinateFromChild(planNode, "min");
        Coordinate maxCoordinate = coordinateFromChild(planNode, "max");
        if (minCoordinate == null || maxCoordinate == null || minCoordinate.equals2D(maxCoordinate)) {
            return null;
        }

        return new Envelope(minCoordinate, maxCoordinate);
    }

    private static Coordinate coordinateFromChild(NodeInfo parent, String localName) {
        AxisIterator children = parent.iterateAxis(AxisInfo.CHILD);
        for (Item item = children.next(); item != null; item = children.next()) {
            if (item instanceof NodeInfo nodeInfo && localName.equals(nodeInfo.getLocalPart())) {
                Double c1 = numericChildValue(nodeInfo, "c1");
                Double c2 = numericChildValue(nodeInfo, "c2");
                if (c1 != null && c2 != null) {
                    return new Coordinate(c1, c2);
                }
            }
        }
        return null;
    }

    private static MultiPolygon multiSurfaceToJts(NodeInfo limitNode) {
        GeometryFactory geometryFactory = new GeometryFactory();
        if (limitNode == null) {
            return geometryFactory.createMultiPolygon();
        }

        List<Polygon> polygonList = new ArrayList<>();
        AxisIterator surfaces = limitNode.iterateAxis(AxisInfo.CHILD);
        for (Item surfaceItem = surfaces.next(); surfaceItem != null; surfaceItem = surfaces.next()) {
            if (!(surfaceItem instanceof NodeInfo surfaceNode) || !"surface".equals(surfaceNode.getLocalPart())) {
                continue;
            }

            LinearRing shell = null;
            List<LinearRing> holes = new ArrayList<>();

            AxisIterator surfaceChildren = surfaceNode.iterateAxis(AxisInfo.CHILD);
            for (Item ringItem = surfaceChildren.next(); ringItem != null; ringItem = surfaceChildren.next()) {
                if (!(ringItem instanceof NodeInfo ringNode)) {
                    continue;
                }

                if ("exterior".equals(ringNode.getLocalPart())) {
                    List<Coordinate> coordinates = polylineCoordinates(ringNode);
                    if (coordinates.size() >= 4) {
                        shell = geometryFactory.createLinearRing(closeRing(coordinates).toArray(new Coordinate[0]));
                    }
                } else if ("interior".equals(ringNode.getLocalPart())) {
                    List<Coordinate> coordinates = polylineCoordinates(ringNode);
                    if (coordinates.size() >= 4) {
                        holes.add(geometryFactory.createLinearRing(closeRing(coordinates).toArray(new Coordinate[0])));
                    }
                }
            }

            if (shell != null) {
                polygonList.add(geometryFactory.createPolygon(shell, holes.toArray(new LinearRing[0])));
            }
        }

        return geometryFactory.createMultiPolygon(polygonList.toArray(new Polygon[0]));
    }

    private static List<Coordinate> polylineCoordinates(NodeInfo parent) {
        List<Coordinate> coordinates = new ArrayList<>();
        AxisIterator polylines = parent.iterateAxis(AxisInfo.CHILD);
        for (Item polylineItem = polylines.next(); polylineItem != null; polylineItem = polylines.next()) {
            if (!(polylineItem instanceof NodeInfo polylineNode) || !"polyline".equals(polylineNode.getLocalPart())) {
                continue;
            }

            AxisIterator nodes = polylineNode.iterateAxis(AxisInfo.CHILD);
            for (Item coordinateItem = nodes.next(); coordinateItem != null; coordinateItem = nodes.next()) {
                if (!(coordinateItem instanceof NodeInfo coordinateNode) || !"coord".equals(coordinateNode.getLocalPart())) {
                    continue;
                }

                Double c1 = numericChildValue(coordinateNode, "c1");
                Double c2 = numericChildValue(coordinateNode, "c2");
                if (c1 != null && c2 != null) {
                    coordinates.add(new Coordinate(c1, c2));
                }
            }
        }
        return coordinates;
    }

    private static List<Coordinate> closeRing(List<Coordinate> coordinates) {
        if (coordinates.isEmpty()) {
            return coordinates;
        }

        List<Coordinate> closedCoordinates = new ArrayList<>(coordinates);
        Coordinate first = closedCoordinates.get(0);
        Coordinate last = closedCoordinates.get(closedCoordinates.size() - 1);
        if (!first.equals2D(last)) {
            closedCoordinates.add(new Coordinate(first.x, first.y));
        }
        return closedCoordinates;
    }

    private static Double numericChildValue(NodeInfo parent, String localName) {
        String value = childValue(parent, localName);
        if (value.isBlank()) {
            return null;
        }

        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String childValue(NodeInfo parent, String localName) {
        AxisIterator children = parent.iterateAxis(AxisInfo.CHILD);
        for (Item item = children.next(); item != null; item = children.next()) {
            if (item instanceof NodeInfo nodeInfo && localName.equals(nodeInfo.getLocalPart())) {
                return normalize(nodeInfo.getStringValue());
            }
        }
        return "";
    }

    private static String descendantValue(NodeInfo parent, String localName) {
        AxisIterator iterator = parent.iterateAxis(AxisInfo.DESCENDANT);
        for (Item item = iterator.next(); item != null; item = iterator.next()) {
            if (item instanceof NodeInfo nodeInfo && localName.equals(nodeInfo.getLocalPart())) {
                return normalize(nodeInfo.getStringValue());
            }
        }
        return "";
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
