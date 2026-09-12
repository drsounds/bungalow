package se.spacify.app.search.views;

import se.spacify.navigation.View;
import se.spacify.navigation.ViewStack;
import se.spacify.search.EntityKind;
import se.spacify.search.SearchProvider;
import se.spacify.search.SearchResult;
import se.spacify.service.media.PlaybackCoordinator;
import se.spacify.ui.theme.ThemeManager;
import se.spacify.controls.Control;
import se.spacify.controls.Label;
import se.spacify.controls.Panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import se.spacify.controls.ScrollPane;
import javax.swing.SwingWorker;

/**
 * The unified search screen ({@code spacify:search?q=…}). It queries every
 * registered {@link SearchProvider} off the EDT — streaming each provider's
 * results in as they arrive — and presents them grouped into sections by
 * {@link EntityKind}. A result navigates to its view, or plays if it's a
 * playback URI (e.g. a YouTube video).
 */
public class SearchView extends View {

    private final Label queryLabel;
    private final Panel resultsPanel;
    private final List<SearchResult> all = new ArrayList<>();

    private SwingWorker<Void, List<SearchResult>> worker;
    private int seq;

    public SearchView(ViewStack viewStack) {
        super(viewStack);
        getComponent().setLayout(new BorderLayout(0, 12));

        queryLabel = new Label("Search");
        //queryLabel.setFont(queryLabel.getFont().deriveFont(Font.BOLD, 20f));
        //queryLabel.setForeground(Color.WHITE);
        add(queryLabel, BorderLayout.NORTH);

        resultsPanel = new Panel();
        resultsPanel.getComponent().setLayout(new BoxLayout(resultsPanel.getComponent(), BoxLayout.Y_AXIS));
       
        ScrollPane scroll = new ScrollPane(resultsPanel);
      
         add(scroll, BorderLayout.CENTER);
    }

    @Override
    public boolean acceptsUri(String uri) {
        return uri != null && uri.matches("spacify:search.*");
    }

    @Override
    public void navigate(String uri) {
        String query = "";
        if (uri.contains("?q=")) {
            query = URLDecoder.decode(uri.substring(uri.indexOf("?q=") + 3), StandardCharsets.UTF_8);
        }
        queryLabel.setText(query.isEmpty() ? "Search" : "Results for: " + query);

        // Supersede any in-flight search.
        final int mySeq = ++seq;
        if (worker != null) worker.cancel(true);
        all.clear();
        render();

        if (query.isBlank()) return;

        List<SearchProvider> providers = new ArrayList<>(getMainWindow().getSearchManager().providers());
        final String q = query;
        worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                for (SearchProvider p : providers) {
                    if (isCancelled()) break;
                    try {
                        List<SearchResult> rs = p.search(q);
                        if (rs != null && !rs.isEmpty()) publish(rs);
                    } catch (Exception ignored) { /* one provider failing is fine */ }
                }
                return null;
            }
            @Override protected void process(List<List<SearchResult>> chunks) {
                if (mySeq != seq) return;   // a newer search started
                for (List<SearchResult> rs : chunks) all.addAll(rs);
                render();
            }
        };
        worker.execute();
    }

    /** Rebuild the grouped sections from {@link #all}. */
    private void render() {
        resultsPanel.removeAll();
        all.stream()
           .map(SearchResult::kind)
           .distinct()
           .sorted(Comparator.comparingInt(EntityKind::order))
           .forEach(kind -> {
               resultsPanel.add(sectionHeader(kind.label()));
               for (SearchResult r : all) {
                   if (r.kind() == kind) resultsPanel.add(resultRow(r));
               }
           });
        if (all.isEmpty()) {
            Label none = new Label("No results");
            none.getComponent().setForeground(new Color(150, 150, 150));
            //none.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            resultsPanel.add(none);
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private static Label sectionHeader(String text) {
        Label l = new Label(text);
        l.getComponent().setFont(l.getComponent().getFont().deriveFont(Font.BOLD, 14f));
        l.getComponent().setForeground(new Color(180, 180, 180));
        l.getComponent().setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        l.getComponent().setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        return l;
    }

    private Control<?> resultRow(SearchResult r) {
        String text = "<html>" + escape(r.title())
            + (r.subtitle().isEmpty() ? "" : " <font color='#999999'>— " + escape(r.subtitle()) + "</font>")
            + (r.source().isEmpty() ? "" : "  <font color='#777777'>[" + escape(r.source()) + "]</font>")
            + "</html>";
        Label row = new Label(text);
        row.getComponent().setForeground(new Color(220, 220, 220));
        row.getComponent().setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        row.getComponent().setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        row.getComponent().setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getComponent().getPreferredSize().height + 10));
        if (r.uri() != null && !r.uri().isBlank()) {
            row.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.getComponent().addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { activate(r); }
                @Override public void mouseEntered(MouseEvent e) { row.getComponent().setForeground(ThemeManager.getAccentBackgroundColor()); }
                @Override public void mouseExited(MouseEvent e)  { row.getComponent().setForeground(new Color(220, 220, 220)); }
            });
        }
        return row;
    }

    /** Navigate the result's URI, or play it when it's a playback URI. */
    private void activate(SearchResult r) {
        String uri = r.uri();
        if (uri == null || uri.isBlank()) return;
        if (uri.startsWith("spacify:youtube:") || uri.startsWith("spacify:local:")) {
            PlaybackCoordinator.playUri(uri);
        } else {
            getViewStack().navigate(uri);
        }
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public String getName() { return "Search"; }
}
