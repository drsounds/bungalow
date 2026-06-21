package se.spacify.plugin.wmp.chrome;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.controls.Panel;
import se.spacify.controls.SplitPane;

import se.spacify.plugin.wmp.controls.WMP1XAppHeader;
import se.spacify.ui.AppFooter;
import se.spacify.ui.chrome.Chrome;
import se.spacify.ui.LeftLibraryMenu;
import se.spacify.ui.LeftMenuPanel;
import se.spacify.ui.NowPlayingPanel;
import se.spacify.ui.TopBar;

public class WMP1XChrome extends Chrome {

	private static final long serialVersionUID = 1884115920556166687L;
	
        @Override
        public void build() {
                super.build();
  
                // Core shell views only; content views (library, web, search, playlist)
                // are contributed by built-in plugins during PluginManager.start().
        
                leftMenuPanel = new LeftMenuPanel();
                leftMenuPanel.setMinimumSize(new Dimension(100, 0));
                leftMenuPanel.setMaximumSize(new Dimension(100, Short.MAX_VALUE));
                leftMenuPanel.setPreferredSize(new Dimension(100, Short.MAX_VALUE));
                leftMenuPanel.setOpaque(false);
                appPanel = new Panel();
                
                leftMenuPanel.setVisible(false);
                add(appPanel);
                appPanel.setOpaque(false);
                leftMenuPanel.setLayout(new BoxLayout(leftMenuPanel, BoxLayout.PAGE_AXIS));
                appPanel.setLayout(new BoxLayout(appPanel, BoxLayout.PAGE_AXIS));
                
                topSpacing = new Panel();
                topSpacing.setMinimumSize(new Dimension(0, 1660));
                topSpacing.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1660));
                topSpacing.setOpaque(false);
                appPanel.add(topSpacing);
                topBar = new TopBar();
                topBar.add(new JButton());
                appPanel.add(topBar);
                topBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 18));
                topBar.setMinimumSize(new Dimension(0, 18));
                // The WMP header carries the nav buttons, the tab strip
                // (Now Playing / Library / Media Guide) and the Stores dropdown.
                appHeader = new WMP1XAppHeader(viewStack);
                appHeader.setMaximumSize(new Dimension(Short.MAX_VALUE, 64));
                appHeader.setMinimumSize(new Dimension(0, 56));
                appPanel.add(appHeader);

                // Reuse the shared sidebar (owned by MainWindow, populated by plugins) if set.
                if (leftLibraryMenu == null) leftLibraryMenu = new LeftLibraryMenu(viewStack);

                centerPanel = new Panel(new BorderLayout());
                centerPanel.add(viewStack);

                leftSplit = new SplitPane(SplitPane.HORIZONTAL_SPLIT, leftLibraryMenu, centerPanel);
                leftSplit.setDividerLocation(220);
                leftSplit.setDividerSize(1);
                // Keep the leftLibraryMenu at its width and let the centre view absorb resizes.
                leftSplit.setResizeWeight(0.0);
                // Empty (non-UIResource) border survives the Nimbus reinstall in
                // rebuildTheme(); a null border would get a default border re-installed.
                leftSplit.setBorder(BorderFactory.createEmptyBorder());
                leftSplit.setContinuousLayout(true);

                mainSplit = new SplitPane(SplitPane.HORIZONTAL_SPLIT, leftSplit, new NowPlayingPanel(viewStack));
                mainSplit.setDividerLocation(880);
                mainSplit.setDividerSize(1);
                // Give all extra width to the left (leftLibraryMenu + centre); the right
                // Now Playing / queue panel keeps its width as the window resizes.
                mainSplit.setResizeWeight(1.0);
                mainSplit.setBorder(BorderFactory.createEmptyBorder());
                mainSplit.setContinuousLayout(true);

                appPanel.add(mainSplit, BorderLayout.CENTER);

                appFooter = new AppFooter();
                appPanel.add(appFooter);
                appFooter.setMaximumSize(new Dimension(Short.MAX_VALUE, 18));
                appFooter.setMinimumSize(new Dimension(0, 18));
	}

        @Override
        public String getId() {
                // TODO Auto-generated method stub
                return "wmp10";
        }
        @Override
        public void onRegister(AspectManager<? extends Aspect> aspectManager) {
                // TODO Auto-generated method stub
        }
}
