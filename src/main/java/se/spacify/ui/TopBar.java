package se.spacify.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class TopBar extends JPanel {
	private static final long serialVersionUID = -4103483010651019343L;

	public TopBar() {
		
	}

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        ((MainWindow)(SwingUtilities.getWindowAncestor(this))).getSkin().paintTopBar(this, g2);
		
        g2.dispose();
    }

}
