package se.spacify.plugin.musicbrainz;

import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.PluginSetting;

import java.util.List;

/**
 * Built-in plugin contributing the MusicBrainz catalogue. Registers a
 * {@link MusicBrainzService} (the discovery aspect,
 * {@link se.spacify.service.catalogue.MusicCatalogueService}) so search and
 * MBID/ISRC/ISNI lookups can resolve against the MusicBrainz web Service.
 *
 * <p>Exposes a single {@code contact} setting folded into the API
 * {@code User-Agent}, which MusicBrainz asks callers to supply.
 */
public class MusicBrainzPlugin implements Plugin {

    /** Settings key for the contact URL/email sent in the MusicBrainz User-Agent. */
    public static final String SETTING_CONTACT = "contact";

    @Override
    public void onActivate(PluginContext ctx) {
        String contact = ctx.settings().getString(SETTING_CONTACT);
        ctx.registerService(new MusicBrainzService(contact));
    }

    @Override
    public List<PluginSetting> getSettingsSchema() {
        return List.of(
            PluginSetting.string(SETTING_CONTACT,
                "Contact (email or URL, sent to MusicBrainz in the User-Agent)", "")
        );
    }
}
