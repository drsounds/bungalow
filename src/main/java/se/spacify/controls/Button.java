package se.spacify.controls;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;

public class Button extends JButton implements Control {
	private boolean primary = false;
	public boolean getPrimary() {
		return primary;
	}
	public void setPrimary(boolean value) {
		primary = value;
	}
	public Button() {

        java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) { repaint(); }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { repaint(); }
        };
        addMouseListener(mouse);
	}
	public Button(String text) {
		super(text);
        java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) { repaint(); }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { repaint(); }
        };
        addMouseListener(mouse);
	}

	public Button(Icon icon) {
		super(icon);
        java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) { repaint(); }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { repaint(); }
        };
        addMouseListener(mouse);
	}
 
	
	public Button(Icon icon, String text) {
		super(icon);
		setText(text);
        java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) { repaint(); }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { repaint(); }
        };
        addMouseListener(mouse);
	}
	public Skin getSkin() {
		return getMainWindow().getSkin();
	}
	public MainWindow getMainWindow() {
		return ((MainWindow)(SwingUtilities.getWindowAncestor(this)));
	}
	private static final long serialVersionUID = 1L;
    private Theme theme;
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
