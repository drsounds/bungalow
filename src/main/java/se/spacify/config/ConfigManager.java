package se.spacify.config;

import se.spacify.ui.theme.ThemeManager;

import java.awt.Color;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ConfigManager {

    private static final Path CONFIG_FILE =
        Path.of(System.getProperty("user.home"), ".spacify", "settings.properties");

    public static void load() {
        if (!Files.exists(CONFIG_FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        try {
            ThemeManager.setHue(        Float.parseFloat(p.getProperty("theme.hue",        "0.0")));
            ThemeManager.setSaturation( Float.parseFloat(p.getProperty("theme.saturation", "0.0")));
            ThemeManager.setLightness(  Float.parseFloat(p.getProperty("theme.lightness",  "0.5")));
            ThemeManager.setDarkMode(  Boolean.parseBoolean(p.getProperty("theme.darkMode", "true")));
            ThemeManager.setAccentColor(new Color(
                Integer.parseInt(p.getProperty("theme.accentColor",
                    String.valueOf(new Color(30, 215, 96).getRGB()))), true));
            ThemeManager.setStripedRows(         Boolean.parseBoolean(p.getProperty("theme.stripedRows",          "true")));
            ThemeManager.setHighContrast(        Boolean.parseBoolean(p.getProperty("theme.highContrast",         "false")));
            ThemeManager.setHighContrastInverted(Boolean.parseBoolean(p.getProperty("theme.highContrastInverted", "false")));
            ThemeManager.setTintText(            Boolean.parseBoolean(p.getProperty("theme.tintText",            "true")));
        } catch (NumberFormatException ignored) {
            // corrupted config — keep defaults
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("theme.hue",         String.valueOf(ThemeManager.getHue()));
        p.setProperty("theme.saturation",  String.valueOf(ThemeManager.getSaturation()));
        p.setProperty("theme.lightness",   String.valueOf(ThemeManager.getLightness()));
        p.setProperty("theme.darkMode",    String.valueOf(ThemeManager.isDarkMode()));
        p.setProperty("theme.accentColor", String.valueOf(ThemeManager.getAccentColor().getRGB()));
        p.setProperty("theme.stripedRows",          String.valueOf(ThemeManager.isStripedRows()));
        p.setProperty("theme.highContrast",         String.valueOf(ThemeManager.isHighContrast()));
        p.setProperty("theme.highContrastInverted", String.valueOf(ThemeManager.isHighContrastInverted()));
        p.setProperty("theme.tintText",             String.valueOf(ThemeManager.isTintText()));
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
                p.store(out, "Spacify theme settings");
            }
        } catch (IOException ignored) {
            // non-fatal — skip silently
        }
    }
}
