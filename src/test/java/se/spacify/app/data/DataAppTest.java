package se.spacify.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.spacify.app.data.controller.DataController;
import se.spacify.app.data.model.DataField;
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
    }

    @Test
    public void relatedRowsResolveByUriSuffixField() throws Exception {
        DataTable artists = repo.createTable("Artists " + UUID.randomUUID());
        repo.addField(artists, "Name", "text");

        DataTable albums = repo.createTable("Albums " + UUID.randomUUID());
        repo.addField(albums, "Title", "text");
        // A LINK field must be named "<pointed-to-table-slug>_uri" to be discovered
        // as a pointer back at that table (see DataRepository.relatedTablesFor).
        DataField artistLink = repo.addField(albums, artists.getSlug() + " uri", "link");
        assertEquals(artists.getSlug() + "_uri", artistLink.getSlug());

        Map<String, Object> artistPosted = new HashMap<>();
        artistPosted.put("f_name", "Test Artist");
        DataRow artistRow = repo.createRow(artists, repo.listFields(artists), artistPosted);
        String artistUri = repo.rowUri(artists.getSlug(), artistRow.getId());

        Map<String, Object> albumPosted = new HashMap<>();
        albumPosted.put("f_title", "Test Album");
        albumPosted.put("f_" + artistLink.getSlug(), artistUri);
        repo.createRow(albums, repo.listFields(albums), albumPosted);

        Element detail = get(artistUri);
        assertTrue("expected a link to the related albums table",
            textOf(detail).contains(albums.getName()));

        String relatedUri = artistUri + ":" + albums.getSlug();
        Element related = get(relatedUri);
        assertTrue("expected the related album row's title", textOf(related).contains("Test Album"));
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
