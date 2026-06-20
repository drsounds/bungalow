package se.spacify.views;

import se.spacify.navigation.View;
import se.spacify.navigation.ViewStack;

import javax.swing.*;
import java.awt.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SearchView extends View {

    private final JPanel panel;
    private final JLabel queryLabel;
    private final JPanel resultsPanel;

    public SearchView(ViewStack viewStack) {
        super(viewStack);
        panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        queryLabel = new JLabel("Search");
        queryLabel.setFont(queryLabel.getFont().deriveFont(Font.BOLD, 20f));
        queryLabel.setForeground(Color.WHITE);
        panel.add(queryLabel, BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);
    }

    @Override
    public boolean acceptsUri(String uri) {
        return uri != null && uri.matches("spacify:search.*");
    }

    @Override
    public void navigate(String uri) {
        String query = "";
        if (uri.contains("?q=")) {
            String raw = uri.substring(uri.indexOf("?q=") + 3);
            query = URLDecoder.decode(raw, StandardCharsets.UTF_8);
        }
        queryLabel.setText(query.isEmpty() ? "Search" : "Results for: " + query);
        resultsPanel.removeAll();

        if (!query.isEmpty()) {
            for (int i = 1; i <= 5; i++) {
                JLabel result = new JLabel("Track " + i + " — matching \"" + query + "\"");
                result.setForeground(new Color(200, 200, 200));
                result.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
                resultsPanel.add(result);
            }
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    @Override
    public JComponent getComponent() { return panel; }

    @Override
    public String getTitle() { return "Search"; }
}
