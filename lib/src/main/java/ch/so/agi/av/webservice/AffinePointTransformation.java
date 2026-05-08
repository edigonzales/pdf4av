package ch.so.agi.av.webservice;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.locationtech.jts.awt.PointTransformation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * Transformiert Weltkoordinaten in Pixelkoordinaten fuer die JTS-Shape-Ausgabe.
 */
final class AffinePointTransformation implements PointTransformation {
    private final AffineTransform worldToPixel;

    AffinePointTransformation(Envelope pixelEnvelope, Envelope worldEnvelope) {
        int imageWidthPx = (int) Math.round(pixelEnvelope.getWidth());
        int imageHeightPx = (int) Math.round(pixelEnvelope.getHeight());

        AffineTransform translate = AffineTransform.getTranslateInstance(-worldEnvelope.getMinX(), -worldEnvelope.getMinY());
        AffineTransform scale = AffineTransform.getScaleInstance(
                imageWidthPx / worldEnvelope.getWidth(),
                imageHeightPx / worldEnvelope.getHeight()
        );
        AffineTransform mirrorY = new AffineTransform(1, 0, 0, -1, 0, imageHeightPx);

        worldToPixel = new AffineTransform(mirrorY);
        worldToPixel.concatenate(scale);
        worldToPixel.concatenate(translate);
    }

    @Override
    public void transform(Coordinate src, Point2D dest) {
        Point2D transformed = worldToPixel.transform(new Point2D.Double(src.getX(), src.getY()), null);
        dest.setLocation(transformed);
    }
}
