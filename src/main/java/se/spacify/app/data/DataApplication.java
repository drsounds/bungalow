package se.spacify.app.data;

import java.util.ArrayList;
import java.util.List;

import se.spacify.app.Application;
import se.spacify.app.ApplicationContext;
import se.spacify.app.SidebarHandle;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.data.views.DataTableRowsView;
import se.spacify.app.data.views.DataTablesListView;
import se.spacify.app.data.views.DataView;
import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.navigation.SidebarNode;

/**
 * Built-in plugin letting users CRUD their own custom tables — arbitrary named
 * tables with an unlimited, user-defined set of typed fields (text, link, number,
 * float, timestamp) — without touching code. Registers three {@code spacify:table...}
 * views, mirroring how {@code se.spacify.app.library.concept.LibraryConcept} registers
 * one view per Library screen: {@link DataTablesListView} (the table-of-tables index)
 * and {@link DataTableRowsView} (one table's row list) render through the same
 * {@link se.spacify.controls.Table} grid Library's own list views use, while
 * {@link DataView} (see it and {@link se.spacify.app.data.controller.DataController})
 * still renders the row-detail/related-rows screens. Also adds a "Custom Tables"
 * sidebar node whose children track the live table list.
 */
public class DataApplication extends Application {

    private final DataRepository repo = new DataRepository();
    private SidebarHandle sidebar;

    @Override
    public String getId() {
        return "data";
    }

    @Override
    public String getName() {
        return "Custom Tables";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // No aspect-manager wiring needed.
    }

    @Override
    public void onActivate(ApplicationContext ctx) {
        ctx.registerView(new DataTablesListView(ctx.viewStack(), repo));
        ctx.registerView(new DataTableRowsView(ctx.viewStack(), repo));
        ctx.registerView(new DataView(ctx.viewStack(), repo));

        sidebar = ctx.addSidebarNode(new SidebarNode("Custom Tables", "spacify:table"));
        repo.setOnTablesChanged(this::refreshSidebar);
        refreshSidebar();
    }

    @Override
    public void onDeactivate() {
        repo.setOnTablesChanged(null);
    }

    private void refreshSidebar() {
        if (sidebar == null) {
            return;
        }
        List<SidebarNode> children = new ArrayList<>();
        try {
            for (DataTable t : repo.listTables()) {
                children.add(new SidebarNode(t.getName(), repo.tableUri(t.getSlug())));
            }
        } catch (Exception ignored) {
            // Best-effort: an empty sidebar subtree beats crashing activation.
        }
        sidebar.setChildren(children);
    }
}
