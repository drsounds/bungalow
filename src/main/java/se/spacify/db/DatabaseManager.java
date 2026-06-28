package se.spacify.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import se.spacify.db.entity.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Singleton that owns the SQLite connection and exposes ORMLite DAOs.
 * Call init() once at startup; close() on shutdown.
 */
public class DatabaseManager {

    private static DatabaseManager instance;

    private ConnectionSource connectionSource;

    private Dao<Artist,                  Integer> artistDao;
    private Dao<MusicWork,               Integer> musicWorkDao;
    private Dao<Recording,               Integer> recordingDao;
    private Dao<RecordingFile,           Integer> recordingFileDao;
    private Dao<MusicRelease,            Integer> releaseDao;
    private Dao<Track,                   Integer> trackDao;
    private Dao<RecordingCreatorCredit,  Integer> recordingArtistCreditDao;
    private Dao<ReleaseCreatorCredit,    Integer> releaseArtistCreditDao;
    private Dao<LocalFile,               Integer> localFileDao;
    private Dao<Bookmark,                Integer> bookmarkDao;
    private Dao<MusicServiceTrack,       Integer> musicServiceTrackDao;
    private Dao<Download,                Integer> downloadDao;
    private Dao<Playlist,                Integer> playlistDao;
    private Dao<PlaylistRow,             Integer> playlistRowDao;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void init() throws Exception {
        Path dbFile = Path.of(System.getProperty("user.home"), ".spacify", "library.db");
        Files.createDirectories(dbFile.getParent());

        connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + dbFile);

        // Create tables if they don't already exist (concrete entities only;
        // the abstract Node/Content/Creator/Release/ContentCollection bases are
        // not tables themselves).
        TableUtils.createTableIfNotExists(connectionSource, Artist.class);
        TableUtils.createTableIfNotExists(connectionSource, MusicWork.class);
        TableUtils.createTableIfNotExists(connectionSource, Recording.class);
        TableUtils.createTableIfNotExists(connectionSource, RecordingFile.class);
        TableUtils.createTableIfNotExists(connectionSource, MusicRelease.class);
        TableUtils.createTableIfNotExists(connectionSource, Track.class);
        TableUtils.createTableIfNotExists(connectionSource, RecordingCreatorCredit.class);
        TableUtils.createTableIfNotExists(connectionSource, ReleaseCreatorCredit.class);
        TableUtils.createTableIfNotExists(connectionSource, LocalFile.class);
        TableUtils.createTableIfNotExists(connectionSource, Bookmark.class);
        TableUtils.createTableIfNotExists(connectionSource, MusicServiceTrack.class);
        TableUtils.createTableIfNotExists(connectionSource, Download.class);
        TableUtils.createTableIfNotExists(connectionSource, Playlist.class);
        TableUtils.createTableIfNotExists(connectionSource, PlaylistRow.class);

        artistDao                = DaoManager.createDao(connectionSource, Artist.class);
        musicWorkDao             = DaoManager.createDao(connectionSource, MusicWork.class);
        recordingDao             = DaoManager.createDao(connectionSource, Recording.class);
        recordingFileDao         = DaoManager.createDao(connectionSource, RecordingFile.class);
        releaseDao               = DaoManager.createDao(connectionSource, MusicRelease.class);
        trackDao                 = DaoManager.createDao(connectionSource, Track.class);
        recordingArtistCreditDao = DaoManager.createDao(connectionSource, RecordingCreatorCredit.class);
        releaseArtistCreditDao   = DaoManager.createDao(connectionSource, ReleaseCreatorCredit.class);
        localFileDao             = DaoManager.createDao(connectionSource, LocalFile.class);
        bookmarkDao              = DaoManager.createDao(connectionSource, Bookmark.class);
        musicServiceTrackDao     = DaoManager.createDao(connectionSource, MusicServiceTrack.class);
        downloadDao              = DaoManager.createDao(connectionSource, Download.class);
        playlistDao              = DaoManager.createDao(connectionSource, Playlist.class);
        playlistRowDao           = DaoManager.createDao(connectionSource, PlaylistRow.class);
    }

    public void close() {
        if (connectionSource != null) {
            try { connectionSource.close(); } catch (Exception ignored) {}
            connectionSource = null;
        }
    }

    // ── DAO accessors ─────────────────────────────────────────────────────────

    public Dao<Artist,                 Integer> artistDao()                { return artistDao; }
    public Dao<MusicWork,              Integer> musicWorkDao()             { return musicWorkDao; }
    public Dao<Recording,              Integer> recordingDao()             { return recordingDao; }
    public Dao<RecordingFile,          Integer> recordingFileDao()         { return recordingFileDao; }
    public Dao<MusicRelease,           Integer> releaseDao()               { return releaseDao; }
    public Dao<Track,                  Integer> trackDao()                 { return trackDao; }
    public Dao<RecordingCreatorCredit, Integer> recordingArtistCreditDao() { return recordingArtistCreditDao; }
    public Dao<ReleaseCreatorCredit,   Integer> releaseArtistCreditDao()   { return releaseArtistCreditDao; }
    public Dao<LocalFile,              Integer> localFileDao()             { return localFileDao; }
    public Dao<Bookmark,               Integer> bookmarkDao()              { return bookmarkDao; }
    public Dao<MusicServiceTrack,      Integer> musicServiceTrackDao()     { return musicServiceTrackDao; }
    public Dao<Download,               Integer> downloadDao()              { return downloadDao; }
    public Dao<Playlist,               Integer> playlistDao()              { return playlistDao; }
    public Dao<PlaylistRow,            Integer> playlistRowDao()           { return playlistRowDao; }
}
