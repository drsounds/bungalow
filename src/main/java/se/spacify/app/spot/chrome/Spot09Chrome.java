package se.spacify.app.spot.chrome;

import java.awt.BorderLayout;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.controls.Panel;
import se.spacify.controls.SplitPane;
import se.spacify.app.spot.controls.Spot09AppFooter;
import se.spacify.app.spot.controls.Spot09AppHeader;

import se.spacify.ui.chrome.Chrome;

import se.spacify.ui.LeftLibraryMenu;
import se.spacify.ui.LeftMenuPanel;
import se.spacify.ui.NowPlayingPanel;

public class Spot09Chrome extends Chrome {
        private static final long serialVersionUID = 2766717544747588265L;

        @Override
        public String getId() {
                // TODO Auto-generated method stub
                return "spot09";
        }
        @Override
        public void onRegister(AspectManager<? extends Aspect> aspectManager) {
                // TODO Auto-generated method stub
        }
        @Override
        public void build() {
                super.build();
                setLayout(new BoxLayout(getComponent(), BoxLayout.PAGE_AXIS));
                
                appHeader = new Spot09AppHeader(viewStack);
                appHeader.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
                appHeader.setMinimumSize(new Dimension(0, 28));
                add(appHeader);
                leftMenuPanel = new LeftMenuPanel();
                leftMenuPanel.setMinimumSize(new Dimension(100, 0));
                leftMenuPanel.setMaximumSize(new Dimension(100, Short.MAX_VALUE));
                leftMenuPanel.setPreferredSize(new Dimension(100, Short.MAX_VALUE));
                leftMenuPanel.setOpaque(false);
                appPanel = new Panel();

                add(leftMenuPanel);
                leftMenuPanel.setVisible(false);
                add(appPanel);
                appPanel.setOpaque(false);
                leftMenuPanel.setLayout(new BoxLayout(leftMenuPanel.getComponent(), BoxLayout.PAGE_AXIS));
                appPanel.setLayout(new BoxLayout(appPanel.getComponent(), BoxLayout.PAGE_AXIS));

                // Reuse the shared sidebar (owned by MainWindow, populated by plugins) if set.
                if (leftLibraryMenu == null) leftLibraryMenu = new LeftLibraryMenu(viewStack);
                leftLibraryMenu.setOpaque(false);
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

                appFooter = new Spot09AppFooter();
                appPanel.add(appFooter);
                appFooter.setMaximumSize(new Dimension(Short.MAX_VALUE, 18));
                appFooter.setMinimumSize(new Dimension(0, 18));

        }
}
