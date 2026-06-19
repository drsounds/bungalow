package se.spacify.controls;

import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Theme;

public class ToggleButton extends JToggleButton implements Control {
	private static final long serialVersionUID = 1L;

	public Skin getSkin() {
		return getMainWindow().getSkin();
	}

	public ToggleButton() {
		super();

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
	public ToggleButton(String text) {
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
	
	public MainWindow getMainWindow() {
		return ((MainWindow)(SwingUtilities.getWindowAncestor(this)));
	}

    @Override
    public Theme getTheme() {
        // TODO Auto-generated method stub
        return getMainWindow().getTheme();
    }
}
