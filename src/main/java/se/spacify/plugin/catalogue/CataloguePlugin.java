package se.spacify.plugin.catalogue;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.navigation.ViewStack;
import se.spacify.navigation.SidebarNode;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.SidebarHandle;
import se.spacify.plugin.catalogue.service.MusicCatalogueService;
import se.spacify.plugin.library.views.ArtistsCatalogView;
import se.spacify.plugin.library.views.RecordingsCatalogView;
import se.spacify.plugin.library.views.ReleasesCatalogView;

/**
 * Built-in plugin contributing the <strong>Catalogs</strong> sidebar folder and
 * the catalogue browsing views. Every registered
 * {@link MusicCatalogueService} (MusicBrainz, …) becomes a node with
 * Artists / Releases / Recordings sub-entries that open the shared, async
 * catalogue views — browsing remote data on background threads, in the spirit of
 * Windows Media Player 11's "Urge" online catalogue.
 */
public class CataloguePlugin extends Plugin {

    @Override
    public void onActivate(PluginContext ctx) {
        ViewStack viewStack = ctx.viewStack();
        ctx.registerView(new ArtistsCatalogView(viewStack));
        ctx.registerView(new ReleasesCatalogView(viewStack));
        ctx.registerView(new RecordingsCatalogView(viewStack));

        SidebarNode catalogs = new SidebarNode("Catalogs", null);
        for (MusicCatalogueService svc : getManager().getMainWindow().getServiceManager().getServices(MusicCatalogueService.class)) {
            String id = svc.getId();
            SidebarNode node = new SidebarNode(svc.getName(), null);
            node.addChild(new SidebarNode("Artists",    "spacify:catalog:" + id + ":artists"));
            node.addChild(new SidebarNode("Releases",   "spacify:catalog:" + id + ":releases"));
            node.addChild(new SidebarNode("Recordings", "spacify:catalog:" + id + ":recordings"));
            catalogs.addChild(node);
        }

        SidebarHandle handle = ctx.addSidebarNode(catalogs);
        handle.expand();
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "catalogs";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Catalogs";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
        return;
    }
}
