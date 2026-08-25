package se.spacify.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.spacify.app.data.controller.DataController;
import se.spacify.app.data.model.DataField;
import se.spacify.app.data.model.DataRelation;
import se.spacify.app.data.model.DataRow;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.spider.Request;
import se.spacify.app.spider.Spider;
import se.spacify.db.DatabaseManager;

/**
 * End-to-end checks that the dynamic {@link DataController} template actually renders
 * for every mode (index/list/detail/related/not-found) without a Lua runtime error —
 * the risk the rest of the suite can't catch, since a bad {@code model.*} index inside
 * a Lua loop only fails at render time, not at {@code javac} time — and that a full
 * create/edit/delete/related-lookup flow behaves as the app's spec describes.
 */
public class DataAppTest {

    private static DataRepository repo;
    private static Spider spider;

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        DatabaseManager.getInstance().init();
        repo = new DataRepository();
        spider = new Spider();
        spider.getControllers().add(new DataController(repo));
    }

    private static Element get(String uri) {
        Element el = spider.process(new Request("GET", uri, new HashMap<>(), null, new HashMap<>()));
        assertNotNull("expected a rendered tree for " + uri, el);
        return el;
    }

    private static Element post(String uri, String action, Map<String, Object> data) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("action", action);
        Element el = spider.process(new Request("POST", uri, headers, action, data));
        assertNotNull("expected a rendered tree for " + uri + " action=" + action, el);
        return el;
    }

    @Test
    public void indexRendersAndCreatesAndDeletesTables() throws Exception {
        get("spacify:table");

        String name = "Test Table " + UUID.randomUUID();
        Map<String, Object> posted = new HashMap<>();
        posted.put("new_table_name", name);
        post("spacify:table", "createtable", posted);

        DataTable table = repo.findTable(findSlugFor(name));
        assertNotNull(table);

        post("spacify:table", "deletetable:" + table.getSlug(), Map.of());
        assertEquals(null, repo.findTable(table.getSlug()));
    }

    @Test
    public void rowLifecycleAndFieldTypeFormatting() throws Exception {
        DataTable table = repo.createTable("Widgets " + UUID.randomUUID());
        try {
            repo.addField(table, "Title", "text");
            repo.addField(table, "Count", "number");
            repo.addField(table, "Price", "float");
            repo.addField(table, "Released", "timestamp");

            get("spacify:table:" + table.getSlug());

            Map<String, Object> posted = new HashMap<>();
            posted.put("f_title", "First & <Widget>");
            posted.put("f_count", "1234567");
            posted.put("f_price", "19.5");
            posted.put("f_released", "2020-01-01");
            post("spacify:table:" + table.getSlug(), "createrow", posted);

            java.util.List<DataRow> rows = repo.listRows(table);
            assertEquals(1, rows.size());
            DataRow row = rows.get(0);

            Element listView = get("spacify:table:" + table.getSlug());
            String rendered = textOf(listView);
            assertTrue("expected XML-escaped free text, got: " + rendered, rendered.contains("First & <Widget>"));
            assertTrue("expected grouped number", rendered.contains("1,234,567"));
            assertTrue("expected grouped decimal", rendered.contains("19.50"));

            String rowUri = "spacify:table:" + table.getSlug() + ":" + row.getId();
            get(rowUri);

            Map<String, Object> edit = new HashMap<>();
            edit.put("f_title", "Renamed");
            edit.put("f_count", "42");
            edit.put("f_price", "1.00");
            edit.put("f_released", "2020-01-01");
            post(rowUri, "save", edit);
            assertEquals("Renamed", repo.valuesFor(row.getId(), fieldBySlug(table, "title").getId()).get(0).getValue());

            post(rowUri, "deleterow", Map.of());
            assertEquals(null, repo.findRow(table, row.getId()));
        } finally {
            repo.deleteTable(table);
        }
    }

    @Test
    public void rowsGetStandardFieldsOnCreation() throws Exception {
        DataTable table = repo.createTable("Gadgets " + UUID.randomUUID());
        try {
            repo.addField(table, "Title", "text");

            Map<String, Object> first = new HashMap<>();
            first.put("name", "First row");
            first.put("f_title", "One");
            DataRow row1 = repo.createRow(table, repo.listFields(table), first);

            assertEquals("First row", row1.getName());
            assertNotNull(row1.getSlug());
            assertTrue("expected a non-blank auto slug", !row1.getSlug().isBlank());
            assertEquals(1L, row1.getNumber());
            assertEquals(1L, row1.getIdNo());

            Map<String, Object> second = new HashMap<>();
            second.put("f_title", "Two");
            DataRow row2 = repo.createRow(table, repo.listFields(table), second);

            assertEquals(null, row2.getName());
            assertEquals(2L, row2.getNumber());
            assertEquals(2L, row2.getIdNo());

            Map<String, Object> renamed = new HashMap<>();
            renamed.put("name", "Renamed row");
            renamed.put("f_title", "One");
            repo.updateRow(table, row1, repo.listFields(table), renamed);
            assertEquals("Renamed row", row1.getName());
        } finally {
            repo.deleteTable(table);
        }
    }

    @Test
    public void belongsToRelationRendersAsRowDetailTab() throws Exception {
        DataTable artists = repo.createTable("Artists " + UUID.randomUUID());
        DataTable albums = repo.createTable("Albums " + UUID.randomUUID());
        try {
            repo.addField(artists, "Name", "text");
            repo.addField(albums, "Title", "text");
            DataRelation relation = repo.defineBelongsTo(albums, "Artist", artists, false, null);
            DataField artistLink = repo.findField(relation.getFieldId());
            assertNotNull(artistLink);

            Map<String, Object> artistPosted = new HashMap<>();
            artistPosted.put("f_name", "Test Artist");
            DataRow artistRow = repo.createRow(artists, repo.listFields(artists), artistPosted);
            String artistUri = repo.rowUri(artists.getSlug(), artistRow.getId());

            Map<String, Object> albumPosted = new HashMap<>();
            albumPosted.put("f_title", "Test Album");
            albumPosted.put("f_" + artistLink.getSlug(), artistUri);
            repo.createRow(albums, repo.listFields(albums), albumPosted);

            // Same single-page render now shows the related album inline as a tab, no
            // separate ":<related slug>" screen/fetch needed.
            Element detail = get(artistUri);
            assertTrue("expected the related album's title on the artist's own detail page",
                textOf(detail).contains("Test Album"));

            List<DataRepository.RelationTab> tabs = repo.relationTabsFor(artists);
            assertEquals(1, tabs.size());
            assertEquals(albums.getSlug(), tabs.get(0).listedTable().getSlug());
        } finally {
            repo.deleteTable(albums);
            repo.deleteTable(artists);
        }
    }

    @Test
    public void manyToManyRelationListsJunctionRowsWithSpecialFields() throws Exception {
        DataTable artists = repo.createTable("Artists " + UUID.randomUUID());
        DataTable venues = repo.createTable("Venues " + UUID.randomUUID());
        DataRelation relation = repo.defineManyToMany(artists, venues, "Bookings " + UUID.randomUUID());
        DataTable junction = repo.findTableById(relation.getJunctionTableId());
        try {
            repo.addField(artists, "Name", "text");
            repo.addField(venues, "Name", "text");
            // A "special field" attached to the junction, beyond its two FK fields.
            repo.addField(junction, "Fee", "number");

            DataRow artistRow = repo.createRow(artists, repo.listFields(artists),
                Map.of("f_name", "Test Artist"));
            DataRow venueRow = repo.createRow(venues, repo.listFields(venues),
                Map.of("f_name", "Test Venue"));

            DataField junctionArtistField = repo.findField(relation.getJunctionSourceFieldId());
            DataField junctionVenueField = repo.findField(relation.getJunctionTargetFieldId());
            Map<String, Object> junctionPosted = new HashMap<>();
            junctionPosted.put("f_" + junctionArtistField.getSlug(), repo.rowUri(artists.getSlug(), artistRow.getId()));
            junctionPosted.put("f_" + junctionVenueField.getSlug(), repo.rowUri(venues.getSlug(), venueRow.getId()));
            junctionPosted.put("f_fee", "500");
            repo.createRow(junction, repo.listFields(junction), junctionPosted);

            // Both sides of the many-to-many see a tab listing the junction row, including
            // its special field.
            Element artistDetail = get(repo.rowUri(artists.getSlug(), artistRow.getId()));
            assertTrue("expected the junction row's special field on the artist's page",
                textOf(artistDetail).contains("500"));

            Element venueDetail = get(repo.rowUri(venues.getSlug(), venueRow.getId()));
            assertTrue("expected the junction row's special field on the venue's page",
                textOf(venueDetail).contains("500"));
        } finally {
            repo.deleteTable(junction);
            repo.deleteTable(venues);
            repo.deleteTable(artists);
        }
    }

    @Test
    public void migrateLegacyLinkRelationsBackfillsOldConventionFields() throws Exception {
        DataTable artists = repo.createTable("Artists " + UUID.randomUUID());
        DataTable albums = repo.createTable("Albums " + UUID.randomUUID());
        try {
            // The old convention: a LINK field slugged "<targetSlug>_uri", created via the
            // plain addField (no relation metadata) — as any pre-existing field would be.
            DataField legacyLink = repo.addField(albums, artists.getSlug() + " uri", "link");
            assertEquals(artists.getSlug() + "_uri", legacyLink.getSlug());
            assertTrue("no relation should exist yet", repo.relationTabsFor(artists).isEmpty());

            repo.migrateLegacyLinkRelations();
            List<DataRepository.RelationTab> tabs = repo.relationTabsFor(artists);
            assertEquals(1, tabs.size());
            assertEquals(albums.getSlug(), tabs.get(0).listedTable().getSlug());
            assertEquals(legacyLink.getId(), tabs.get(0).pointerField().getId());

            // Idempotent: running it again doesn't create a second relation for the same field.
            repo.migrateLegacyLinkRelations();
            assertEquals(1, repo.relationTabsFor(artists).size());
        } finally {
            repo.deleteTable(albums);
            repo.deleteTable(artists);
        }
    }

    @Test
    public void migrateLegacyLinkRelationsDoesNotDuplicateManyToManyJunctionFields() throws Exception {
        // A many-to-many junction's own FK fields are ordinary "<targetSlug>_uri"-looking LINK
        // fields (createLinkField names them after the table they point at), so the migration
        // backfill must recognize them as already-managed and not also treat them as legacy
        // convention fields — otherwise every activation would grow a second, spurious
        // BELONGS_TO relation (and a duplicate tab) alongside the real MANY_TO_MANY one.
        DataTable artists = repo.createTable("Artists " + UUID.randomUUID());
        DataTable venues = repo.createTable("Venues " + UUID.randomUUID());
        DataRelation relation = repo.defineManyToMany(artists, venues, "Bookings " + UUID.randomUUID());
        DataTable junction = repo.findTableById(relation.getJunctionTableId());
        try {
            assertEquals(1, repo.relationTabsFor(artists).size());
            assertEquals(1, repo.relationTabsFor(venues).size());

            repo.migrateLegacyLinkRelations();

            assertEquals("expected no spurious extra relation/tab on the artist side",
                1, repo.relationTabsFor(artists).size());
            assertEquals("expected no spurious extra relation/tab on the venue side",
                1, repo.relationTabsFor(venues).size());
        } finally {
            repo.deleteTable(junction);
            repo.deleteTable(venues);
            repo.deleteTable(artists);
        }
    }

    @Test
    public void searchRowsFiltersByNameAndBelongsTo() throws Exception {
        DataTable artists = repo.createTable("Artists " + UUID.randomUUID());
        DataTable albums = repo.createTable("Albums " + UUID.randomUUID());
        try {
            repo.addField(artists, "Name", "text");
            repo.addField(albums, "Title", "text");
            DataRelation relation = repo.defineBelongsTo(albums, "Artist", artists, false, null);
            DataField linkField = repo.findField(relation.getFieldId());

            DataRow artistOne = repo.createRow(artists, repo.listFields(artists), Map.of("f_name", "Artist One"));
            DataRow artistTwo = repo.createRow(artists, repo.listFields(artists), Map.of("f_name", "Artist Two"));
            String oneUri = repo.rowUri(artists.getSlug(), artistOne.getId());
            String twoUri = repo.rowUri(artists.getSlug(), artistTwo.getId());

            repo.createRow(albums, repo.listFields(albums),
                Map.of("name", "Sunrise", "f_title", "Sunrise", "f_" + linkField.getSlug(), oneUri));
            repo.createRow(albums, repo.listFields(albums),
                Map.of("name", "Sunset", "f_title", "Sunset", "f_" + linkField.getSlug(), twoUri));

            assertEquals(2, repo.searchRows(albums, DataRepository.RowFilter.NONE).size());

            List<DataRow> byName = repo.searchRows(albums, new DataRepository.RowFilter("sun", List.of()));
            assertEquals(2, byName.size());

            List<DataRow> byTitle = repo.searchRows(albums, new DataRepository.RowFilter("rise", List.of()));
            assertEquals(1, byTitle.size());

            List<DataRow> byArtist = repo.searchRows(albums, new DataRepository.RowFilter(null,
                List.of(new DataRepository.RowFilter.FieldEquals(linkField, oneUri))));
            assertEquals(1, byArtist.size());
        } finally {
            repo.deleteTable(albums);
            repo.deleteTable(artists);
        }
    }

    @Test
    public void unknownTableAndRowRenderNotFoundInsteadOfCrashing() {
        get("spacify:table:does-not-exist");
        get("spacify:table:does-not-exist:also-missing");
        get("spacify:table:does-not-exist:also-missing:nor-this");
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private static String findSlugFor(String name) throws Exception {
        for (DataTable t : repo.listTables()) {
            if (t.getName().equals(name)) {
                return t.getSlug();
            }
        }
        throw new AssertionError("table not found: " + name);
    }

    private static DataField fieldBySlug(DataTable table, String slug) throws Exception {
        for (DataField f : repo.listFields(table)) {
            if (f.getSlug().equals(slug)) {
                return f;
            }
        }
        throw new AssertionError("field not found: " + slug);
    }

    private static String textOf(Element el) {
        StringBuilder sb = new StringBuilder();
        collectText(el, sb);
        return sb.toString();
    }

    private static void collectText(Node node, StringBuilder sb) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            sb.append(node.getNodeValue()).append(' ');
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            collectText(children.item(i), sb);
        }
    }
}
