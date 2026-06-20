package se.spacify.plugin.wmp.skin;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JTable;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.controls.GlassPanel;
import se.spacify.controls.GlossyButton;
import se.spacify.controls.TabButton;
import se.spacify.controls.TextField;
import se.spacify.controls.ToolBar;
import se.spacify.controls.ToolButton;
import se.spacify.controls.VerticalPanel;
import se.spacify.skinning.Skin;
import se.spacify.ui.theme.ColorUtils;
import se.spacify.ui.theme.ThemeManager;

public class WMP8Skin extends Skin {

	private static final int ARC = 12;
	@Override
	public void paintTopBar(JPanel control, Graphics2D g2) {
        int  h = control.getHeight();
        Color tintColor = ThemeManager.getTintColor();
        g2.setPaint(new GradientPaint(0, 0, tintColor, 0, h, ThemeManager.accentLight(2f)));
	}
	@Override
	public void paintHeader(JPanel control, Graphics2D g2) {
        int h = control.getHeight();
		Color tintColor = ThemeManager.getTintColor();
		g2.setPaint(new GradientPaint(0, 0, ThemeManager.accentLight(2f), 0, h, tintColor));
	}
	@Override
	public void paintFooter(JPanel control, Graphics2D g2) {
        int h = control.getHeight();

    	Color tintColor = ThemeManager.getTintColor();        
    	g2.setPaint(new GradientPaint(0, 0, ThemeManager.accentLight(2f), 0, h, tintColor));
	}
	@Override
	public void paintGlassPanel(GlassPanel control, Graphics2D g2) {
		int w = control.getWidth(), h = control.getHeight();
		Path2D shape = control.shape(w, h);

		// Base accent gradient, antialiased to the rounded/diagonal outline.
		g2.setPaint(new GradientPaint(0, 0, ThemeManager.accentLight(2f), 0, h, ThemeManager.getTintColor()));
		g2.fill(shape);

		// Glossy white overlays, confined to the shape.
		g2.setClip(shape);
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 0), 0, h, new Color(255, 255, 255, 255)));
		g2.fillRect(0, h / 2, w, h - (h / 2));
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 127), 0, h, new Color(255, 255, 255, 0)));
		g2.fillRect(0, 1, w, h / 2);
	}

	@Override
	public void paintToolBar(ToolBar control, Graphics2D g2) {
		int w = control.getWidth(), h = control.getHeight();
		g2.setPaint(ThemeManager.getTintColor());
		g2.fillRect(0, 0, w, h);
	}
	@Override
	public void paintTabButton(TabButton control, Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = control.getWidth(), h = control.getHeight();

		// Top-rounded, bottom-sharp shape flush with the panel's bottom edge.
		Path2D tab = new Path2D.Float();
		tab.moveTo(0, h);
		tab.lineTo(0, ARC);
		tab.quadTo(0, 0, ARC, 0);
		tab.lineTo(w - ARC, 0);
		tab.quadTo(w, 0, w, ARC);
		tab.lineTo(w, h);
		tab.closePath();


	 	if (control.isSelected()) {
			g2.setPaint(new RadialGradientPaint((float)(w / 2), (float)h, 20f, new float[] {}, new Color[] { ThemeManager.getTintColor(), ThemeManager.accentDark(0.5f)}));
			if (control.getOrientation() == TabButton.ORIENTATION_HORIZONTAL) {
				g2.fill(tab);
			} else {
				g2.fillRect(0, 0, w, h);
			}
			g2.setPaint(new GradientPaint(0f, 0f, ThemeManager.getAccentColor(), (float)(w), h / 2f, new Color(255, 255 ,255, 0)));
	
		} else if (control.isHovered()) {
			g2.setColor(new Color(255, 255, 255, 45));
			if (control.getOrientation() == TabButton.ORIENTATION_HORIZONTAL) {
				g2.fill(tab);
			} else {
				g2.fillRect(0, 0, w, h);
			}
		} else {
		}
	 	
	}
	@Override
	public void paintGlossyButton(GlossyButton control, Graphics2D g2, int x, int y, int d) {
		// TODO Auto-generated method stub
		
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
	public void paintPlaylist(JPanel control, Graphics2D g2) {
		// TODO Auto-generated method stub
		Color tintColor = ThemeManager.getTintColor();
        g2.setPaint(tintColor);
        g2.fillRect(0, 0, control.getWidth(), control.getHeight());
		
	}

	@Override
	public void paintTextField(TextField control, Graphics2D g2) {

	}

	@Override
	public void paintVerticalPanel(VerticalPanel control, Graphics2D g2) {
		// TODO Auto-generated method stub

		int w = control.getWidth();
		Color tintColor = ThemeManager.getTintColor();
		g2.setPaint(new GradientPaint(0, 0, ThemeManager.accentLight(2f), w, 0, tintColor));
	}
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "wmp8";
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "WMP 8";
	}
	@Override
	public void onRegister(AspectManager<? extends Aspect> aspectManager) {
		// TODO Auto-generated method stub
	}
}
