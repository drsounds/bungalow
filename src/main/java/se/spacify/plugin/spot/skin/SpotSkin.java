package se.spacify.plugin.spot.skin;

import java.awt.Graphics2D;

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

public class SpotSkin extends Skin {

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "spot";
    }

    @Override
    public void paintTopBar(JPanel control, Graphics2D g2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void paintHeader(JPanel header, Graphics2D g2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void paintFooter(JPanel footer, Graphics2D g2) {
        // TODO Auto-generated method stub
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
    }

    @Override
    public void paintTableHeader(JTable table, int width, int height, Graphics2D g2) {
        
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
        return "Spot";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
    }
}
