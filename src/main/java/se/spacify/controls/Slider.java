package se.spacify.controls;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;

public class Slider extends JSlider implements Control {
    public Slider(int x, int y, int z) {
        super(x, y, z);
    }
    public Slider() {
        
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
