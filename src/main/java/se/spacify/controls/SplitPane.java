package se.spacify.controls;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;
import se.spacify.ui.theme.ThemeManager;

public class SplitPane extends JSplitPane implements Control {

	public Skin getSkin() {
		return getMainWindow().getSkin();
	}
	private Theme theme;
	public MainWindow getMainWindow() {
		// While a panel is still being constructed it has no window ancestor yet,
		// so fall back to the live MainWindow so theme/skin/taste stay resolvable.
		java.awt.Window w = SwingUtilities.getWindowAncestor(this);
		if (w instanceof MainWindow) return (MainWindow) w;
		return MainWindow.getInstance();
	}
	public Theme getTheme() {
		if (theme != null) {
			return theme;
		}
		if (getParent() != null && getParent() instanceof Panel) {
			if (((Panel)getParent()).getTheme() != null) {
				return ((Panel)getParent()).getTheme();
			}		
		}
		return getMainWindow().getTheme();
	}
	private Design design;
	public Design getDesign() {
		if (design != null) {
			return design;
		}
		if (getParent() != null && getParent() instanceof Panel) {
			if (((Panel)getParent()).getDesign() != null) {
				return ((Panel)getParent()).getDesign();
			}		
		}
		return getMainWindow().getDesign();
	}
	private Taste taste;
	public Taste getTaste() {
		if (taste != null) {
			return taste;
		}
		if (getParent() != null && getParent() instanceof Panel) {
			if (((Panel)getParent()).getTaste() != null) {
				return ((Panel)getParent()).getTaste();
			}
		}
		return getMainWindow().getTaste();
	}
	private class SplitPaneDivider extends BasicSplitPaneDivider {
        public SplitPaneDivider( BasicSplitPaneUI ui ) {
            super( ui );
            super.setBorder( null );
        }

        @Override
        public void setBorder( Border border ) {
            // ignore
        }

        @Override
        public void paint( Graphics g ) {
        	Graphics2D g2 = (Graphics2D)g.create();
    		int w = getWidth(), h = getHeight();
    		if (ThemeManager.isDarkMode()) {
    			g2.setPaint(new Color(235, 234, 219));
    			g2.fillRect(0, 0, w, h);
    		}
    		g2.dispose();
        }

        @Override
        protected void dragDividerTo( int location ) {
            super.dragDividerTo( location );
        }

        @Override
        protected void finishDraggingTo( int location ) {
            super.finishDraggingTo( location );
        }
    }
    private class SplitPaneDividerUI extends BasicSplitPaneUI {
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new SplitPaneDivider( this );
        }
    }
	private static final long serialVersionUID = -5587739050405239941L;
	public SplitPane() {
		
		getDivider().setBorder(new EmptyBorder(0, 0, 0, 0));
	}
	public BasicSplitPaneDivider getDivider() {
		if (this.getUI() instanceof BasicSplitPaneUI) {
            return ((BasicSplitPaneUI)(this.getUI())).getDivider();
        }
		return null;
	}
	public SplitPane(int horizontalSplit, JComponent leftSplit, JComponent c) {
		super(horizontalSplit, leftSplit, c);
		setUI(new SplitPaneDividerUI());
	}
	@Override
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
	}
}
