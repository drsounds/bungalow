package se.spacify.plugin.spot.skin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point; 
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

public class Spot09Skin extends Skin {

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "spot09";
    }

    @Override
    public void paintTopBar(JPanel control, Graphics2D g2) {
        // TODO Auto-generated method stub
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
    public void paintTabButton(TabButton button, Graphics2D g2) {
    }

    @Override
    public void paintGlossyButton(GlossyButton control, Graphics2D g2, int x, int y, int d) {
        
    }

    @Override
    public void paintPlaylist(JPanel control, Graphics2D g2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void paintGlassPanel(GlassPanel control, Graphics2D g2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void paintToolBar(ToolBar control, Graphics2D g2) {
        int w = control.getWidth(), h = control.getHeight();
        g2.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(0, h), new float[] {0, 1}, new Color[] { ColorUtils.darken(control.getBackground(),0.5f), ColorUtils.darken(control.getBackground(), 0.3f) }));
        g2.fillRect(0, 0, w, h);
        g2.setColor(ColorUtils.darken(control.getBackground(), 1.1f));
        g2.drawLine(0, h - 1, w, h - 1);
        g2.setColor(ColorUtils.lighten(control.getBackground(), 3.1f));
        g2.drawLine(0, h - 2, w, h - 2);
    }

    @Override
    public void paintTableHeader(JTable table, int width, int height, Graphics2D g2) {
        
    }

    @Override
    public void paintTextField(TextField control, Graphics2D g2) {
        int w = control.getWidth(), h = control.getHeight();
 
        g2.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(0, h), new float[] {0, 1}, new Color[] { ColorUtils.darken(control.getBackground(),0.3f), ColorUtils.darken(control.getBackground(), 0.5f) }));
        g2.fillRect(0, 0, w, h);
    }

    @Override
    public void paintToolButton(ToolButton control, Graphics2D g2) {
        
    }

    @Override
    public void paintVerticalPanel(VerticalPanel verticalPanel, Graphics2D g2) {
        
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Spot 09";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
    }
}
