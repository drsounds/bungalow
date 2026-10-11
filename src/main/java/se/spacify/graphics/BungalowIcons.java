package se.spacify.graphics;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.Icon;

/**
 * Icons from the Bungalow Icons font, the same glyphs the web app uses
 * ({@code bungalow-icon-<name>}). The font and its name -> codepoint map are
 * copied from drsounds/bungalow-design-language into {@code resources/fonts/}.
 * Each icon paints in its component's foreground colour, so it follows the
 * theme and selection like the label next to it.
 */
public final class BungalowIcons {

    private static final String FONT = "/fonts/bungalow-icons.ttf";
    private static final String MAP = "/fonts/bungalow-icons.properties";

    private static Font font;
    private static Properties codepoints;

    private BungalowIcons() {}

    /**
     * @param name icon name, e.g. {@code "playlist"} or {@code "magnifying-glass"}
     * @param size width and height of the icon, in pixels
     * @return the icon, or {@code null} when the font or the name is missing
     */
    public static Icon icon(String name, int size) {
        load();
        if (font == null) return null;
        String hex = codepoints.getProperty(name);
        if (hex == null) return null;
        return new GlyphIcon(new String(Character.toChars(Integer.parseInt(hex, 16))), size);
    }

    private static synchronized void load() {
        if (codepoints != null) return;
        codepoints = new Properties();
        try (InputStream f = BungalowIcons.class.getResourceAsStream(FONT);
             InputStream m = BungalowIcons.class.getResourceAsStream(MAP)) {
            if (f == null || m == null) return;
            font = Font.createFont(Font.TRUETYPE_FONT, f);
            codepoints.load(m);
        } catch (IOException | FontFormatException e) {
            font = null;
        }
    }

    private static final class GlyphIcon implements Icon {
        private final String glyph;
        private final int size;

        GlyphIcon(String glyph, int size) {
            this.glyph = glyph;
            this.size = size;
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(c != null ? c.getForeground() : g.getColor());
                Font f = font.deriveFont((float) size);
                GlyphVector gv = f.createGlyphVector(g2.getFontRenderContext(), glyph);
                Rectangle2D b = gv.getVisualBounds();
                // Shrink wide glyphs to fit, then centre the glyph's ink in the square.
                double scale = Math.min(1.0, size / Math.max(b.getWidth(), b.getHeight()));
                g2.translate(x + size / 2.0, y + size / 2.0);
                g2.scale(scale, scale);
                g2.translate(-b.getCenterX(), -b.getCenterY());
                g2.fill(gv.getOutline());
            } finally {
                g2.dispose();
            }
        }
    }
}
