package se.spacify.ui.chrome;
import se.spacify.aspect.Aspect;
import se.spacify.controls.Panel;
import se.spacify.controls.SplitPane;
import se.spacify.navigation.SPViewStack;
import se.spacify.skinning.Skin;
import se.spacify.ui.AppFooter;
import se.spacify.ui.AppHeader;
import se.spacify.ui.LeftLibraryMenu;
import se.spacify.ui.LeftMenuPanel;

import se.spacify.ui.TopBar;
import se.spacify.views.NowPlayingView;
	
public abstract class Chrome extends Panel implements Aspect {
	
    private static final long serialVersionUID = 2144395787232553079L;
	public void onDestroy() {
		
	}
	protected SPViewStack    viewStack; 
	public void setViewStack(SPViewStack value) {
		viewStack = value;
	} 
    protected AppFooter      appFooter;
    protected NowPlayingView nowPlayingView;
    protected LeftLibraryMenu  leftLibraryMenu;
	public void setLeftLibraryMenu(LeftLibraryMenu value) {
		leftLibraryMenu = value;
	}

    public SPViewStack    getViewStack()     { return viewStack; }
    public AppFooter      getAppFooter()     { return appFooter; }
    public NowPlayingView getNowPlayingView() { return nowPlayingView; }
    public LeftLibraryMenu        getLeftLibraryMenu()       { return leftLibraryMenu; }
  
	protected SplitPane leftSplit;
	public SplitPane getLeftSplit() {
		return leftSplit;
	}
	protected SplitPane mainSplit;
	public SplitPane getMainSplit() {
		return mainSplit;
	}
    protected boolean userWantsSidebar = true;  // user's manual show/hide preference
    protected boolean immersive = false;        // full-width store browsing

	protected Panel appPanel;
	public Panel getAppPanel() {
		return appPanel;
	}
	public void setAppPanel(Panel value) {
		appPanel = value;
	}

	protected LeftMenuPanel leftMenuPanel;
	public LeftMenuPanel getLeftMenuPanel() {
		return leftMenuPanel;
	}
	public void setLeftMenuPanel(LeftMenuPanel value) {
		leftMenuPanel = value;
	}

	protected Panel centerPanel; 
	public Panel getCenterPanel() {
		return  centerPanel;
	}
	public void setCenterPanel(Panel value) {
		centerPanel = value;
	}
	protected TopBar topBar;
	public TopBar getTopBar() {
		return topBar;
	}
	public void setTopBar(TopBar value) {
		topBar = value;
	}

	protected AppHeader appHeader;

	public AppHeader getAppHeader() {
		return appHeader;
	}

	protected Panel topSpacing;
	public Panel getTopSpacing() {
		return topSpacing;
	}

    public Chrome() {
		viewStack = new SPViewStack();
		nowPlayingView = new NowPlayingView(viewStack);
		// Core shell views only; content views (library, web, search, playlist)
		// are contributed by built-in plugins during PluginManager.start().
		viewStack.registerView(nowPlayingView);
		viewStack.registerView(new se.spacify.plugin.ui.PluginManagerView(viewStack));
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

    public void applySidebar(boolean visible) {
        leftLibraryMenu.setVisible(visible);
        leftSplit.setDividerLocation(visible ? 220 : 0);
        leftSplit.revalidate();
        leftSplit.repaint();
    }

    /** Full-width browsing: collapse both side panels for store views. */
    public void applyImmersive(boolean on) {
        if (on == immersive) return;
        immersive = on;
        if (on) {
            applySidebar(false);
            mainSplit.setDividerLocation(1.0);   // collapse the right Now Playing panel
        } else {
            applySidebar(userWantsSidebar);
            mainSplit.setDividerLocation(880);
        }
        mainSplit.revalidate();
        mainSplit.repaint();
    }

    public void navigate(String uri) {
    	viewStack.navigate(uri);
    } 

}
