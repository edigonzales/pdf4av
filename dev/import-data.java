//JAVA 21
//REPOS jars.interlis.ch=https://jars.interlis.ch
//REPOS central=https://repo1.maven.org/maven2
//DEPS ch.interlis:ili2pg:5.5.1
//DEPS org.postgresql:postgresql:42.7.4

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgMain;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class import_data {
    private static final String BUILDING_ADDRESS_URL =
        "https://data.geo.admin.ch/ch.swisstopo.amtliches-gebaeudeadressverzeichnis/amtliches-gebaeudeadressverzeichnis_ch/amtliches-gebaeudeadressverzeichnis_ch_2056.xtf.zip";

    private static final String SCHEMA1_MODELS = "OfficialIndexOfAddresses_V2_2";
    private static final String SCHEMA2_MODELS =
        "AV_WebService_V1_0;DMAV_Grundstuecke_V1_1;DMAV_HoheitsgrenzenAV_V1_0;DMAV_Nomenklatur_V1_1;DMAV_Bodenbedeckung_V1_1;DMAV_Einzelobjekte_V1_1;DMAVSUP_UntereinheitGrundbuch_V1_1;DMAV_DauerndeBodenverschiebungen_V1_1;DMAV_Gebaeudeadressen_V1_1;DMAV_Rohrleitungen_V1_1;DMAV_FixpunkteAVKategorie3_V1_1;DMAV_Dienstbarkeitsgrenzen_V1_1;DMAV_Toleranzstufen_V1_1";

    record DbConfig(String host, int port, String db, String schema, String user, String password) {
        String jdbcUrl() {
            return "jdbc:postgresql://" + host + ":" + port + "/" + db;
        }
    }

    record ImportItem(String key, String datasetName, String description) {}

    public static void main(String[] args) throws Exception {
        Path devDir = Paths.get("dev").toAbsolutePath().normalize();
        Path iliDir = devDir.resolve("ili");
        Path dataDir = devDir.resolve("data");
        Path tmpDir = devDir.resolve("tmp");
        Path logsDir = tmpDir.resolve("logs");

        Files.createDirectories(tmpDir);
        Files.createDirectories(logsDir);

        DbConfig db = parseDbConfig(args);
        List<String> positional = positionalArgs(args);

        if (positional.isEmpty()) {
            printUsage();
            return;
        }

        String command = positional.get(0);

        Map<String, ImportItem> items = new LinkedHashMap<>();
        items.put("dmav", new ImportItem("dmav", "DMAVTYM_Alles_V1_1", "DMAV-Daten aus dev/Testdaten_DMAV_V1_1.zip"));
        items.put("gebaddr", new ImportItem("gebaddr", "amtliches-gebaeudeadressverzeichnis_ch_2056", "Gebaeudeadressen aus Bundes-URL (live download)"));
        items.put("texte", new ImportItem("texte", "AV_WebService_V1_0_Texte", "AV_WebService_V1_0_Texte.xml"));
        items.put("metadaten", new ImportItem("metadaten", "AV_WebService_V1_0_MetadatenAV", "AV_WebService_V1_0_MetadatenAV.xml"));
        items.put("amt", new ImportItem("amt", "AV_WebService_V1_0_Amt", "AV_WebService_V1_0_Amt.xml"));
        items.put("zustaendige-stelle", new ImportItem("zustaendige-stelle", "AV_WebService_V1_0_ZustaendigeStelle", "AV_WebService_V1_0_ZustaendigeStelle.xml"));
        items.put("information", new ImportItem("information", "AV_WebService_V1_0_Information", "AV_WebService_V1_0_Information.xml"));
        items.put("logo-ch-pi", new ImportItem("logo-ch-pi", "AV_WebService_V1_0_Logo-ch.pi", "AV_WebService_V1_0_Logo-ch.pi.xml"));
        items.put("logo-ch", new ImportItem("logo-ch", "AV_WebService_V1_0_Logo-ch", "AV_WebService_V1_0_Logo-ch.xml"));
        items.put("logo-ch-so", new ImportItem("logo-ch-so", "AV_WebService_V1_0_Logo-ch.SO", "AV_WebService_V1_0_Logo-ch.SO.xml"));
        items.put("logo-ch-449", new ImportItem("logo-ch-449", "AV_WebService_V1_0_Logo-ch.449", "AV_WebService_V1_0_Logo-ch.449.xml"));
        // items.put("logo-ch-2498", new ImportItem("logo-ch-2498", "AV_WebService_V1_0_Logo-ch.2498", "AV_WebService_V1_0_Logo-ch.2498.xml"));
        // items.put("logo-ch-2500", new ImportItem("logo-ch-2500", "AV_WebService_V1_0_Logo-ch.2500", "AV_WebService_V1_0_Logo-ch.2500.xml"));
        // items.put("logo-ch-2502", new ImportItem("logo-ch-2502", "AV_WebService_V1_0_Logo-ch.2502", "AV_WebService_V1_0_Logo-ch.2502.xml"));

        switch (command) {
            case "list" -> printList(items);
            case "schema" -> runSchemaImports(db, iliDir, logsDir);
            case "all" -> {
                runSchemaImports(db, iliDir, logsDir);
                importKeys(db, iliDir, dataDir, tmpDir, logsDir, items, new ArrayList<>(items.keySet()));
            }
            case "import" -> {
                if (positional.size() < 2) {
                    throw new IllegalArgumentException("Missing dataset list. Example: import dmav,gebaddr");
                }
                List<String> keys = parseKeys(positional.get(1));
                importKeys(db, iliDir, dataDir, tmpDir, logsDir, items, keys);
            }
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: jbang dev/import-data.java <command> [args] [--host=...] [--port=...] [--db=...] [--schema=...] [--user=...] [--password=...]");
        System.out.println("Commands:");
        System.out.println("  list");
        System.out.println("  schema");
        System.out.println("  all");
        System.out.println("  import <dataset1,dataset2,...>");
    }

    private static DbConfig parseDbConfig(String[] args) {
        Map<String, String> opts = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
                int idx = arg.indexOf('=');
                opts.put(arg.substring(2, idx), arg.substring(idx + 1));
            }
        }
        String host = opts.getOrDefault("host", "localhost");
        int port = Integer.parseInt(opts.getOrDefault("port", "54321"));
        String db = opts.getOrDefault("db", "edit");
        String schema = opts.getOrDefault("schema", "stage");
        String user = opts.getOrDefault("user", "ddluser");
        String password = opts.getOrDefault("password", "ddluser");
        return new DbConfig(host, port, db, schema, user, password);
    }

    private static List<String> positionalArgs(String[] args) {
        List<String> out = new ArrayList<>();
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                out.add(arg);
            }
        }
        return out;
    }

    private static List<String> parseKeys(String csv) {
        List<String> keys = new ArrayList<>();
        for (String p : csv.split(",")) {
            String k = p.trim();
            if (!k.isEmpty()) {
                keys.add(k);
            }
        }
        return keys;
    }

    private static void printList(Map<String, ImportItem> items) {
        System.out.println("Importierbare Datasets:");
        for (ImportItem item : items.values()) {
            System.out.println("- " + item.key() + " => dataset='" + item.datasetName() + "' | " + item.description());
        }
    }

    private static void runSchemaImports(DbConfig db, Path iliDir, Path logsDir) throws Exception {
        try (Connection connection = DriverManager.getConnection(db.jdbcUrl(), db.user(), db.password())) {
            connection.setAutoCommit(false);

            Config config1 = new Config();
            new PgMain().initConfig(config1);
            config1.setJdbcConnection(connection);
            config1.setDbschema(db.schema());
            config1.setLogfile(logsDir.resolve("ili23-schema-import.log").toString());
            config1.setFunction(Config.FC_SCHEMAIMPORT);
            Config.setStrokeArcs(config1, Config.STROKE_ARCS_ENABLE);
            config1.setCreateFk(Config.CREATE_FK_YES);
            config1.setCreateFkIdx(Config.CREATE_FKIDX_YES);
            config1.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);
            config1.setTidHandling(Config.TID_HANDLING_PROPERTY);
            config1.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
            config1.setCreateTypeDiscriminator(Config.CREATE_TYPE_DISCRIMINATOR_ALWAYS);
            config1.setCreateImportTabs(true);
            config1.setCreateMetaInfo(true);
            config1.setNameOptimization(Config.NAME_OPTIMIZATION_DISABLE);
            config1.setDefaultSrsAuthority("EPSG");
            config1.setDefaultSrsCode("2056");
            config1.setModels(SCHEMA1_MODELS);
            config1.setModeldir(modelDir(iliDir));
            Ili2db.readSettingsFromDb(config1);
            Ili2db.run(config1, null);
            connection.commit();

            Config config2 = new Config();
            new PgMain().initConfig(config2);
            config2.setJdbcConnection(connection);
            config2.setDbschema(db.schema());
            config2.setLogfile(logsDir.resolve("ili24-schema-import.log").toString());
            config2.setFunction(Config.FC_SCHEMAIMPORT);
            config2.setModels(SCHEMA2_MODELS);
            config2.setModeldir(modelDir(iliDir));
            Ili2db.readSettingsFromDb(config2);
            Ili2db.run(config2, null);
            connection.commit();
        }
    }

    private static void importKeys(
        DbConfig db,
        Path iliDir,
        Path dataDir,
        Path tmpDir,
        Path logsDir,
        Map<String, ImportItem> items,
        List<String> keys
    ) throws Exception {
        for (String key : keys) {
            ImportItem item = items.get(key);
            if (item == null) {
                throw new IllegalArgumentException("Unknown dataset key: " + key + " (use 'list')");
            }
            Path file = resolveDataFile(key, dataDir, tmpDir);
            importFile(db, iliDir, logsDir, file, item.datasetName());
        }
    }

    private static Path resolveDataFile(String key, Path dataDir, Path tmpDir) throws Exception {
        return switch (key) {
            case "dmav" -> extractFileFromZip(
                dataDir.getParent().resolve("Testdaten_DMAV_V1_1.zip"),
                "DMAVTYM_Alles_V1_1.xtf",
                tmpDir.resolve("DMAVTYM_Alles_V1_1.xtf")
            );
            case "gebaddr" -> downloadAndExtractSingleXtf(tmpDir.resolve("gebaddr-2056.zip"), tmpDir.resolve("gebaddr-2056.xtf"));
            case "texte" -> dataDir.resolve("AV_WebService_V1_0_Texte.xml");
            case "metadaten" -> dataDir.resolve("AV_WebService_V1_0_MetadatenAV.xml");
            case "amt" -> dataDir.resolve("AV_WebService_V1_0_Amt.xml");
            case "zustaendige-stelle" -> dataDir.resolve("AV_WebService_V1_0_ZustaendigeStelle.xml");
            case "information" -> dataDir.resolve("AV_WebService_V1_0_Information.xml");
            case "logo-ch-pi" -> dataDir.resolve("AV_WebService_V1_0_Logo-ch.pi.xml");
            case "logo-ch" -> dataDir.resolve("AV_WebService_V1_0_Logo-ch.xml");
            case "logo-ch-so" -> dataDir.resolve("AV_WebService_V1_0_Logo-ch.SO.xml");
            case "logo-ch-449" -> dataDir.resolve("AV_WebService_V1_0_Logo-ch.449.xml");
            // case "logo-ch-2498" -> dataDir.resolve("AV_WebService_V1_0_Logo-ch.2498.xml");
            // case "logo-ch-2500" -> dataDir.resolve("AV_WebService_V1_0_Logo-ch.2500.xml");
            // case "logo-ch-2502" -> dataDir.resolve("AV_WebService_V1_0_Logo-ch.2502.xml");
            default -> throw new IllegalArgumentException("Unhandled dataset key: " + key);
        };
    }

    private static Path extractFileFromZip(Path zipPath, String entryName, Path outputFile) throws IOException {
        if (!Files.exists(zipPath)) {
            throw new IllegalStateException("ZIP not found: " + zipPath);
        }
        try (InputStream in = Files.newInputStream(zipPath);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entryName.equals(entry.getName())) {
                    Files.copy(zis, outputFile, StandardCopyOption.REPLACE_EXISTING);
                    return outputFile;
                }
            }
        }
        throw new IllegalStateException("Entry not found in ZIP: " + entryName + " @ " + zipPath);
    }

    private static Path downloadAndExtractSingleXtf(Path zipTarget, Path xtfTarget) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(BUILDING_ADDRESS_URL)).GET().timeout(Duration.ofMinutes(5)).build();
        HttpResponse<Path> resp = client.send(req, HttpResponse.BodyHandlers.ofFile(zipTarget));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IllegalStateException("Download failed with HTTP " + resp.statusCode());
        }

        try (InputStream in = Files.newInputStream(zipTarget);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase(Locale.ROOT).endsWith(".xtf")) {
                    Files.copy(zis, xtfTarget, StandardCopyOption.REPLACE_EXISTING);
                    return xtfTarget;
                }
            }
        }
        throw new IllegalStateException("No .xtf file found in downloaded ZIP: " + zipTarget);
    }

    private static void importFile(DbConfig db, Path iliDir, Path logsDir, Path dataFile, String datasetName) throws Exception {
        if (!Files.exists(dataFile)) {
            throw new IllegalStateException("Data file not found: " + dataFile);
        }

        //EhiLogger.getInstance().setTraceFilter(false);

        try (Connection connection = DriverManager.getConnection(db.jdbcUrl(), db.user(), db.password())) {
            connection.setAutoCommit(false);

            Config config = new Config();
            new PgMain().initConfig(config);
            config.setJdbcConnection(connection);
            config.setDbschema(db.schema());
            config.setLogfile(logsDir.resolve(dataFile.getFileName().toString() + "-import.log").toString());
            config.setXtffile(dataFile.toString());
            if (Ili2db.isItfFilename(dataFile.toString())) {
                config.setItfTransferfile(true);
            }

            config.setBatchSize(Integer.parseInt("5000"));
            config.setFunction(Config.FC_REPLACE);
            config.setDatasetName(datasetName);
            config.setImportTid(true);
            config.setModeldir(modelDir(iliDir));
            config.setValidation(false);
            Ili2db.readSettingsFromDb(config);

            Ili2db.run(config, null);
            connection.commit();
        }
    }

    private static String modelDir(Path iliDir) {
        return Ili2db.ILI_FROM_DB
            + ch.interlis.ili2c.Main.ILIDIR_SEPARATOR + iliDir.toString()
            + ch.interlis.ili2c.Main.ILIDIR_SEPARATOR + ch.interlis.ili2c.Main.ILI_REPOSITORY;
    }
}
