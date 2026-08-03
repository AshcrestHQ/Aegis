package org.ashcrest.music;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Metadata resolver for Apple Music URLs (songs, albums, playlists).
 */
public class AppleMusicResolver {

    public record AppleMusicTrack(String title, String artist, String album, String artworkUrl) {
        public String toSearchQuery() {
            if (artist != null && !artist.isBlank() && !artist.equalsIgnoreCase("Unknown Artist")) {
                return "ytsearch:" + artist + " - " + title;
            }
            return "ytsearch:" + title;
        }
    }

    private static final Pattern APPLE_MUSIC_PATTERN = Pattern.compile(
            "https?://music\\.apple\\.com/([a-z]{2})/(album|song|playlist)/([^/]+)/([a-zA-Z0-9.]+)(\\?i=([0-9]+))?"
    );

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean isAppleMusicUrl(String url) {
        return url != null && (url.contains("music.apple.com") || APPLE_MUSIC_PATTERN.matcher(url).find());
    }

    /**
     * Resolve tracks from an Apple Music URL.
     */
    public static List<AppleMusicTrack> resolve(String url) {
        List<AppleMusicTrack> tracks = new ArrayList<>();
        try {
            // Check for track ID in query parameter ?i=123456 or /song/
            String trackId = extractTrackId(url);
            if (trackId != null && !trackId.isEmpty()) {
                AppleMusicTrack track = fetchFromITunesApi(trackId);
                if (track != null) {
                    tracks.add(track);
                    return tracks;
                }
            }

            // Fallback to Web Scraping via OpenGraph Meta Tags
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(5000)
                    .get();

            String ogTitle = getMetaTag(doc, "og:title");
            String ogImage = getMetaTag(doc, "og:image");

            if (ogTitle != null && !ogTitle.isEmpty()) {
                String title = ogTitle;
                String artist = "Unknown Artist";

                if (ogTitle.contains(" by ")) {
                    String[] parts = ogTitle.split(" by ", 2);
                    title = parts[0].trim();
                    artist = parts[1].replaceAll(" on Apple Music.*$", "").replaceAll(" - Song.*$", "").trim();
                } else if (ogTitle.contains(" - ")) {
                    String[] parts = ogTitle.split(" - ", 2);
                    title = parts[0].trim();
                    artist = parts[1].replaceAll(" on Apple Music.*$", "").trim();
                }

                title = title.replaceAll(" on Apple Music$", "").replaceAll(" - Song by .*$", "").trim();

                tracks.add(new AppleMusicTrack(title, artist, "Apple Music", ogImage));
            }
        } catch (Exception e) {
            System.err.println("⚠️ Apple Music metadata resolution warning: " + e.getMessage());
        }
        return tracks;
    }

    private static String extractTrackId(String url) {
        Matcher matcher = Pattern.compile("[?&]i=([0-9]+)").matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        Matcher songMatcher = Pattern.compile("/song/[^/]+/([0-9]+)").matcher(url);
        if (songMatcher.find()) {
            return songMatcher.group(1);
        }
        return null;
    }

    private static AppleMusicTrack fetchFromITunesApi(String trackId) {
        try {
            String apiUrl = "https://itunes.apple.com/lookup?id=" + trackId + "&entity=song";
            URLConnection connection = URI.create(apiUrl).toURL().openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream in = connection.getInputStream()) {
                JsonNode root = objectMapper.readTree(in);
                JsonNode results = root.get("results");
                if (results != null && results.isArray() && !results.isEmpty()) {
                    JsonNode trackNode = results.get(0);
                    String trackName = trackNode.path("trackName").asText("Unknown Track");
                    String artistName = trackNode.path("artistName").asText("Unknown Artist");
                    String collectionName = trackNode.path("collectionName").asText("Unknown Album");
                    String artworkUrl = trackNode.path("artworkUrl100").asText("");
                    return new AppleMusicTrack(trackName, artistName, collectionName, artworkUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ iTunes Lookup API error: " + e.getMessage());
        }
        return null;
    }

    private static String getMetaTag(Document doc, String attr) {
        org.jsoup.select.Elements meta = doc.select("meta[property=" + attr + "]");
        if (meta.isEmpty()) {
            meta = doc.select("meta[name=" + attr + "]");
        }
        return meta.isEmpty() ? null : meta.first().attr("content");
    }
}
