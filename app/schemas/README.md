# Room schemas

Room schema JSON files are generated here by the configured KSP `room.schemaLocation`.

For every database version that ships:

1. Run a successful local build.
2. Review the generated schema JSON.
3. Commit the schema JSON with the release.
4. Use the committed schemas when adding migration tests for future database versions.

Do not hand-edit generated Room schema JSON.
