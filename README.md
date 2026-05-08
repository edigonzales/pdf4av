# pdf4av

`pdf4av` konvertiert einen AV-Extract in `PDF` oder `XSL-FO`. `XSL-FO` bleibt eine offizielle Ausgabeoption, damit sich die Transformation beim Entwickeln und Debuggen direkt inspizieren lässt.

## Default-XSLT

Das eingebaute Default-Stylesheet ist `lib/src/main/resources/xslt/xml2pdf_V1_0.xsl` und erwartet ein XML im AV-Extract-Format, zum Beispiel:

- [CH994641443597.xml](/Users/stefan/sources/pdf4av/examples/CH994641443597.xml)

Der erste Umsetzungsstand rendert bewusst nur die Titelseite.

## CLI

Pflichtoptionen:

- `--xml <path>`
- `--out <directory>`

Optionale Parameter:

- `--format pdf|fo` mit Default `pdf`
- `--xslt <path>` für ein eigenes XSLT
- `--locale <language-tag>` mit Default `de`
- `--debug-table-grid` für sichtbare Tabellenrahmen beim Layout-Debugging

Beispielaufrufe:

```bash
./gradlew :app:run --args="--xml /Users/stefan/sources/pdf4av/examples/CH994641443597.xml --out /tmp/pdf4av-out"
```

```bash
./gradlew :app:run --args="--xml /Users/stefan/sources/pdf4av/examples/CH994641443597.xml --out /tmp/pdf4av-out --format fo"
```

```bash
./gradlew :app:run --args="--xml /tmp/input.xml --out /tmp/pdf4av-out --xslt /tmp/custom.xsl --locale fr"
```

```bash
./gradlew :app:run --args="--xml /Users/stefan/sources/pdf4av/examples/CH994641443597.xml --out /tmp/pdf4av-out --format fo --debug-table-grid"
```

Mit `--debug-table-grid` rendert die AV-XSL sichtbare Tabellenrahmen für Header, Footer und die Tabellen der Titelseite. Der Schalter ist nur fürs Layout-Debugging gedacht.

Die CLI schreibt den Pfad des erzeugten Artefakts auf `stdout`. Fehler werden auf `stderr` ausgegeben.

## Java API

```java
PdfConverter converter = new DefaultPdfConverter();
ConversionResult pdf = converter.xmlToPdf(Path.of("/tmp/input.xml"), Path.of("/tmp/out"), Locale.GERMAN);
ConversionResult fo = converter.xmlToFo(Path.of("/tmp/input.xml"), Path.of("/tmp/out"), Locale.GERMAN);
ConversionResult debugFo = converter.xmlToFo(Path.of("/tmp/input.xml"), Path.of("/tmp/out"), Locale.GERMAN, true);
```

`/tmp/input.xml` muss dabei ein AV-Extract im Default-Format sein, sofern keine eigene XSLT übergeben wird.

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
