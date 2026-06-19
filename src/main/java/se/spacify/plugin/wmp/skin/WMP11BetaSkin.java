package se.spacify.plugin.wmp.skin;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JTable;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.controls.GlassPanel;
import se.spacify.controls.GlossyButton;
import se.spacify.controls.TabButton;
import se.spacify.controls.ToolBar;
import se.spacify.controls.ToolButton;
import se.spacify.controls.VerticalPanel;
import se.spacify.skinning.Skin;
import se.spacify.ui.theme.ColorUtils;
import se.spacify.ui.theme.ThemeManager;

public class WMP11BetaSkin extends Skin {
	@Override
	public void paintHeader(JPanel control, Graphics2D g2) {
        int w = control.getWidth(), h = control.getHeight();
		g2.setPaint(ThemeManager.getAccentColor());
		g2.fillRect(0, 0, w, h);
		
		g2.setPaint(new GradientPaint(0, 0, ThemeManager.accentLight(2f), 0, h, new Color(255, 255, 255 ,0)));
		g2.fillRect(0, 0, w, h / 2);
	
		g2.setPaint(new GradientPaint(0, 0, ThemeManager.accentLight(2f), 0, h, new Color(255, 255, 255, 0)));
		g2.fillRect(0, h / 2, w, (h / 2));
	}

	@Override
	public void paintTopBar(JPanel control, Graphics2D g2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paintFooter(JPanel footer, Graphics2D g2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paintTabButton(TabButton button, Graphics2D g2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paintGlossyButton(GlossyButton control, Graphics2D g2, int x, int y, int d) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void paintPlaylist(JPanel control, Graphics2D g2) {
		// TODO Auto-generated method stub
		int w = control.getWidth(), h = control.getHeight();
        Color tintColor = ThemeManager.getTintColor();
        g2.setPaint(new GradientPaint(0, 0, tintColor, 0, h, ThemeManager.accentLight(2f)));
        g2.fillRect(0, 0, w, h);
	}
	@Override
	public void paintToolBar(ToolBar control, Graphics2D g2) {
		int w = control.getWidth(), h = control.getHeight();
		g2.setPaint(ThemeManager.getTintColor());
		g2.fillRect(0, 0, w, h);
	}

	@Override
	public void paintToolButton(ToolButton control, Graphics2D g2) {
		int w = control.getWidth(), h = control.getHeight();
		Color background = ThemeManager.getTintColor();
        ButtonModel model = control.getModel();
		g2.setPaint(background);
		g2.fillRect(0, 0, w, h);
		if (model.isPressed()) {
			background = ColorUtils.darken(background, 0.9f);
			g2.setPaint(background);
			g2.fillRect(0, 0, w, h);
			background = ColorUtils.darken(background, 0.5f);
			g2.setPaint(background);
			g2.fillRect(0, 0, w, 2);
			g2.fillRect(0, 0, 2, h);
			g2.setPaint(ColorUtils.lighten(background, 5));
			g2.fillRect(w - 2, 0, 2, h);
			g2.fillRect(0, h - 2, 2, h);
		} else if (model.isRollover()) {
			background = ColorUtils.darken(background, 2f);
			g2.setPaint(background);
			g2.fillRect(0, 0, w, h);
			background = ColorUtils.lighten(background, 2);
			g2.setPaint(background);
			g2.fillRect(0, 0, w, h);
			g2.setPaint(ColorUtils.darken(background, 3));
			g2.fillRect(0, 0, w, 2);
			g2.fillRect(0, 0, 2, h);
			g2.setPaint(ColorUtils.lighten(background, 3));
			g2.fillRect(w, 0, 2, h);
			g2.fillRect(0, h, 2, h);
			
		}
	}


	@Override
	public void paintTableHeader(JTable table, int width, int height, Graphics2D g2) {
		// TODO Auto-generated method stub
		g2.setPaint(new Color(235, 234, 219));
		g2.fillRect(0, 0, width, height);
		
	}

	@Override
	public void paintGlassPanel(GlassPanel control, Graphics2D g2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paintVerticalPanel(VerticalPanel verticalPanel, Graphics2D g2) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "wmp11";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "WMP 11 Beta";
	}

	@Override
	public void onRegister(AspectManager<? extends Aspect> aspectManager) {
		// TODO Auto-generated method stub
	}
}
