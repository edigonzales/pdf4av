# Datenimport mit JBang

Skript: `dev/import-data.java`

## Voraussetzungen
- Docker-DB starten: `docker compose -f dev/docker-compose.yml up -d`
- JBang installiert

## Befehle
- Liste der importierbaren Datasets:
  - `jbang dev/import-data.java list`
- Schema importieren:
  - `jbang dev/import-data.java schema`
- Einzelne Datasets importieren/ersetzen:
  - `jbang dev/import-data.java import dmav`
  - `jbang dev/import-data.java import gebaddr,texte,metadaten`
- Alles (Schema + alle Datasets):
  - `jbang dev/import-data.java all`

## DB-Defaults (aus Docker-Setup)
- host: `localhost`
- port: `54321`
- db: `edit`
- schema: `stage`
- user/password: `ddluser` / `ddluser`

Alle Werte sind überschreibbar, z.B.:

`jbang dev/import-data.java all --host=localhost --port=54321 --db=edit --schema=stage --user=ddluser --password=ddluser`

## Hinweise
- Der Import nutzt Dataset-Replace (`FC_REPLACE`) und deaktivierte Validierung.
- `gebaddr` wird bei jedem Lauf frisch von der Bundes-URL geladen.
- Laufzeitdateien/Logs liegen unter `dev/tmp/`.
