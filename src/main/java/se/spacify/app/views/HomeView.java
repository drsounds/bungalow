package se.spacify.app.views;

import se.spacify.navigation.View;
import se.spacify.navigation.ViewStack;

import javax.swing.*;
import java.awt.*;

public class HomeView extends View {


    public HomeView(ViewStack viewStack) {
        super(viewStack);
        getSwingComponent().setLayout(new BorderLayout());
        getSwingComponent().setOpaque(false);

        JLabel label = new JLabel("Home", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        label.setForeground(Color.WHITE);
        getSwingComponent().add(label, BorderLayout.CENTER);
    }

    @Override
    public boolean acceptsUri(String uri) {
        return uri != null && uri.matches("spacify:(home|//)");
    }   

    @Override
    public void navigate(String uri) {}


    @Override
    public String getName() { return "Home"; }
}
