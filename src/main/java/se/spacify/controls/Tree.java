package se.spacify.controls;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JTree;
import javax.swing.SwingUtilities;

import javax.swing.tree.TreeNode;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;
import se.spacify.ui.theme.ThemeManager;

public class Tree extends JTree implements Control {

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        getMainWindow().getSkin().paintTree(this, g2);
        g2.dispose();
        // Selection highlight spanning the full row width (a plain JTree only
        // fills behind the label). The cell renderer paints transparently on top.
        int[] selected = getSelectionRows();
        if (selected != null) {
            g.setColor(ThemeManager.getAccentColor());
            for (int row : selected) {
                Rectangle b = getRowBounds(row);
                if (b != null) g.fillRect(0, b.y, getWidth(), b.height);
            }
        }
        super.paintComponent(g);
    }

    public Tree() {
        super();   
    }
    public Tree(TreeNode root) {
        super(root);
    }
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
    
}
