package se.spacify.app.web;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.db.entity.Bookmark;
import se.spacify.navigation.ViewStack;
import se.spacify.navigation.SidebarNode;
import se.spacify.app.Application;
import se.spacify.app.ApplicationContext;
import se.spacify.app.SidebarHandle;
import se.spacify.app.web.views.SPServiceWebView;
import se.spacify.app.web.views.SPWebView;
import se.spacify.web.BookmarkEvents;
import se.spacify.web.BookmarkManager;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

/**
 * Built-in plugin providing in-app web browsing: the {@link SPWebView} and
 * {@link SPServiceWebView} (which drive their own navigation through the view
 * stack), plus the "Sites" sidebar subtree of bookmarks kept in sync via
 * {@link BookmarkEvents}.
 */
public class WebApplication extends Application {

    private final Runnable refresh = this::refresh;
    private SidebarHandle sites;

    @Override
    public void onActivate(ApplicationContext ctx) {
        ViewStack vs = ctx.viewStack();
        ctx.registerView(new SPWebView(vs));
        ctx.registerView(new SPServiceWebView(vs));

        sites = ctx.addSidebarNode(new SidebarNode("Sites", null));
        refresh();

        BookmarkEvents.addListener(refresh);
    }

    @Override
    public void onDeactivate() {
        BookmarkEvents.removeListener(refresh);
    }

    private void refresh() {
        if (sites != null) sites.setChildren(siteNodes());
    }

    private static List<SidebarNode> siteNodes() {
        List<SidebarNode> out = new ArrayList<>();
        for (Bookmark root : BookmarkManager.roots()) {
            SidebarNode rootNode = siteNode(root);
            for (Bookmark child : BookmarkManager.childrenOf(root)) {
                rootNode.addChild(siteNode(child));
            }
            out.add(rootNode);
        }
        return out;
    }

    private static SidebarNode siteNode(Bookmark b) {
        SidebarNode sn = new SidebarNode(b.toString(), b.getSpacifyUri());
        if (b.getFavicon() != null) sn.setIcon(new ImageIcon(b.getFavicon()));
        return sn;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "web";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Web Browser";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
        
    }
}
