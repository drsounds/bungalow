package se.spacify.skinning;

import java.awt.Graphics2D;

import javax.swing.JPanel;
import javax.swing.JTable;

import se.spacify.aspect.Aspect;
import se.spacify.controls.GlassPanel;
import se.spacify.controls.GlossyButton;
import se.spacify.controls.TabButton;
import se.spacify.controls.ToolBar;
import se.spacify.controls.ToolButton;
import se.spacify.controls.VerticalPanel;
import se.spacify.plugin.Plugin;

public abstract class Skin implements Aspect {
	protected Plugin plugin;
	public Plugin getPlugin() {
		return plugin;
	}
	public abstract String getId();
	public abstract void paintTopBar(JPanel control, Graphics2D g2);
	public abstract void paintHeader(JPanel header, Graphics2D g2);
	public abstract void paintFooter(JPanel footer, Graphics2D g2);
	public abstract void paintTabButton(TabButton button, Graphics2D g2);
	public abstract void paintGlossyButton(GlossyButton control, Graphics2D g2, int x, int y, int d);
	public abstract void paintPlaylist(JPanel control, Graphics2D g2);
	public abstract void paintGlassPanel(GlassPanel control, Graphics2D g2);
	public abstract void paintToolBar(ToolBar control, Graphics2D g2);
	public abstract void paintTableHeader(JTable table, int width, int height, Graphics2D g2);
	public abstract void paintToolButton(ToolButton control, Graphics2D g2);
	public abstract void paintVerticalPanel(VerticalPanel verticalPanel, Graphics2D g2);
	public void onRegister(SkinManager manager) {
		
	}
}
