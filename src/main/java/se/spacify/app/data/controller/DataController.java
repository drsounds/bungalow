package se.spacify.app.data.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import se.spacify.app.data.DataRepository;
import se.spacify.app.data.Format;
import se.spacify.app.data.model.DataField;
import se.spacify.app.data.model.DataRelation;
import se.spacify.app.data.model.DataRow;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.data.model.DataValue;
import se.spacify.app.data.model.FieldType;
import se.spacify.app.spider.Request;
import se.spacify.app.spider.controller.Controller;
import se.spacify.net.Uri;

/**
 * The {@code spacify:table:<slug>:<row_id>} Spider controller: one row, editable —
 * a single template ({@link #TEMPLATE}) with one {@code <page>} for the row's own
 * fields (a belongsTo field renders as a picker, via {@link DataRepository#belongsToRelationsOn})
 * plus one sibling {@code <page>} per {@link DataRepository#relationTabsFor configured
 * relation} — each a tab listing the related/junction rows pointing at this one.
 *
 * <p>The table-of-tables index and a table's row list are native, Library-style
 * views ({@code se.spacify.app.data.views.DataTablesListView}/{@code DataTableRowsView})
 * — not Spider-rendered — so this controller (reached only through
 * {@code se.spacify.app.data.views.DataView}, whose {@code acceptsUri} requires a row
 * id) never sees those URIs.
 *
 * <p>Mutations are applied in {@link #data(Request)} (before the read model is
 * built, so a render always reflects its own postback) via an {@code action} naming
 * convention: {@code nav:<uri>} is a pure navigation the owning
 * {@code se.spacify.app.data.views.DataView} intercepts before this controller ever
 * sees it; {@code save} and {@code deleterow} mutate then fall through to a normal render.
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
            DataTable table = repo.findTable(path.slug);
            if (table == null) {
                return notFoundModel(message);
            }
            DataRow row = repo.findRow(table, path.rowId);
            if (row == null) {
                return notFoundModel(message);
            }
            return detailModel(table, row, message);
        } catch (SQLException e) {
            return notFoundModel("Database error: " + e.getMessage());
        }
    }

    // ── Request parsing ──────────────────────────────────────────────────────────

    /**
     * The {@code spacify:table:<slug>[:<row_id>]} URI's path. In the live app
     * {@code slug} and {@code rowId} are always both present —
     * {@link se.spacify.app.data.views.DataView#acceptsUri} requires both before this
     * controller is ever reached — but callers that hit this controller directly
     * (e.g. tests exercising {@code spacify:table} or {@code spacify:table:<slug>}
     * alone) can post a shorter URI, so missing segments parse to {@code null} rather
     * than throwing; {@link #data} already renders {@code notFoundModel} for a
     * {@code null} slug/rowId via {@link DataRepository#findTable}/{@link DataRepository#findRow}.
     * A trailing third segment (an old {@code :<related slug>} bookmark from before
     * relations became tabs) is simply ignored rather than rejected.
     */
    private record Path(String slug, String rowId) {
        static Path parse(String uri) {
            String prefix = "spacify:table:";
            String rest = uri.length() > prefix.length() ? uri.substring(prefix.length()) : "";
            String[] parts = rest.isEmpty() ? new String[0] : rest.split(":", -1);
            String slug = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : null;
            String rowId = parts.length > 1 && !parts[1].isEmpty() ? parts[1] : null;
            return new Path(slug, rowId);
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

    // ── Mutations ────────────────────────────────────────────────────────────────

    /** Apply {@code action} (a no-op for a plain load or a {@code nav:} action) and return a status message. */
    private String applyAction(Path path, String action, Map<String, Object> posted) throws SQLException {
        if (action == null || action.isEmpty() || action.startsWith("nav:")) {
            return "";
        }
        DataTable table = repo.findTable(path.slug);
        if (table == null) {
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

    private Map<String, Object> notFoundModel(String message) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("mode", "notfound");
        model.put("title", "Not found");
        model.put("message", Format.xml(message));
        model.put("relationTabs", List.of());
        return model;
    }

    private Map<String, Object> detailModel(DataTable table, DataRow row, String message) throws SQLException {
        List<DataField> tableFields = repo.listFields(table);
        Map<String, Object> editValues = new LinkedHashMap<>();
        for (DataField f : tableFields) {
            editValues.put(f.getSlug(), Format.xml(editValue(f, repo.valuesFor(row.getId(), f.getId()))));
        }
        Map<String, Object> rowModel = new LinkedHashMap<>();
        rowModel.put("id", row.getId());
        rowModel.put("name", Format.xml(row.getName() != null ? row.getName() : ""));
        rowModel.put("createdAgo", Format.timestamp(row.getCreatedAt()));
        rowModel.put("updatedAgo", Format.timestamp(row.getUpdatedAt()));

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("mode", "detail");
        model.put("title", Format.xml(table.getName()) + " row");
        model.put("message", Format.xml(message));
        model.put("table", tableModel(table));
        model.put("row", rowModel);
        model.put("fields", editableFieldModels(table, tableFields, row));
        model.put("values", editValues);
        model.put("relationTabs", relationTabModels(table, row));
        return model;
    }

    /** Each of {@code fields} for the edit form: a belongsTo-backed field gets {@code options}
     *  for a {@code <select>} picker; every other field renders its plain {@code <input>} as before. */
    private List<Map<String, Object>> editableFieldModels(DataTable table, List<DataField> fields, DataRow row)
            throws SQLException {
        Map<String, DataRelation> belongsToByField = new LinkedHashMap<>();
        for (DataRelation r : repo.belongsToRelationsOn(table)) {
            belongsToByField.put(r.getFieldId(), r);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (DataField f : fields) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("slug", f.getSlug());
            m.put("name", Format.xml(f.getName()));
            DataRelation rel = belongsToByField.get(f.getId());
            if (rel != null && !f.isMultiValueLink()) {
                List<DataValue> current = repo.valuesFor(row.getId(), f.getId());
                String currentUri = current.isEmpty() ? null : current.get(0).getValue();
                m.put("belongsTo", true);
                m.put("options", pickerOptions(rel, currentUri));
            } else {
                m.put("belongsTo", false);
            }
            out.add(m);
        }
        return out;
    }

    /** {@code <option>} models for a belongsTo picker: a blank "—" plus one per row of the
     *  relation's target table, the current value (if any) marked {@code selected}. If the
     *  currently stored pointer no longer matches any live row (its target was soft-deleted
     *  since), it's kept as one extra option — still selected — rather than silently falling
     *  back to the blank option, which would clear the pointer on the next unrelated Save. */
    private List<Map<String, Object>> pickerOptions(DataRelation rel, String currentUri) throws SQLException {
        List<Map<String, Object>> out = new ArrayList<>();
        Map<String, Object> blank = new LinkedHashMap<>();
        blank.put("value", "");
        blank.put("label", "—");
        blank.put("selected", currentUri == null);
        out.add(blank);
        DataTable target = repo.findTableById(rel.getTargetTableId());
        boolean foundCurrent = false;
        if (target != null) {
            for (DataRow r : repo.listRows(target)) {
                String uri = repo.rowUri(target.getSlug(), r.getId());
                boolean selected = uri.equals(currentUri);
                foundCurrent |= selected;
                Map<String, Object> opt = new LinkedHashMap<>();
                opt.put("value", Format.xml(uri));
                opt.put("label", Format.xml(r.getName() != null ? r.getName() : uri));
                opt.put("selected", selected);
                out.add(opt);
            }
        }
        if (currentUri != null && !foundCurrent) {
            Map<String, Object> missing = new LinkedHashMap<>();
            missing.put("value", Format.xml(currentUri));
            missing.put("label", "(missing row)");
            missing.put("selected", true);
            out.add(missing);
        }
        return out;
    }

    /** One model per {@link DataRepository.RelationTab}, each becoming a sibling {@code <page>}. */
    private List<Map<String, Object>> relationTabModels(DataTable table, DataRow row) throws SQLException {
        List<Map<String, Object>> out = new ArrayList<>();
        String rowUri = repo.rowUri(table.getSlug(), row.getId());
        for (DataRepository.RelationTab tab : repo.relationTabsFor(table)) {
            List<DataField> listedFields = repo.listFields(tab.listedTable());
            List<Map<String, Object>> rowModels = new ArrayList<>();
            for (DataRow rr : repo.rowsPointingAt(tab.pointerField(), rowUri)) {
                rowModels.add(rowModel(rr, listedFields));
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", Format.xml(tab.label()));
            m.put("tableSlug", tab.listedTable().getSlug());
            m.put("fields", fieldModels(listedFields));
            m.put("rows", rowModels);
            out.add(m);
        }
        return out;
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
                    % if model.mode == "detail" then
                    <hbox>
                        <button onclick="nav:spacify:table:${model.table.slug}">Back to ${model.table.name}</button>
                    </hbox>
                    <text>Created ${model.row.createdAgo} — updated ${model.row.updatedAgo}</text>
                    <hbox>
                        <text>Name</text>
                        <input name="name">${model.row.name}</input>
                    </hbox>
                    % for fi,field in ipairs(model.fields) do
                    <hbox>
                        <text>${field.name}</text>
                        % if field.belongsTo then
                        <select name="f_${field.slug}">
                            % for oi,opt in ipairs(field.options) do
                            <option value="${opt.value}" selected="${opt.selected}">${opt.label}</option>
                            % end
                        </select>
                        % else
                        <input name="f_${field.slug}">${model.values[field.slug]}</input>
                        % end
                    </hbox>
                    % end
                    <hbox>
                        <button onclick="save">Save</button>
                        <button onclick="deleterow">Delete row</button>
                    </hbox>
                    % else
                    <text>Not found.</text>
                    <button onclick="nav:spacify:table">Back to tables</button>
                    % end
                </vbox>
            </page>
            % for ti,tab in ipairs(model.relationTabs) do
            <page title="${tab.label}">
                <vbox>
                    <text>${tab.label} (${#tab.rows})</text>
                    <hbox>
                        % for fi,rf in ipairs(tab.fields) do
                        <text>${rf.name}</text>
                        % end
                    </hbox>
                    % for ri,rr in ipairs(tab.rows) do
                    <hbox>
                        % for fi,rf in ipairs(tab.fields) do
                        % if rf.type == "LINK" then
                        % for li,lnk in ipairs(rr.links[rf.slug]) do
                        <button onclick="nav:${lnk.uri}">${lnk.label}</button>
                        % end
                        % else
                        <text>${rr.cells[rf.slug]}</text>
                        % end
                        % end
                        <button onclick="nav:spacify:table:${tab.tableSlug}:${rr.id}">View</button>
                    </hbox>
                    % end
                </vbox>
            </page>
            % end
        </view>""";
}
