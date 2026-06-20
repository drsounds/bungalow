package se.spacify.ui;

import se.spacify.controls.GlassPanel;
import se.spacify.controls.GlossyButton;
import se.spacify.service.media.PlaybackCoordinator;
import se.spacify.service.media.PlayQueue;
import se.spacify.ui.theme.ThemeManager;

import se.spacify.controls.Slider;
import se.spacify.plugin.media.service.MediaService;
import se.spacify.plugin.media.service.MediaService.PlaybackState;
import se.spacify.controls.Panel;

import javax.swing.*;
import java.awt.*;

public class AppFooter extends JPanel {

    private static Color HIGHLIGHT = new Color(255, 255, 255, 35);

    // Fields exposed for Service wiring
    protected JLabel  trackNameLabel;
    protected JLabel  artistLabel;
    protected GlossyButton playPauseBtn;
	protected Panel mainBar;
	protected GlassPanel leftPanel;
	protected Panel controls;
	protected Panel buttons;
	protected GlassPanel rightPanel;
	protected Slider progress;
	protected Panel progressPanel;
	protected GlossyButton backwardButton;
	protected GlossyButton forwardButton;

    public AppFooter() {
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setOpaque(true);
        this.build();
    }
    public void build() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setPreferredSize(new Dimension(0, 90));
  
        progressPanel = new Panel(new BorderLayout());
        progressPanel.setMinimumSize(new Dimension(0, 18));
        progressPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 18));
        progressPanel.setOpaque(false);
        add(progressPanel);
       
        backwardButton = makeControlButton("⏪");
        progressPanel.add(backwardButton, BorderLayout.WEST);
        progress = new Slider(0, 1000, 0);
        progress.setOpaque(false); 
        progressPanel.add(progress, BorderLayout.CENTER);
        forwardButton = makeControlButton("⏩");
        progressPanel.add(forwardButton, BorderLayout.EAST);
        mainBar = new Panel();
        mainBar.setLayout(new BoxLayout(mainBar, BoxLayout.LINE_AXIS));
        mainBar.setPreferredSize(new Dimension(0, 28));
        mainBar.setOpaque(false);
        add(mainBar);
        
        // Left: track info
        leftPanel = new GlassPanel();
		leftPanel.setLayout(new GridLayout(2, 1, 0, 2));
		leftPanel.setOpaque(false);
		leftPanel.setPreferredSize(new Dimension(200, 0));
		leftPanel.setTrailingDiagonal(true);   // sharp left edge, bottom longer than top
        leftPanel.setDiagonalInset(65);
        leftPanel.setPreferredSize(new Dimension(160, 0));
        trackNameLabel = new JLabel("No track playing");
        trackNameLabel.setForeground(Color.WHITE);
        trackNameLabel.setFont(trackNameLabel.getFont().deriveFont(Font.BOLD, 13f));
        artistLabel = new JLabel("");
        artistLabel.setForeground(new Color(180, 180, 180));
        artistLabel.setFont(artistLabel.getFont().deriveFont(11f));
        leftPanel.add(trackNameLabel);
        leftPanel.add(artistLabel);

        // Center: playback controls + progress
        controls = new Panel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setOpaque(false);

        buttons = new Panel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttons.setOpaque(false);
        GlossyButton prevBtn = makeControlButton("⏮");
        prevBtn.addActionListener(e -> PlayQueue.getInstance().previous());
        buttons.add(prevBtn);
        playPauseBtn = makeControlButton("▶");
        playPauseBtn.setFont(playPauseBtn.getFont().deriveFont(16f));
        playPauseBtn.setPrimary(true);
        playPauseBtn.setDiameter(48);
        buttons.add(playPauseBtn);
        JButton nextBtn = makeControlButton("⏭");
        nextBtn.addActionListener(e -> PlayQueue.getInstance().next());
        buttons.add(nextBtn);

        // Transport acts on whichever Service is currently active, regardless of
        // which one is playing. Wired once here; per-Service event observation is
        // added separately via setMediaService.
        playPauseBtn.addActionListener(e -> {
            MediaService active = PlaybackCoordinator.getActiveService();
            if (active == null) return;
            if (active.getPlaybackState() == PlaybackState.PLAYING) active.pause();
            else active.play();
        });

        controls.add(buttons);
        controls.add(Box.createVerticalStrut(4));
       
        // Right: volume
        //right.add(searchField);
        rightPanel = new GlassPanel();
        rightPanel.setPreferredSize(new Dimension(300, 16));
        rightPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        rightPanel.setLeadingDiagonal(true);   // sharp left edge, bottom longer than top
        rightPanel.setDiagonalInset(65); 
        JLabel volIcon = new JLabel("🔊");
        volIcon.setForeground(Color.WHITE);
        JSlider volume = new JSlider(0, 100, 70);
        volume.setPreferredSize(new Dimension(100, 20));
        volume.setOpaque(false);
        controls.add(volIcon);
        controls.add(volume);

        mainBar.add(leftPanel);
        mainBar.add(controls);
        mainBar.add(rightPanel);
        ThemeManager.addChangeListener(this::repaint);

    }
    private LayoutMode layoutMode = LayoutMode.WMP10;
    public LayoutMode getLayoutMode() {
    	return layoutMode;
    }
    public void setLayoutMode(LayoutMode value) {
    	layoutMode = value;
    	if (value == LayoutMode.WMP11) {
    		leftPanel.setVisible(true);
    	}
    	if (value == LayoutMode.WMP10) {
    		leftPanel.setVisible(false);
    	}
    	if (value == LayoutMode.WMP9) {
    		leftPanel.setVisible(false);
    	}
    }

    /**
     * Observe a MediaService so its playback events update the bar's labels and
     * play/pause state. May be called for several Services; transport controls
     * are wired once (in the constructor) and act on the active Service. So that
     * only the active Service drives the labels, events from a non-active Service
     * are ignored.
     */
    public void setMediaService(MediaService ms) {
        ms.addPlaybackListener(new MediaService.PlaybackListener() {
            private boolean active() { return PlaybackCoordinator.getActiveService() == ms; }

            @Override
            public void onStateChanged(PlaybackState state) {
                if (!active()) return;
                SwingUtilities.invokeLater(() ->
                    playPauseBtn.setText(state == PlaybackState.PLAYING ? "⏸" : "▶"));
            }

            @Override
            public void onPositionChanged(long posMs, long durMs) {
                if (!active() || durMs <= 0) return;
                int value = (int) Math.round(1000.0 * posMs / durMs);
                SwingUtilities.invokeLater(() -> progress.setValue(value));
            }

            @Override
            public void onTrackChanged(String title, String artist, String album) {
                if (!active()) return;
                SwingUtilities.invokeLater(() -> {
                    trackNameLabel.setText(title  != null ? title  : "");
                    artistLabel.setText(artist != null ? artist : "");
                });
            }

            @Override
            public void onError(Exception e) {
                if (!active()) return;
                SwingUtilities.invokeLater(() -> trackNameLabel.setText("Error: " + e.getMessage()));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();

        ((MainWindow)(SwingUtilities.getWindowAncestor(this))).getSkin().paintFooter(this, g2);
        
        g2.setColor(HIGHLIGHT);
        g2.drawLine(0, h - 1, w, h - 1);
        g2.dispose();
    }

    protected GlossyButton makeControlButton(String text) {
        GlossyButton btn = new GlossyButton(text);
        btn.setFocusPainted(false);
        btn.setDiameter(36);
        btn.setFont(btn.getFont().deriveFont(14f));
        return btn;
    }
}
