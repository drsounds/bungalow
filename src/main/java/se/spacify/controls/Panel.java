package se.spacify.controls;

import java.awt.LayoutManager;
import java.awt.LayoutManager2;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;

public class Panel extends JPanel implements Control {
	private static final long serialVersionUID = 1L;
	
	public Panel() {
		super();
	}
	public Panel(LayoutManager layout) {
		super();
	}
	public Panel(LayoutManager2 layout) {
		super(layout);
	}
 
	private Theme theme;
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	public void setDesign(Design design) {
		this.design = design;
	}
	private Skin skin;
	public void setSkin(Skin skin) {
		this.skin = skin;
	}
	public Skin getSkin() {
		if (skin != null) {
			return skin;
		}
		if (getParent() != null && getParent() instanceof Panel) {
			if (((Panel)getParent()).getSkin() != null) {
				return ((Panel)getParent()).getSkin();
			}		
		}
		return getMainWindow().getSkin();
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

	public MainWindow getMainWindow() {
		return ((MainWindow)(SwingUtilities.getWindowAncestor(this)));
	}
}
