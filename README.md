# pdf4av

`pdf4av` konvertiert ein bewusst einfaches XML-Dokument in `PDF` oder `XSL-FO`. `XSL-FO` bleibt eine offizielle Ausgabeoption, damit sich die Transformation beim Entwickeln und Debuggen direkt inspizieren lässt.

## XML-v1

Das eingebaute Default-Stylesheet unterstützt aktuell dieses Minimalformat:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<document>
  <title>Beispieltitel</title>
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
```

Regeln:

- `title` ist Pflicht.
- `content` muss mindestens ein `paragraph` enthalten.
- `meta` ist optional.

## CLI

Pflichtoptionen:

- `--xml <path>`
- `--out <directory>`

Optionale Parameter:

- `--format pdf|fo` mit Default `pdf`
- `--xslt <path>` für ein eigenes XSLT
- `--locale <language-tag>` mit Default `de`

Beispielaufrufe:

```bash
./gradlew :app:run --args="--xml ../examples/simple-document.xml --out /tmp/pdf4av-out"
```

```bash
./gradlew :app:run --args="--xml ../examples/simple-document.xml --out /tmp/pdf4av-out --format fo"
```

```bash
./gradlew :app:run --args="--xml /tmp/input.xml --out /tmp/pdf4av-out --xslt /tmp/custom.xsl --locale fr"
```

Die CLI schreibt den Pfad des erzeugten Artefakts auf `stdout`. Fehler werden auf `stderr` ausgegeben.

## Java API

```java
PdfConverter converter = new DefaultPdfConverter();
ConversionResult pdf = converter.xmlToPdf(Path.of("/tmp/input.xml"), Path.of("/tmp/out"), Locale.GERMAN);
ConversionResult fo = converter.xmlToFo(Path.of("/tmp/input.xml"), Path.of("/tmp/out"), Locale.GERMAN);
```

Mit expliziter XSLT-Datei:

```java
PdfConverter converter = new DefaultPdfConverter();
ConversionResult result = converter.xmlToPdf(
        Path.of("/tmp/input.xml"),
        Path.of("/tmp/custom.xsl"),
        Path.of("/tmp/out"),
        Locale.FRENCH
);
```

`ConversionResult` liefert den erzeugten Ausgabepfad, das Format und die effektiv verwendete XSLT-Datei zurück.

## Implementierungsnotizen

- PDF-Erzeugung läuft direkt als Saxon→FOP-Pipeline ohne persistierte Zwischen-FO-Datei.
- Default-XSLT, Locale-Dateien, Fonts und `fop.xconf` werden einmalig in ein internes Laufzeitverzeichnis materialisiert.
- Für Default- und explizite XSLTs werden kompilierte Saxon-Artefakte wiederverwendet; pro Anfrage wird aber immer ein neuer `XsltTransformer` erzeugt.
