package se.spacify.net.cache;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * One persisted row of {@link RequestStore}'s cache: the last-fetched body of a
 * request URI plus enough revalidation metadata (ETag/Last-Modified) and a
 * freshness window ({@code fetchedAt}/{@code expiresAt}) to avoid re-fetching it
 * needlessly. The URI itself is the primary key — one row per resource,
 * regardless of scheme.
 */
@DatabaseTable(tableName = "cached_resources")
public class CachedResource {

    @DatabaseField(id = true)
    private String uri;

    @DatabaseField
    private String scheme;

    @DatabaseField(columnName = "content_type")
    private String contentType;

    @DatabaseField
    private String etag;

    @DatabaseField(columnName = "last_modified")
    private String lastModified;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] body;

    @DatabaseField(columnName = "fetched_at")
    private long fetchedAt;

    @DatabaseField(columnName = "expires_at")
    private long expiresAt;

    /** Required by ORMLite. */
    public CachedResource() {}

    CachedResource(String uri, String scheme, String contentType, String etag,
                   String lastModified, byte[] body, long fetchedAt, long expiresAt) {
        this.uri = uri;
        this.scheme = scheme;
        this.contentType = contentType;
        this.etag = etag;
        this.lastModified = lastModified;
        this.body = body;
        this.fetchedAt = fetchedAt;
        this.expiresAt = expiresAt;
    }

    public String getUri()          { return uri; }
    public String getScheme()       { return scheme; }
    public String getContentType()  { return contentType; }
    public String getEtag()         { return etag; }
    public String getLastModified() { return lastModified; }
    public byte[] getBody()         { return body; }
    public long   getFetchedAt()    { return fetchedAt; }
    public long   getExpiresAt()    { return expiresAt; }

    void setFetchedAt(long v) { this.fetchedAt = v; }
    void setExpiresAt(long v) { this.expiresAt = v; }
}
