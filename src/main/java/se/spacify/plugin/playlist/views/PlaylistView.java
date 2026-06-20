package se.spacify.plugin.playlist.views;

import se.spacify.navigation.View;
import se.spacify.navigation.ViewStack;

import javax.swing.*;
import java.awt.*;

public class PlaylistView extends View {

    private final JPanel panel;
    private final JLabel titleLabel;

    public PlaylistView(ViewStack viewStack) {
        super(viewStack);
        panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        titleLabel = new JLabel("Playlist");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel placeholder = new JLabel("No tracks yet.", SwingConstants.CENTER);
        placeholder.setForeground(new Color(160, 160, 160));
        panel.add(placeholder, BorderLayout.CENTER);
    }

    @Override
    public boolean acceptsUri(String uri) {
        return uri != null && uri.matches("spacify:playlist:.*");
    }

    @Override
    public void navigate(String uri) {
        String id = uri.replaceFirst("spacify:playlist:", "");
        titleLabel.setText("Playlist: " + id);
    }

    @Override
    public JComponent getComponent() { return panel; }

    @Override
    public String getTitle() { return "Playlist"; }
}
