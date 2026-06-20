package se.spacify.views;

import se.spacify.navigation.View;
import se.spacify.navigation.ViewStack;

import javax.swing.*;
import java.awt.*;

public class HomeView extends View {

    private final JPanel panel;

    public HomeView(ViewStack viewStack) {
        super(viewStack);
        panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel label = new JLabel("Home", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);
    }

    @Override
    public boolean acceptsUri(String uri) {
        return uri != null && uri.matches("spacify:(home|//)");
    }

    @Override
    public void navigate(String uri) {}

    @Override
    public JComponent getComponent() { return panel; }

    @Override
    public String getTitle() { return "Home"; }
}
