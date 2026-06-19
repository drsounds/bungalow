package se.spacify.app;

import se.spacify.config.ConfigManager;
import se.spacify.db.DatabaseManager;
import se.spacify.service.ServiceManager;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.ThemeManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.*;



public class SpacifyApp {
	// 1. Create your custom painter
    static Painter<JComponent> customHeaderPainter = new Painter<JComponent>() {
        @Override
        public void paint(Graphics2D g, JComponent c, int w, int h) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Define your custom background color/gradient
            g.setColor(new Color(55, 65, 80)); 
            g.fillRect(0, 0, w, h);
            
            // Optional: Add a bottom border
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(0, h - 1, w, h - 1);
        }
    };
    public static void main(String[] args) {
        // ── Look and feel ─────────────────────────────────────────────────────
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // fall back to default L&F
        }


        // ── Database ──────────────────────────────────────────────────────────
        try {
            DatabaseManager.getInstance().init();
        } catch (Exception e) {
            System.err.println("Warning: could not initialise library database: " + e.getMessage());
        }

     

	     // 2. Register the painter globally in your UIDefaults
	     /*UIDefaults defaults = UIManager.getLookAndFeelDefaults();
	     defaults.put("TableHeader.renderer[Enabled].backgroundPainter", customHeaderPainter);
	     defaults.put("TableHeader.renderer[MouseOver].backgroundPainter", customHeaderPainter);
	     defaults.put("TableHeader.renderer[Pressed].backgroundPainter", customHeaderPainter);*/
        // ── Services ──────────────────────────────────────────────────────────
        // Services are now contributed by plugins (see the Local Music plugin),
        // discovered and started by PluginManager during MainWindow startup.
        ServiceManager sm = ServiceManager.getInstance();
        sm.startAll();

        // ── Register shutdown hook ────────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sm.shutdownAll();
            se.spacify.web.CefRuntime.dispose();
            DatabaseManager.getInstance().close();
        }));

        // ── UI ────────────────────────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
