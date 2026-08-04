package se.spacify.app.wmp.chrome;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.controls.Panel;
import se.spacify.controls.SplitPane;

import se.spacify.app.wmp.controls.WMP1XAppHeader;
import se.spacify.ui.AppFooter;
import se.spacify.ui.chrome.Chrome;
import se.spacify.ui.LeftLibraryMenu;
import se.spacify.ui.LeftMenuPanel;
import se.spacify.ui.NowPlayingPanel;
import se.spacify.ui.TopBar;

public class WMP1XChrome extends Chrome {
	
        @Override
        public void build() {
                super.build();
  
                // Core shell views only; content views (library, web, search, playlist)
                // are contributed by built-in plugins during ApplicationManager.start().
        
                leftMenuPanel = new LeftMenuPanel();
                leftMenuPanel.getSwingComponent().setMinimumSize(new Dimension(100, 0));
                leftMenuPanel.getSwingComponent().setMaximumSize(new Dimension(100, Short.MAX_VALUE));
                leftMenuPanel.getSwingComponent().setPreferredSize(new Dimension(100, Short.MAX_VALUE));
                leftMenuPanel.getSwingComponent().setOpaque(false);
                appPanel = new Panel();
                
                leftMenuPanel.setVisible(false);
                add(appPanel);
                appPanel.getSwingComponent().setOpaque(false);
                leftMenuPanel.getSwingComponent().setLayout(new BoxLayout(leftMenuPanel.getSwingComponent(), BoxLayout.PAGE_AXIS));
                appPanel.getSwingComponent().setLayout(new BoxLayout(appPanel.getSwingComponent(), BoxLayout.PAGE_AXIS));
                
                topSpacing = new Panel();
                topSpacing.getSwingComponent().setMinimumSize(new Dimension(0, 1660));
                topSpacing.getSwingComponent().setMaximumSize(new Dimension(Integer.MAX_VALUE, 1660));
                topSpacing.getSwingComponent().setOpaque(false);
                appPanel.add(topSpacing);
                topBar = new TopBar();
                topBar.getSwingComponent().add(new JButton());
                appPanel.add(topBar);
                topBar.getSwingComponent().setMaximumSize(new Dimension(Short.MAX_VALUE, 18));
                topBar.getSwingComponent().setMinimumSize(new Dimension(0, 18));
                // The WMP header carries the nav buttons, the tab strip
                // (Now Playing / Library / Media Guide) and the Stores dropdown.
                appHeader = new WMP1XAppHeader(viewStack);
                appHeader.getSwingComponent().setMaximumSize(new Dimension(Short.MAX_VALUE, 64));
                appHeader.getSwingComponent().setMinimumSize(new Dimension(0, 56));
                appPanel.add(appHeader);

                // Reuse the shared sidebar (owned by MainWindow, populated by plugins) if set.
                if (leftLibraryMenu == null) leftLibraryMenu = new LeftLibraryMenu(viewStack);

                centerPanel = new Panel(new BorderLayout());
                centerPanel.add(viewStack);

                leftSplit = new SplitPane(SplitPane.HORIZONTAL_SPLIT, leftLibraryMenu, centerPanel);
                leftSplit.getComponent().setDividerLocation(220);
                leftSplit.getComponent().setDividerSize(1);
                // Keep the leftLibraryMenu at its width and let the centre view absorb resizes.
                leftSplit.getComponent().setResizeWeight(0.0);
                // Empty (non-UIResource) border survives the Nimbus reinstall in
                // rebuildTheme(); a null border would get a default border re-installed.
                leftSplit.getComponent().setBorder(BorderFactory.createEmptyBorder());
                leftSplit.getComponent().setContinuousLayout(true);

                mainSplit = new SplitPane(SplitPane.HORIZONTAL_SPLIT, leftSplit, new NowPlayingPanel(viewStack));
                mainSplit.getComponent().setDividerLocation(880);
                mainSplit.getComponent().setDividerSize(1);
                // Give all extra width to the left (leftLibraryMenu + centre); the right
                // Now Playing / queue panel keeps its width as the window resizes.
                mainSplit.getComponent().setResizeWeight(1.0);
                mainSplit.getComponent().setBorder(BorderFactory.createEmptyBorder());
                mainSplit.getComponent().setContinuousLayout(true);

                appPanel.add(mainSplit, BorderLayout.CENTER);

                appFooter = new AppFooter();
                appPanel.add(appFooter);
                appFooter.getSwingComponent().setMaximumSize(new Dimension(Short.MAX_VALUE, 18));
                appFooter.getSwingComponent().setMinimumSize(new Dimension(0, 18));
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
