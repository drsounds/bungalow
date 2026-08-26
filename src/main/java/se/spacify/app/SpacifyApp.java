package se.spacify.app;

import se.spacify.db.DatabaseManager;
import se.spacify.ui.MainWindow;

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
        // ── Crash logging ─────────────────────────────────────────────────────
        // Installed first, before anything else can fail: the app has no console
        // attached when launched other than from a terminal (double-click, a
        // packaged app, etc.), so an uncaught exception on the EDT — which Swing
        // otherwise only ever prints to System.err — would previously vanish
        // with no trace at all. Every uncaught exception on any thread now also
        // lands in ~/.spacify/crash.log, in addition to (not instead of) stderr.
        installCrashLogging();

        // ── Look and feel ─────────────────────────────────────────────────────
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("SplitPane.dividerSize", 1); 

                    break;
                }
            }
        } catch (Exception e) {
            // fall back to default L&F
        }
        // Seed colours + the app-wide 11px Tahoma font into UIDefaults before any
        // component is built, so trees/tables/labels pick it up from the start.
        se.spacify.ui.theme.ThemeManager.applyToDefaults();


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
        // discovered and started by ApplicationManager during MainWindow startup.

        // ── Register shutdown hook ────────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //sm.shutdownAll();
            se.spacify.web.CefRuntime.dispose();
            DatabaseManager.getInstance().close();
        }));

        // ── UI ────────────────────────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }

    /** Write every uncaught exception (on the EDT included) to ~/.spacify/crash.log,
     *  alongside the existing System.err output, so it's recoverable even when no
     *  console is attached to how the app was launched. */
    private static void installCrashLogging() {
        try {
            java.nio.file.Path logFile = java.nio.file.Path.of(System.getProperty("user.home"), ".spacify", "crash.log");
            java.nio.file.Files.createDirectories(logFile.getParent());
            Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
                String header = java.time.LocalDateTime.now() + " [" + thread.getName() + "] uncaught exception:";
                System.err.println(header);
                ex.printStackTrace();
                try (java.io.PrintWriter w = new java.io.PrintWriter(new java.io.FileWriter(logFile.toFile(), true))) {
                    w.println(header);
                    ex.printStackTrace(w);
                } catch (java.io.IOException ignored) {
                    // Best-effort: nothing more useful to do if the log file itself can't be written.
                }
            });
        } catch (Exception ignored) {
            // Best-effort: crash-logging setup failing shouldn't block startup.
        }
    }
}
