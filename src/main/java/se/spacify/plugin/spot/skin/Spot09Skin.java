package se.spacify.plugin.spot.skin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import javax.swing.JPanel;

import se.spacify.controls.SplitPane.SplitPaneDivider;

import se.spacify.controls.TextField;
import se.spacify.controls.ToolBar;
import se.spacify.controls.Tree;

import se.spacify.navigation.ViewStack;

import se.spacify.ui.LeftLibraryMenu;
import se.spacify.ui.LeftMenuPanel;
import se.spacify.ui.theme.ColorUtils;
import se.spacify.ui.theme.ThemeManager;

public class Spot09Skin extends SpotSkin {

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "spot09";
    }

    @Override
    public void paintHeader(JPanel header, Graphics2D g2) {
        // TODO Auto-generated method stub
        int w = header.getWidth(), h = header.getHeight();
        g2.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(0, h), new float[] {0, 1}, new Color[] { ColorUtils.darken(header.getBackground(), 3.3f), ColorUtils.darken(header.getBackground(), 2.1f) }));
        g2.fillRect(0, 0, w, h);
        g2.setColor(ColorUtils.darken(header.getBackground(), 1.1f));
        g2.drawLine(0, h - 1, w, h - 1);
        g2.setColor(ColorUtils.lighten(header.getBackground(), 3.1f));
        g2.drawLine(0, h - 2, w, h - 2);
    }

	@Override
	public void paintLeftLibraryMenu(LeftLibraryMenu control, Graphics2D g2) {
        int w = control.getWidth(), h = control.getHeight();
        g2.setBackground(control.getBackground().darker());
        g2.fillRect(0, 0, w, h);
	}

	@Override
	public void paintTree(Tree control, Graphics2D g2) {
        int w = control.getWidth(), h = control.getHeight();
        Color bgColor = ColorUtils.darken(control.getBackground(),  0.8f); 
        g2.setBackground(bgColor);
        g2.setPaint(bgColor);
        g2.fillRect(0, 0, w, h); 
        System.out.println("Test");
	}

	@Override
	public void paintLeftMenuPanel(LeftMenuPanel control, Graphics2D g2) {
        int w = control.getWidth(), h = control.getHeight();
        g2.setBackground(control.getBackground().darker());
        g2.fillRect(0, 0, w, h);
	}

	@Override
	public void paintViewStack(ViewStack control, Graphics2D g2) {
        if (control.getTaste().isDarkMode()) {
            control.setBackground(control.getBackground().brighter());
        }
	}

    @Override
    public void paintFooter(JPanel footer, Graphics2D g2) {
        // TODO Auto-generated method stub
        int w = footer.getWidth(), h = footer.getHeight();
        g2.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(0, h), new float[] {0, 1}, new Color[] { ColorUtils.darken(footer.getBackground(), 1.9f), ColorUtils.darken(footer.getBackground(), 1.1f) }));
        g2.fillRect(0, 0, w, h);
        g2.setColor(ColorUtils.darken(footer.getBackground(), 1.1f));
        g2.drawLine(0, 0, w, 0);
        g2.setColor(ColorUtils.lighten(footer.getBackground(), 3.1f));
        g2.drawLine(0, 1, w, 1);
    }

    @Override
    public void paintToolBar(ToolBar control, Graphics2D g2) {
        int w = control.getWidth(), h = control.getHeight();
        g2.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(0, h), new float[] {0, 1}, new Color[] { ColorUtils.darken(control.getBackground(),1f), ColorUtils.darken(control.getBackground(), 0.9f) }));
        g2.fillRect(0, 0, w, h);
        g2.setColor(ColorUtils.darken(control.getBackground(), 1.1f));
        g2.drawLine(0, h - 1, w, h - 1);
        g2.setColor(ColorUtils.lighten(control.getBackground(), 7.1f));
        g2.drawLine(0, h - 2, w, h - 2);
    }

    @Override
    public void paintTextField(TextField control, Graphics2D g2) {
        int w = control.getWidth(), h = control.getHeight();
 
        g2.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(0, h), new float[] {0, 1}, new Color[] { ColorUtils.darken(control.getBackground(),0.3f), ColorUtils.darken(control.getBackground(), 0.5f) }));
        g2.fillRect(0, 0, w, h);
    }

	@Override
	public void paintSplitPaneDivider(SplitPaneDivider control, Graphics2D g2) {
		int h = control.getHeight();
		if (ThemeManager.isDarkMode()) {
			g2.setPaint(ColorUtils.darken(control.getBackground(), 0.2f));
		}
        g2.fillRect(0, 0, 1, h);
	}

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Spot 09";
    }
}
