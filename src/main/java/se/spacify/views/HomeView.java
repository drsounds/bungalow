package se.spacify.views;

import se.spacify.navigation.View;
import se.spacify.navigation.ViewStack;

import javax.swing.*;
import java.awt.*;

public class HomeView extends View {


    public HomeView(ViewStack viewStack) {
        super(viewStack);
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel label = new JLabel("Home", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        label.setForeground(Color.WHITE);
        add(label, BorderLayout.CENTER);
    }

    @Override
    public boolean acceptsUri(String uri) {
        return uri != null && uri.matches("spacify:(home|//)");
    }

    @Override
    public void navigate(String uri) {}


    @Override
    public String getTitle() { return "Home"; }
}
