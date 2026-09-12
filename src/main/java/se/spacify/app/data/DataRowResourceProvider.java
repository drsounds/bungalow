package se.spacify.app.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.spacify.app.data.model.DataField;
import se.spacify.app.data.model.DataRow;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.data.model.DataValue;
import se.spacify.net.cache.InternalResourceProvider;
import se.spacify.net.cache.RequestStore;

/**
 * Serves a {@code spacify:table:<slug>:<row_id>} row's field values as plain
 * text through the global {@link RequestStore}, so anything that just wants a
 * row's data — not its rendered detail screen, which is
 * {@code se.spacify.app.data.controller.DataController}'s job — can request it
 * exactly like an {@code https:} resource and get a cached, cross-session
 * answer instead of re-querying SQLite every time.
 *
 * <p>Read-only by construction: {@link #fetch} never applies an {@code action},
 * so a cached row can never go stale in a way that silently hides a save —
 * within its TTL it's just a possibly-few-minutes-old snapshot, exactly like a
 * cached HTTP GET.
 */
public final class DataRowResourceProvider implements InternalResourceProvider {

    private static final Pattern URI = Pattern.compile("spacify:table:([^:]+):([^:]+)");

    private final DataRepository repo;

    public DataRowResourceProvider(DataRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean accepts(String uri) {
        return URI.matcher(uri).matches();
    }

    @Override
    public byte[] fetch(String uri) throws IOException {
        Matcher m = URI.matcher(uri);
        if (!m.matches()) {
            return null;
        }
        try {
            DataTable table = repo.findTable(m.group(1));
            if (table == null) {
                return null;
            }
            DataRow row = repo.findRow(table, m.group(2));
            if (row == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (DataField f : repo.listFields(table)) {
                List<DataValue> values = repo.valuesFor(row.getId(), f.getId());
                sb.append(f.getSlug()).append('=');
                for (int i = 0; i < values.size(); i++) {
                    if (i > 0) sb.append(',');
                    sb.append(values.get(i).getValue());
                }
                sb.append('\n');
            }
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        } catch (SQLException e) {
            throw new IOException("Row lookup failed for " + uri, e);
        }
    }

    @Override
    public String contentType(String uri) {
        return "text/plain; charset=utf-8";
    }
}
