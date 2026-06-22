package se.spacify.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugins compiled into the main bundle (no separate jar). They load with the
 * application class loader and provide the app's core content: local playback,
 * the music library, and in-app web browsing.
 */
final class BuiltinPluginRegistry {

    private BuiltinPluginRegistry() {}

    static List<PluginDescriptor> descriptors() {
        ClassLoader cl = BuiltinPluginRegistry.class.getClassLoader();
        List<PluginDescriptor> list = new ArrayList<>();
        // Look-and-feel plugins: each contributes Designs (Chrome + Skin premixes)
        // selectable in Settings. Both enabled by default; WMP is the default Design.
        list.add(builtin("se.spacify.plugin.wmp", "Windows Media Player", "1.0.0",
            "se.spacify.plugin.wmp.WMPPlugin", cl));
        list.add(builtin("se.spacify.plugin.spot", "Spot", "1.0.0",
            "se.spacify.plugin.spot.SpotPlugin", cl));
        list.add(builtin("se.spacify.plugin.localmusic", "Local Music", "1.0.0",
            "se.spacify.plugin.localmusic.LocalMusicPlugin", cl));
        list.add(builtin("se.spacify.plugin.library", "Music Library", "1.0.0",
            "se.spacify.plugin.library.LibraryPlugin", cl));
        list.add(builtin("se.spacify.plugin.search", "Search", "1.0.0",
            "se.spacify.plugin.search.SearchPlugin", cl));
        list.add(builtin("se.spacify.plugin.web", "Web & Sites", "1.0.0",
            "se.spacify.plugin.web.WebPlugin", cl));
        list.add(builtin("se.spacify.plugin.musicbrainz", "MusicBrainz", "1.0.0",
            "se.spacify.plugin.musicbrainz.MusicBrainzPlugin", cl));
        // After MusicBrainz so the catalogue Services it registers are present
        // when the Catalogs sidebar folder is built.
        list.add(builtin("se.spacify.plugin.catalogue", "Catalogs", "1.0.0",
            "se.spacify.plugin.catalogue.CataloguePlugin", cl));
        // After local music so YouTube resolves as a fallback for tracks not held locally.
        list.add(builtin("se.spacify.plugin.youtube", "YouTube", "1.0.0",
            "se.spacify.plugin.youtube.YouTubePlugin", cl));
        // Download manager: captures audio downloads from the store/web views and
        // imports them into the library.
        list.add(builtin("se.spacify.plugin.downloads", "Download Manager", "1.0.0",
            "se.spacify.plugin.downloads.DownloadsPlugin", cl));
        return list;
    }

    private static PluginDescriptor builtin(String id, String name, String version,
                                            String mainClass, ClassLoader cl) {
        return new PluginDescriptor(id, name, version, mainClass,
            PluginDescriptor.Source.BUILTIN_BUNDLE, null, cl);
    }
}
