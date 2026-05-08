package ch.so.agi.av.webservice;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

/**
 * Erzeugt einen Massstabsbalken als PNG.
 *
 * Portiert und lokal gehalten, damit keine Laufzeitabhaengigkeit auf das Referenzprojekt noetig ist.
 */
final class ScalebarGenerator {
    private enum SegmentMeasureUnitType {
        M,
        KM,
        CM,
        MM
    }

    private static final String IMAGE_FORMAT = "png";

    private int height = 40;
    private int numberOfSegments = 3;
    private int lrbMargin = 15;
    private int topMargin = 15;
    private Color colorBorderSegment = Color.DARK_GRAY;
    private Color colorSegmentEven = Color.BLACK;
    private Color colorSegmentUneven = Color.WHITE;
    private Color colorText = Color.RED;
    private Color haloColor = Color.WHITE;
    private Font textFont = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    private SegmentMeasureUnitType segmentMeasureUnit = SegmentMeasureUnitType.M;
    private boolean drawScaleText = true;

    void setColorText(Color colorText) {
        this.colorText = colorText;
    }

    void setDrawScaleText(boolean drawScaleText) {
        this.drawScaleText = drawScaleText;
    }

    void setHeight(int height) {
        this.height = height;
    }

    void setLrbMargin(int lrbMargin) {
        this.lrbMargin = lrbMargin;
    }

    void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    void setNumberOfSegments(int numberOfSegments) {
        this.numberOfSegments = numberOfSegments;
    }

    void setTextFont(Font textFont) {
        this.textFont = textFont;
    }

    byte[] getImageAsByte(Double scale, double width, double dpi) throws IOException {
        BufferedImage bufferedImage = getImage(scale, width, dpi);
        if (bufferedImage == null) {
            return new byte[0];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, IMAGE_FORMAT, outputStream);
        return outputStream.toByteArray();
    }

    private BufferedImage getImage(Double scale, double width, double dpi) {
        double extentWidth = 2.54 * scale / 100;
        double screenWidth = dpi;
        double widthInMeters = extentWidth * width / screenWidth;
        if (widthInMeters < 0.001) {
            return null;
        }

        double rounding = 1;
        segmentMeasureUnit = SegmentMeasureUnitType.M;
        if (widthInMeters > 1000) {
            rounding = 500;
            segmentMeasureUnit = SegmentMeasureUnitType.KM;
        } else if (widthInMeters > 100) {
            rounding = 50;
        } else if (widthInMeters > 10) {
            rounding = 5;
        } else if (widthInMeters < 0.005) {
            rounding = 0.0005;
            segmentMeasureUnit = SegmentMeasureUnitType.MM;
        } else if (widthInMeters < 0.05) {
            rounding = 0.005;
            segmentMeasureUnit = SegmentMeasureUnitType.MM;
        } else if (widthInMeters < 0.5) {
            rounding = 0.05;
            segmentMeasureUnit = SegmentMeasureUnitType.CM;
        } else if (widthInMeters < 1) {
            rounding = 0.5;
            segmentMeasureUnit = SegmentMeasureUnitType.CM;
        }

        double segmentInMeters = Math.ceil((widthInMeters / numberOfSegments) / rounding) * rounding;
        double finalWidthInMeters = segmentInMeters * numberOfSegments;
        int scalebarDrawingWidth = (int) Math.round(finalWidthInMeters * screenWidth / extentWidth);
        int segmentDrawingWidth = scalebarDrawingWidth / numberOfSegments;
        int finalScalebarWidth = scalebarDrawingWidth + (lrbMargin * 2);

        BufferedImage image = new BufferedImage(finalScalebarWidth, height, BufferedImage.TYPE_INT_ARGB);
        int segmentHeight = height - (lrbMargin + topMargin);

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(colorBorderSegment);
        graphics.fillRect(lrbMargin - 1, topMargin - 1, (numberOfSegments * segmentDrawingWidth) + 2, segmentHeight + 2);

        for (int segmentIndex = 0; segmentIndex < numberOfSegments; segmentIndex++) {
            int x = lrbMargin + segmentIndex * segmentDrawingWidth;
            int y = topMargin;
            graphics.setColor(segmentIndex % 2 == 0 ? colorSegmentEven : colorSegmentUneven);
            graphics.fillRect(x, y, segmentDrawingWidth, segmentHeight);
            drawSegmentText(graphics, segmentIndex * segmentInMeters, lrbMargin + segmentIndex * segmentDrawingWidth, height);
        }

        drawSegmentText(graphics, numberOfSegments * segmentInMeters, lrbMargin + numberOfSegments * segmentDrawingWidth, height);

        if (scale != null && drawScaleText) {
            DecimalFormat decimalFormat = scale < 1 ? new DecimalFormat("0.##") : new DecimalFormat("#,###,###");
            decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
            drawText(graphics, "1: " + decimalFormat.format(scale), finalScalebarWidth / 2, topMargin - 2);
        }

        graphics.dispose();
        return image;
    }

    private void drawText(Graphics2D graphics, String text, int x, int y) {
        graphics.setFont(textFont);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(text);
        int centeredX = x - textWidth / 2;

        graphics.setColor(haloColor);
        graphics.drawString(text, centeredX + 2, y);
        graphics.drawString(text, centeredX - 2, y);
        graphics.drawString(text, centeredX, y + 2);
        graphics.drawString(text, centeredX, y - 2);

        graphics.setColor(colorText);
        graphics.drawString(text, centeredX, y);
    }

    private void drawSegmentText(Graphics2D graphics, double segmentMeasureInMeters, int x, int y) {
        String measureText;
        switch (segmentMeasureUnit) {
            case KM -> {
                DecimalFormat decimalFormat = (segmentMeasureInMeters / 1000) < 10 ? new DecimalFormat("#.#") : new DecimalFormat("#");
                measureText = decimalFormat.format(segmentMeasureInMeters / 1000);
            }
            case CM -> measureText = new DecimalFormat("#").format(segmentMeasureInMeters * 100);
            case MM -> {
                DecimalFormat decimalFormat = (segmentMeasureInMeters * 1000) < 1 ? new DecimalFormat("#.#") : new DecimalFormat("#");
                measureText = decimalFormat.format(segmentMeasureInMeters * 1000);
            }
            default -> measureText = new DecimalFormat("#").format(segmentMeasureInMeters);
        }

        drawText(graphics, measureText + " " + segmentMeasureUnit.name().toLowerCase(), x, y);
    }
}
