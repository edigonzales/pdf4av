package ch.so.agi.av.webservice;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

/**
 * Saxon-Extensionfunktion {@code av:normalizeImage(...)} zur Vereinheitlichung
 * eingebetteter Base64-Bilder.
 * Sie dekodiert vorhandene Bilddaten, rendert transparenten Hintergrund auf Weiss
 * und liefert ein PNG als Base64 zurueck.
 */
final class NormalizeImageFunction extends ExtensionFunctionDefinition {
    private static final StructuredQName FUNCTION_NAME = new StructuredQName(
            "av",
            "http://pdf4av.so.ch/av",
            "normalizeImage"
    );

    /**
     * Liefert den vollqualifizierten Namen der Extensionfunktion.
     *
     * @return QName der Funktion.
     */
    @Override
    public StructuredQName getFunctionQName() {
        return FUNCTION_NAME;
    }

    /**
     * Definiert die Signatur der Funktionsargumente.
     *
     * @return ein optionaler String mit Base64-Bilddaten.
     */
    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.OPTIONAL_STRING};
    }

    /**
     * Definiert den Rueckgabetyp der Funktion.
     *
     * @param suppliedArgumentTypes von Saxon bereitgestellte Argumenttypen.
     * @return genau ein String als Ergebnis.
     */
    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_STRING;
    }

    /**
     * Erzeugt den eigentlichen Laufzeitaufruf der Extensionfunktion.
     *
     * @return Aufrufobjekt fuer Saxon.
     */
    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            /**
             * Fuehrt die Bildnormalisierung fuer den XSLT-Aufruf aus.
             *
             * @param context aktueller XPath-Kontext.
             * @param arguments Funktionsargumente aus XSLT.
             * @return normalisierte Base64-Bilddaten als String.
             * @throws XPathException wenn Saxon den Funktionsaufruf nicht korrekt auswerten kann.
             */
            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                String imageData = arguments[0] == null ? "" : arguments[0].head().getStringValue();
                return new StringValue(normalizeImage(imageData));
            }
        };
    }

    /**
     * Normalisiert ein Base64-kodiertes Bild.
     * Bei gueltigen Bilddaten wird ein RGB-PNG mit weissem Hintergrund erzeugt.
     * Bei ungueltigen Daten wird der urspruengliche, getrimmte String unveraendert zurueckgegeben.
     *
     * @param imageData Base64-Bilddaten.
     * @return normalisierte Base64-PNG-Daten oder Originalwert bei Fehlern.
     */
    static String normalizeImage(String imageData) {
        String trimmed = imageData == null ? "" : imageData.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(trimmed);
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(decoded));
            if (source == null) {
                return trimmed;
            }

            BufferedImage normalized = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = normalized.createGraphics();
            try {
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, normalized.getWidth(), normalized.getHeight());
                graphics.drawImage(source, 0, 0, null);
            } finally {
                graphics.dispose();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(normalized, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IllegalArgumentException | IOException e) {
            return trimmed;
        }
    }
}
