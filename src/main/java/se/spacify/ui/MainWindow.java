package se.spacify.ui;

import se.spacify.controls.SplitPane;
import se.spacify.design.Design;
import se.spacify.design.DesignManager;
import se.spacify.feature.FeatureManager;
import se.spacify.navigation.SPViewStack;
import se.spacify.plugin.PluginManager;

import se.spacify.service.ServiceManager;
import se.spacify.service.media.MediaService;
import se.spacify.service.media.PlayQueue;
import se.spacify.concept.ConceptManager;
import se.spacify.config.ConfigManager;
import se.spacify.controls.Panel;
import se.spacify.skinning.Skin;
import se.spacify.skinning.SkinManager;
import se.spacify.ui.chrome.Chrome;
import se.spacify.ui.chrome.ChromeManager;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;
import se.spacify.ui.theme.ThemeManager;
import se.spacify.views.*;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private static final long serialVersionUID = 2144395787232553079L;

    private Taste taste;

    private ConceptManager conceptManager;
    public ConceptManager getConceptManager() {
        return conceptManager;
    }


    private ThemeManager themeManager;
    public ThemeManager getThemeManager() {
        return themeManager;
    }

    private ChromeManager chromeManager;
    public ChromeManager getChromeManager() {
        return chromeManager;
    }
    private DesignManager designManager;
    public DesignManager getDesignManager() {
        return designManager;
    }
    private FeatureManager featureManager;
    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    private ServiceManager serviceManager;
    public ServiceManager getServiceManager() {
        return serviceManager;
    }
    private PluginManager pluginManager;
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }
    private SkinManager skinManager;

    public SkinManager getSkinManager() {
        return skinManager;
    }

    public void setSkinManager(SkinManager skinManager) {
        this.skinManager = skinManager;
    }

    public Taste getTaste() {
        return taste;
    }

    public void setTaste(Taste taste) {
        if (getChrome() != null) {
            remove(getChrome()); 
        }
        this.taste = taste;
        add(getChrome(), BorderLayout.CENTER);
    }

    public Theme getTheme() {
        return getTaste().getTheme();
    }
    
    public Design getDesign() {
        return getTaste().getDesign();
    }
    public void setDesign(Design design) {
        getTaste().setDesign(design);
        setSkin(design.getSkin());
    }
     
    public Skin getSkin() {
        return getTaste().getSkin();
    }

    public void setSkin(Skin skin) {
        this.getTaste().setSkin(skin);
    }
    private boolean userWantsSidebar = true;  // user's manual show/hide preference
    private boolean immersive = false;        // full-width store browsing

    private ConfigManager config;
    public Chrome getChrome() {
        if (getTaste() == null) {
            return null;
        }
        return getTaste().getChrome();
    }
	public Panel getAppPanel() {
		return getChrome().getAppPanel();
	} 

	public LeftMenuPanel getLeftMenuPanel() {
		return getChrome().getLeftMenuPanel();
	}

	public Panel getCenterPanel() {
		return getChrome().getCenterPanel();
	}
 
	public TopBar getTopBar() {
		return getChrome().getTopBar();
	} 
	public SplitPane getLeftSplit() {
		return getChrome().getLeftSplit();
	}
	public SplitPane getMainSplit() {
		return getChrome().getMainSplit();
	}
	public AppHeader getAppHeader() {
		return getChrome().getAppHeader();
	} 
    public void setTheme(Theme theme) { 
        getTaste().setTheme(theme);
        rebuildTheme();
    }
    public MainWindow() {
        super("Spacify");
        themeManager = new ThemeManager(this);
        config = new ConfigManager(this);
        config.load();
        serviceManager = new ServiceManager(this);
        conceptManager = new ConceptManager(this);
        serviceManager.startAll();
        pluginManager = new PluginManager(this);
        chromeManager = new ChromeManager(this);
        designManager = new DesignManager(this);
        featureManager = new FeatureManager(this);
        skinManager = new SkinManager(this);
        setUndecorated(true);  // remove native title bar + border on all platforms
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        // 1px border so the window edge is visible against the desktop
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 1));

        // ── Theme ─────────────────────────────────────────────────────────────
        getTaste().addChangeListener(config::save);

        Timer themeRebuildTimer = new Timer(250, e -> rebuildTheme());
        themeRebuildTimer.setRepeats(false);
        getTaste().addChangeListener(themeRebuildTimer::restart);

        // Swap the active Service when the selected design style changes.
        getTaste().addChangeListener(() -> {
            
        });

        featureManager.activateFeatures(getViewStack(), getLeftLibraryMenu().getRootNode());

        // Discover and activate plugins (built-in bundle, <app>/plugins, ~/Bungalow).
        // The Local Music plugin registers the media Service, so wire it afterwards.
        pluginManager.init(getViewStack(), getLeftLibraryMenu());
        pluginManager.start();

        // Wire every registered media Service for events; the footer/queue follow
        // whichever one PlaybackCoordinator marks active for the current play.
        for (MediaService ms : serviceManager.getServices(MediaService.class)) {
            wireMediaService(ms);
        }

        applySidebar(false);
        // Glass-pane resize handler — intercepts edge events, redispatches others
        WindowResizer.install(this);
        
        // Store pages browse full-width with the side panels collapsed.
        getChrome().getViewStack().addNavigationListener((uri, b, f) ->
            applyImmersive(uri != null && uri.startsWith("spacify:store:")));
        navigate("spacify:now-playing");
    }

    /** Toggle the left leftLibraryMenu; remembers the user's preference. */
    public void toggleSidebar() {
        setSidebarVisible(!userWantsSidebar);
    }

    /** Explicitly show/hide the left leftLibraryMenu; remembers the user's preference. */
    public void setSidebarVisible(boolean visible) {
        userWantsSidebar = visible;
        if (!immersive) applySidebar(visible);
    }

    private void applySidebar(boolean visible) {
        getLeftLibraryMenu().setVisible(visible);
        getLeftSplit().setDividerLocation(visible ? 220 : 0);
        getLeftSplit().revalidate();
        getLeftSplit().repaint();
    }

    /** Full-width browsing: collapse both side panels for store views. */
    private void applyImmersive(boolean on) {
        if (on == immersive) return;
        immersive = on;
        if (on) {
            applySidebar(false);
            getMainSplit().setDividerLocation(1.0);   // collapse the right Now Playing panel
        } else {
            applySidebar(userWantsSidebar);
            getMainSplit().setDividerLocation(880);
        }
        getMainSplit().revalidate();
        getMainSplit().repaint();
    }

    public void navigate(String uri) {
    	getViewStack().navigate(uri);
    }

    private void rebuildTheme() {
        // 1. Reinstall Nimbus to clear its SynthStyleFactory painter cache
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
        // 2. Re-apply our colour overrides to the freshly-installed L&F defaults
        ThemeManager.applyToDefaults();
        // 3. Propagate to all components
        SwingUtilities.updateComponentTreeUI(this);
    }

    /** Connects a MediaService to AppFooter and NowPlayingView. */
    public void wireMediaService(MediaService ms) {
        getAppFooter().setMediaService(ms);
        getNowPlayingView().setMediaService(ms);
        // Auto-advance the play queue when a track reaches its natural end.
        ms.addPlaybackListener(new MediaService.PlaybackListener() {
            @Override public void onCompleted() {
                SwingUtilities.invokeLater(() -> PlayQueue.getInstance().next());
            }
        });
    }

    public SPViewStack    getViewStack()     { return getChrome().getViewStack(); }
    public AppFooter      getAppFooter()     { return getChrome().getAppFooter(); }
    public NowPlayingView getNowPlayingView() { return getChrome().getNowPlayingView(); }
    public LeftLibraryMenu        getLeftLibraryMenu()       { return getChrome().getLeftLibraryMenu(); }
}
