package se.spacify.controls;

import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Theme;

public interface Control {
	public Skin getSkin();
	public Theme getTheme();
	public MainWindow getMainWindow();
}
