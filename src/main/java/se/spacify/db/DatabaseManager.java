package se.spacify.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that owns the SQLite connection and hands out ORMLite DAOs.
 *
 * <p>It is deliberately ignorant of every concrete entity: apps and concepts own
 * their own entity classes (each in its app's {@code model} package) and register
 * their tables on activation via {@code ApplicationContext.registerEntity(...)} /
 * {@code ConceptContext.registerEntity(...)}, which delegate here. Tables are
 * created on demand and DAOs cached, so registration order between plugins does
 * not matter — reading an entity another plugin owns auto-ensures its table.
 *
 * <p>Call {@link #init()} once at startup (opens the connection only) and
 * {@link #close()} on shutdown.
 */
public class DatabaseManager {

    private static DatabaseManager instance;

    private ConnectionSource connectionSource;

    /** Per-entity DAO cache; the key's table is created on first access. */
    private final Map<Class<?>, Dao<?, ?>> daoCache = new ConcurrentHashMap<>();

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    /** Open the SQLite connection. Does not create any tables — apps register their own. */
    public void init() throws Exception {
        Path dbFile = Path.of(System.getProperty("user.home"), ".spacify", "library.db");
        Files.createDirectories(dbFile.getParent());

        connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + dbFile);
    }

    public void close() {
        if (connectionSource != null) {
            try { connectionSource.close(); } catch (Exception ignored) {}
            connectionSource = null;
        }
        daoCache.clear();
    }

    public ConnectionSource getConnectionSource() { return connectionSource; }

    // ── Entity registry ─────────────────────────────────────────────────────────

    /**
     * Ensure {@code entity}'s table exists and return its (cached) DAO. Idempotent
     * and order-independent: the table is created-if-not-exists on first call.
     */
    @SuppressWarnings("unchecked")
    public <T> Dao<T, Integer> dao(Class<T> entity) {
        return (Dao<T, Integer>) daoCache.computeIfAbsent(entity, c -> {
            try {
                TableUtils.createTableIfNotExists(connectionSource, c);
                return DaoManager.createDao(connectionSource, c);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to register entity " + c.getName(), e);
            }
        });
    }

    /**
     * Eagerly create an app's entity table at activation time. Same semantics as
     * {@link #dao(Class)}; named for use from {@code onActivate} registration.
     */
    public <T> Dao<T, Integer> registerEntity(Class<T> entity) {
        return dao(entity);
    }
}
