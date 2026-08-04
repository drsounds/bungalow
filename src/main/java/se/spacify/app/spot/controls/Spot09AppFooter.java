package se.spacify.app.spot.controls;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;

import se.spacify.controls.GlossyButton;
import se.spacify.controls.Panel;
import se.spacify.controls.Slider;
import se.spacify.service.media.PlayQueue;
import se.spacify.ui.AppFooter;

public class Spot09AppFooter extends AppFooter {
    @Override
    public void build() {

        getSwingComponent().setLayout(new BoxLayout(getSwingComponent(), BoxLayout.LINE_AXIS));

        buttons = new Panel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttons.getSwingComponent().setOpaque(false);
        backwardButton = makeControlButton("⏪");
        GlossyButton prevBtn = makeControlButton("⏮");
        prevBtn.getSwingComponent().addActionListener(e -> PlayQueue.getInstance().previous());
        buttons.add(prevBtn);
        playPauseBtn = makeControlButton("▶");
        playPauseBtn.getSwingComponent().setFont(playPauseBtn.getSwingComponent().getFont().deriveFont(16f));
        playPauseBtn.setPrimary(true);
        playPauseBtn.setDiameter(28);
        buttons.add(playPauseBtn);
        GlossyButton nextBtn = makeControlButton("⏭");
        nextBtn.getSwingComponent().addActionListener(e -> PlayQueue.getInstance().next());
        buttons.add(nextBtn);
        add(buttons);

        progressPanel = new Panel(new BorderLayout());
        progressPanel.getSwingComponent().setOpaque(false); 
        progress = new Slider(0,  5, 0);
        add(progressPanel);
        progressPanel.add(progress);
        progress.getComponent().setOpaque(false); 

    }
}

