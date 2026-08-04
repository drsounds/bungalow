package se.spacify.app.data.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import se.spacify.app.data.DataRepository;
import se.spacify.app.data.Format;
import se.spacify.app.data.model.DataField;
import se.spacify.app.data.model.DataRow;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.data.model.DataValue;
import se.spacify.app.data.model.FieldType;
import se.spacify.app.spider.Request;
import se.spacify.app.spider.controller.Controller;
import se.spacify.net.Uri;

/**
 * The {@code spacify:table[:<slug>[:<row_id>[:<related slug>]]]} Spider controller:
 * a single template ({@link #TEMPLATE}) whose top-level {@code model.mode} branch
 * selects one of four screens —
 *
 * <ul>
 *   <li>{@code spacify:table} — the table-of-tables: list/create/delete custom tables.</li>
 *   <li>{@code spacify:table:<slug>} — a table's rows: list/create/delete rows,
 *       manage its fields.</li>
 *   <li>{@code spacify:table:<slug>:<row_id>} — one row: edit/save/delete its
 *       fields, with links into related tables.</li>
 *   <li>{@code spacify:table:<slug>:<row_id>:<related slug>} — the row again,
 *       read-only, alongside the related table's rows (those whose
 *       {@code <slug>_uri}/{@code _uris} field points at this row).</li>
 * </ul>
 *
 * <p>Mutations are applied in {@link #data(Request)} (before the read model is
 * built, so a render always reflects its own postback) via an {@code action} naming
 * convention: {@code nav:<uri>} is a pure navigation the owning
 * {@code se.spacify.app.data.views.DataView} intercepts before this controller ever
 * sees it; everything else (e.g. {@code createrow}, {@code deleterow:<id>},
 * {@code save}, {@code addfield}) mutates then falls through to a normal render.
 */
public class DataController extends Controller {

    private final DataRepository repo;

    public DataController(DataRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean acceptsUri(Uri uri) {
        return uri != null && "spacify".equals(uri.getScheme()) && uri.toString().startsWith("spacify:table");
    }

    @Override
    protected String template(Request request) {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> data(Request request) {
        Path path = Path.parse(request.getUri());
        String action = actionOf(request);
        Map<String, Object> posted = dataOf(request);
        String message;
        try {
            message = applyAction(path, action, posted);
        } catch (IllegalArgumentException e) {
            message = e.getMessage();
        } catch (SQLException e) {
            message = "Database error: " + e.getMessage();
        }

        try {
            if (path.slug == null) {
                return indexModel(message);
            }
            DataTable table = repo.findTable(path.slug);
            if (table == null) {
                return notFoundModel(message);
            }
            if (path.rowId == null) {
                return listModel(table, message);
            }
            DataRow row = repo.findRow(table, path.rowId);
            if (row == null) {
                return notFoundModel(message);
            }
            if (path.relatedSlug == null) {
                return detailModel(table, row, message);
            }
            DataTable relatedTable = repo.findTable(path.relatedSlug);
            if (relatedTable == null) {
                return notFoundModel(message);
            }
            return relatedModel(table, row, relatedTable, message);
        } catch (SQLException e) {
            return notFoundModel("Database error: " + e.getMessage());
        }
    }

    // ── Request parsing ──────────────────────────────────────────────────────────

    /** The {@code spacify:table} URI's path: {@code table[:slug[:rowId[:relatedSlug]]]}. */
    private record Path(String slug, String rowId, String relatedSlug) {
        static Path parse(String uri) {
            if (uri == null || !uri.startsWith("spacify:table")) {
                return new Path(null, null, null);
            }
            String rest = uri.substring("spacify:table".length());
            if (rest.startsWith(":")) {
                rest = rest.substring(1);
            }
            if (rest.isEmpty()) {
                return new Path(null, null, null);
            }
            String[] parts = rest.split(":", -1);
            String slug = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : null;
            String rowId = parts.length > 1 && !parts[1].isEmpty() ? parts[1] : null;
            String relatedSlug = parts.length > 2 && !parts[2].isEmpty() ? parts[2] : null;
            return new Path(slug, rowId, relatedSlug);
        }
    }

    private static String actionOf(Request request) {
        Object action = request.getHeaders() != null ? request.getHeaders().get("action") : null;
        return action != null ? action.toString() : "";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> dataOf(Request request) {
        Object data = request.getData();
        return data instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private static String str(Object o) {
        return o != null ? o.toString() : "";
    }

    // ── Mutations ────────────────────────────────────────────────────────────────

    /** Apply {@code action} (a no-op for a plain load or a {@code nav:} action) and return a status message. */
    private String applyAction(Path path, String action, Map<String, Object> posted) throws SQLException {
        if (action == null || action.isEmpty() || action.startsWith("nav:")) {
            return "";
        }
        if ("createtable".equals(action)) {
            DataTable t = repo.createTable(str(posted.get("new_table_name")));
            return "Created table \"" + t.getName() + "\".";
        }
        if (action.startsWith("deletetable:")) {
            DataTable t = repo.findTable(action.substring("deletetable:".length()));
            if (t == null) {
                return "Table not found.";
            }
            repo.deleteTable(t);
            return "Deleted table \"" + t.getName() + "\".";
        }

        if (path.slug == null) {
            return "";
        }
        DataTable table = repo.findTable(path.slug);
        if (table == null) {
            return "";
        }

        if ("addfield".equals(action)) {
            DataField f = repo.addField(table, str(posted.get("new_field_name")), str(posted.get("new_field_type")));
            return "Added field \"" + f.getName() + "\" (" + f.getSlug() + ").";
        }
        if (action.startsWith("deletefield:")) {
            DataField f = repo.findField(action.substring("deletefield:".length()));
            if (f == null) {
                return "Field not found.";
            }
            repo.deleteField(f);
            return "Deleted field \"" + f.getName() + "\".";
        }
        if ("createrow".equals(action)) {
            repo.createRow(table, repo.listFields(table), posted);
            return "Created row.";
        }
        if (action.startsWith("deleterow:")) {
            DataRow r = repo.findRow(table, action.substring("deleterow:".length()));
            if (r == null) {
                return "Row not found.";
            }
            repo.deleteRow(table, r);
            return "Deleted row.";
        }

        if (path.rowId == null) {
            return "";
        }
        DataRow row = repo.findRow(table, path.rowId);
        if (row == null) {
            return "";
        }

        if ("save".equals(action)) {
            repo.updateRow(table, row, repo.listFields(table), posted);
            return "Saved.";
        }
        if ("deleterow".equals(action)) {
            repo.deleteRow(table, row);
            return "Deleted row.";
        }
        return "";
    }

    // ── View models ──────────────────────────────────────────────────────────────

    private Map<String, Object> indexModel(String message) throws SQLException {
        List<Map<String, Object>> tables = new ArrayList<>();
        for (DataTable t : repo.listTables()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("slug", t.getSlug());
            m.put("name", Format.xml(t.getName()));
            m.put("fieldCount", repo.listFields(t).size());
            m.put("rowCount", repo.listRows(t).size());
            tables.add(m);
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("mode", "index");
        model.put("title", "Custom Tables");
        model.put("message", Format.xml(message));
        model.put("tables", tables);
        return model;
    }

    private Map<String, Object> notFoundModel(String message) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("mode", "notfound");
        model.put("title", "Not found");
        model.put("message", Format.xml(message));
        return model;
    }

    private Map<String, Object> listModel(DataTable table, String message) throws SQLException {
        List<DataField> tableFields = repo.listFields(table);
        List<Map<String, Object>> rowModels = new ArrayList<>();
        for (DataRow r : repo.listRows(table)) {
            rowModels.add(rowModel(r, tableFields));
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("mode", "list");
        model.put("title", table.getName());
        model.put("message", Format.xml(message));
        model.put("table", tableModel(table));
        model.put("fields", fieldModels(tableFields));
        model.put("rows", rowModels);
        return model;
    }

    private Map<String, Object> detailModel(DataTable table, DataRow row, String message) throws SQLException {
        List<DataField> tableFields = repo.listFields(table);
        Map<String, Object> editValues = new LinkedHashMap<>();
        for (DataField f : tableFields) {
            editValues.put(f.getSlug(), Format.xml(editValue(f, repo.valuesFor(row.getId(), f.getId()))));
        }
        List<Map<String, Object>> relatedTables = new ArrayList<>();
        for (DataTable rt : repo.relatedTablesFor(table)) {
            relatedTables.add(tableModel(rt));
        }
        Map<String, Object> rowModel = new LinkedHashMap<>();
        rowModel.put("id", row.getId());
        rowModel.put("createdAgo", Format.timestamp(row.getCreatedAt()));
        rowModel.put("updatedAgo", Format.timestamp(row.getUpdatedAt()));

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("mode", "detail");
        model.put("title", table.getName() + " row");
        model.put("message", Format.xml(message));
        model.put("table", tableModel(table));
        model.put("row", rowModel);
        model.put("fields", fieldModels(tableFields));
        model.put("values", editValues);
        model.put("relatedTables", relatedTables);
        return model;
    }

    private Map<String, Object> relatedModel(DataTable table, DataRow row, DataTable relatedTable, String message)
            throws SQLException {
        List<DataField> tableFields = repo.listFields(table);
        List<DataField> relatedFieldsList = repo.listFields(relatedTable);

        String rowUri = repo.rowUri(table.getSlug(), row.getId());
        List<Map<String, Object>> relatedRows = new ArrayList<>();
        for (DataRow rr : repo.findRelatedRows(relatedTable, table.getSlug(), rowUri)) {
            relatedRows.add(rowModel(rr, relatedFieldsList));
        }

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("mode", "related");
        model.put("title", table.getName() + " → " + relatedTable.getName());
        model.put("message", Format.xml(message));
        model.put("table", tableModel(table));
        model.put("row", rowModel(row, tableFields));
        model.put("fields", fieldModels(tableFields));
        model.put("relatedTable", tableModel(relatedTable));
        model.put("relatedFields", fieldModels(relatedFieldsList));
        model.put("relatedRows", relatedRows);
        return model;
    }

    // ── Model fragments ──────────────────────────────────────────────────────────

    private Map<String, Object> tableModel(DataTable t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("slug", t.getSlug());
        m.put("name", Format.xml(t.getName()));
        return m;
    }

    private List<Map<String, Object>> fieldModels(List<DataField> fields) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (DataField f : fields) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("slug", f.getSlug());
            m.put("name", Format.xml(f.getName()));
            m.put("type", f.getType().name());
            out.add(m);
        }
        return out;
    }

    /**
     * A row for grid display: {@code cells} (formatted text, every field) and
     * {@code links} (LINK-field URIs as {@code {uri,label}}, every field — empty list
     * for non-LINK fields) so the template can index either map for any field
     * without ever indexing a missing key.
     */
    private Map<String, Object> rowModel(DataRow row, List<DataField> fields) throws SQLException {
        Map<String, Object> cells = new LinkedHashMap<>();
        Map<String, Object> links = new LinkedHashMap<>();
        for (DataField f : fields) {
            List<DataValue> values = repo.valuesFor(row.getId(), f.getId());
            if (f.getType() == FieldType.LINK) {
                List<Map<String, Object>> linkList = new ArrayList<>();
                for (DataValue v : values) {
                    Map<String, Object> link = new LinkedHashMap<>();
                    link.put("uri", Format.xml(v.getValue()));
                    link.put("label", Format.xml(v.getValue()));
                    linkList.add(link);
                }
                links.put(f.getSlug(), linkList);
                cells.put(f.getSlug(), "");
            } else {
                String raw = values.isEmpty() ? null : values.get(0).getValue();
                cells.put(f.getSlug(), Format.xml(formatCell(f, raw)));
                links.put(f.getSlug(), List.of());
            }
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("id", row.getId());
        model.put("cells", cells);
        model.put("links", links);
        return model;
    }

    private static String formatCell(DataField f, String raw) {
        if (raw == null) {
            return "";
        }
        try {
            return switch (f.getType()) {
                case NUMBER -> Format.number(Long.parseLong(raw));
                case FLOAT -> Format.decimal(Double.parseDouble(raw));
                case TIMESTAMP -> Format.timestamp(Long.parseLong(raw));
                case TEXT, LINK -> raw;
            };
        } catch (NumberFormatException e) {
            return raw;
        }
    }

    /** An editable field's current value, prefilled into its detail-view {@code <input>}. */
    private static String editValue(DataField f, List<DataValue> values) {
        if (values.isEmpty()) {
            return "";
        }
        if (f.getType() == FieldType.LINK) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(values.get(i).getValue());
            }
            return sb.toString();
        }
        String raw = values.get(0).getValue();
        if (f.getType() == FieldType.TIMESTAMP) {
            try {
                return Format.isoDate(Long.parseLong(raw));
            } catch (NumberFormatException e) {
                return raw;
            }
        }
        return raw;
    }

    // ── Template ─────────────────────────────────────────────────────────────────

    private static final String TEMPLATE = """
        <view>
            <page id="main" title="${model.title}">
                <vbox>
                    % if model.message ~= "" then
                    <hbox>
                        <text>${model.message}</text>
                    </hbox>
                    % end
                    % if model.mode == "index" then
                    <text>Custom Tables</text>
                    % for i,t in ipairs(model.tables) do
                    <hbox>
                        <button onclick="nav:spacify:table:${t.slug}">${t.name}</button>
                        <text> (${t.fieldCount} fields, ${t.rowCount} rows) </text>
                        <button onclick="deletetable:${t.slug}">Delete table</button>
                    </hbox>
                    % end
                    <hbox>
                        <text>New table name:</text>
                        <input name="new_table_name"></input>
                        <button onclick="createtable">Create table</button>
                    </hbox>
                    % elseif model.mode == "list" then
                    <hbox>
                        <button onclick="nav:spacify:table">Back to tables</button>
                        <text>${model.table.name}</text>
                    </hbox>
                    <hbox>
                        % for fi,field in ipairs(model.fields) do
                        <text>${field.name}</text>
                        % end
                        <text>Actions</text>
                    </hbox>
                    % for ri,row in ipairs(model.rows) do
                    <hbox>
                        % for fi,field in ipairs(model.fields) do
                        % if field.type == "LINK" then
                        % for li,lnk in ipairs(row.links[field.slug]) do
                        <button onclick="nav:${lnk.uri}">${lnk.label}</button>
                        % end
                        % else
                        <text>${row.cells[field.slug]}</text>
                        % end
                        % end
                        <button onclick="nav:spacify:table:${model.table.slug}:${row.id}">View</button>
                        <button onclick="deleterow:${row.id}">Delete</button>
                    </hbox>
                    % end
                    <text>Add a row</text>
                    % for fi,field in ipairs(model.fields) do
                    <hbox>
                        <text>${field.name} (${field.type})</text>
                        <input name="f_${field.slug}"></input>
                    </hbox>
                    % end
                    <button onclick="createrow">Create row</button>
                    <text>Manage fields</text>
                    % for fi,field in ipairs(model.fields) do
                    <hbox>
                        <text>${field.name} (${field.type}) [${field.slug}]</text>
                        <button onclick="deletefield:${field.id}">Delete field</button>
                    </hbox>
                    % end
                    <hbox>
                        <text>New field name:</text>
                        <input name="new_field_name"></input>
                        <text>Type (text, link, number, float, timestamp):</text>
                        <input name="new_field_type"></input>
                        <button onclick="addfield">Add field</button>
                    </hbox>
                    % elseif model.mode == "detail" then
                    <hbox>
                        <button onclick="nav:spacify:table:${model.table.slug}">Back to ${model.table.name}</button>
                    </hbox>
                    <text>Created ${model.row.createdAgo} — updated ${model.row.updatedAgo}</text>
                    % for fi,field in ipairs(model.fields) do
                    <hbox>
                        <text>${field.name}</text>
                        <input name="f_${field.slug}">${model.values[field.slug]}</input>
                    </hbox>
                    % end
                    <hbox>
                        <button onclick="save">Save</button>
                        <button onclick="deleterow">Delete row</button>
                    </hbox>
                    % if #model.relatedTables > 0 then
                    <text>Related</text>
                    % for ri,rt in ipairs(model.relatedTables) do
                    <button onclick="nav:spacify:table:${model.table.slug}:${model.row.id}:${rt.slug}">${rt.name}</button>
                    % end
                    % end
                    % elseif model.mode == "related" then
                    <hbox>
                        <button onclick="nav:spacify:table:${model.table.slug}:${model.row.id}">Back to row</button>
                    </hbox>
                    <text>${model.table.name} row — related ${model.relatedTable.name}</text>
                    % for fi,field in ipairs(model.fields) do
                    <hbox>
                        <text>${field.name}:</text>
                        % if field.type == "LINK" then
                        % for li,lnk in ipairs(model.row.links[field.slug]) do
                        <button onclick="nav:${lnk.uri}">${lnk.label}</button>
                        % end
                        % else
                        <text>${model.row.cells[field.slug]}</text>
                        % end
                    </hbox>
                    % end
                    <hbox>
                        % for fi,rf in ipairs(model.relatedFields) do
                        <text>${rf.name}</text>
                        % end
                    </hbox>
                    % for ri,rr in ipairs(model.relatedRows) do
                    <hbox>
                        % for fi,rf in ipairs(model.relatedFields) do
                        % if rf.type == "LINK" then
                        % for li,lnk in ipairs(rr.links[rf.slug]) do
                        <button onclick="nav:${lnk.uri}">${lnk.label}</button>
                        % end
                        % else
                        <text>${rr.cells[rf.slug]}</text>
                        % end
                        % end
                        <button onclick="nav:spacify:table:${model.relatedTable.slug}:${rr.id}">View</button>
                    </hbox>
                    % end
                    % else
                    <text>Not found.</text>
                    <button onclick="nav:spacify:table">Back to tables</button>
                    % end
                </vbox>
            </page>
        </view>""";
}
