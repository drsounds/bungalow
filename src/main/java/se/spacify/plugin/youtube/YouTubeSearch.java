package se.spacify.plugin.youtube;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves a search query to YouTube video ids without an API key, by fetching
 * the public results page and extracting the {@code "videoId":"…"} entries from
 * its embedded {@code ytInitialData}. Needed because the IFrame Player API's
 * {@code listType:"search"} loader was removed by YouTube (Nov 2020), so the
 * player must be handed concrete video ids to {@code loadVideoById}.
 */
final class YouTubeSearch {

    private YouTubeSearch() {}

    private static final Pattern VIDEO_ID = Pattern.compile("\"videoId\":\"([A-Za-z0-9_-]{11})\"");

    // A desktop UA + en/US locale + consent cookie so we get the normal results
    // page (with ytInitialData) rather than a consent interstitial.
    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
        + "Chrome/124.0.0.0 Safari/537.36";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    /** Resolve via the scrape only (no API key). */
    static List<String> search(String query) {
        return search(query, null);
    }

    /**
     * Top video ids for {@code query}. When {@code apiKey} is set, uses the
     * YouTube Data API (more reliable, embeddable-only); otherwise — or if the
     * API call fails (bad key / quota) — falls back to scraping the results page.
     */
    static List<String> search(String query, String apiKey) {
        if (apiKey != null && !apiKey.isBlank()) {
            List<String> viaApi = apiSearch(query, apiKey);
            if (!viaApi.isEmpty()) return viaApi;
        }
        return scrapeSearch(query);
    }

    // ── YouTube Data API v3 (when a key is configured) ──────────────────────────────

    private static List<String> apiSearch(String query, String apiKey) {
        List<String> ids = new ArrayList<>();
        try {
            String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video"
                + "&videoEmbeddable=true&maxResults=12&q="
                + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15)).GET().build();
            String body = CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();

            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
            if (root.has("items")) {
                for (com.google.gson.JsonElement el : root.getAsJsonArray("items")) {
                    com.google.gson.JsonObject id = el.getAsJsonObject().getAsJsonObject("id");
                    if (id != null && id.has("videoId")) ids.add(id.get("videoId").getAsString());
                }
            }
        } catch (Exception ignored) {
            // bad key / quota / network → empty, caller falls back to the scrape.
        }
        return ids;
    }

    // ── Public results-page scrape (no API key) ─────────────────────────────────────

    /** Top video ids for {@code query} (in result order, de-duplicated). Empty on failure. */
    private static List<String> scrapeSearch(String query) {
        List<String> ids = new ArrayList<>();
        try {
            String url = "https://www.youtube.com/results?hl=en&gl=US&search_query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Cookie", "CONSENT=YES+1")
                .GET()
                .build();
            String body = CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();

            Matcher m = VIDEO_ID.matcher(body);
            Set<String> unique = new LinkedHashSet<>();
            while (m.find() && unique.size() < 12) unique.add(m.group(1));
            ids.addAll(unique);
        } catch (Exception ignored) {
            // network/parse failure → empty; the caller reports "couldn't find".
        }
        return ids;
    }

    /** Whether {@code s} is already a bare 11-character YouTube video id. */
    static boolean isVideoId(String s) {
        return s != null && s.matches("[A-Za-z0-9_-]{11}");
    }
}
