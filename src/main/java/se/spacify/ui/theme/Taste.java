package se.spacify.ui.theme;

import java.awt.Color;
import java.util.ArrayList;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.chrome.Chrome;

public class Taste {
    private ArrayList<Runnable> listeners;
    private Theme theme;
    private Design design;
    public Design getDesign() {
        if (theme != null) {
            if (theme.getDesign() != null) return theme.getDesign();
        }
        return design;
    }
    public void setDesign(Design design) {
        this.design = design;
    }
    private Skin skin;
    public Skin getSkin() {
        if (theme != null) {
            if (theme.getSkin() != null) return theme.getSkin();
        }
        return skin;
    }
    public void setSkin(Skin skin) {
        this.skin = skin;
    }
    private Chrome chrome;
    public Chrome getChrome() {
        if (theme != null) {
            if (theme.getChrome() != null) return theme.getChrome();
        }
        return chrome;
    }
    public void setChrome(Chrome chrome) {
        this.chrome = chrome;
    }
    public Theme getTheme() {
        return theme;
    }
    public void setTheme(Theme theme) {
        this.theme = theme;
    }
    public Taste() {
        listeners = new ArrayList<>();
    }
    private float hue        = 0.0f;  // 0-1  (background tint)
    private float saturation = 0.0f;  // 0-1  (background tint)
    private float lightness  = 0.5f;  // 0-1  (background tint)
    private boolean darkMode = true;
    private Color accentColor = new Color(30, 215, 96);  // Spotify green default

    // ── Display toggles (saved alongside the HSL / dark-light / accent settings) ──
    private boolean stripedRows          = true;   // alternate row shading
    private boolean highContrast         = false;  // plain B/W background (WMP-style)
    private boolean highContrastInverted = false;  // white-on-black vs black-on-white
    private boolean tintText             = true;   // tint text in light mode (else black)

    // Cached per applyToDefaults() — safe to read from any component at render time
    private Color currentBg    = new Color(22, 22, 22);
    private Color currentAltBg = new Color(30, 30, 30);
    private Color currentFg    = new Color(210, 210, 210);
    private Color currentGrid  = new Color(35, 35, 35);

    public Color getBackground()          { return currentBg; }
    public Color getAlternateBackground() { return currentAltBg; }
    public Color getForeground()           { return currentFg; }
    public Color getGridColor()            { return currentGrid; }

    // Fixed highlight for the row matching the currently-playing track,
    // applied consistently across the play-queue and all library/list views.
    private final Color NOW_PLAYING_BG = Color.BLACK;
    private final Color NOW_PLAYING_FG = new Color(144, 238, 144);  // light green

    public Color getNowPlayingBackground() { return NOW_PLAYING_BG; }
    public Color getNowPlayingForeground() { return NOW_PLAYING_FG; }

    public void setHue(float h)           { hue = h;  }
    public void setSaturation(float s)    { saturation = s; }
    public void setLightness(float l)     { lightness = l; }
    public void setDarkMode(boolean d)    { darkMode = d; }
    public void setAccentColor(Color c)   { accentColor = c; }

    public void setStripedRows(boolean v)          { stripedRows = v; }
    public void setHighContrast(boolean v)         { highContrast = v; }
    public void setHighContrastInverted(boolean v) { highContrastInverted = v; }
    public void setTintText(boolean v)             { tintText = v; }

    public float   getHue()          { return hue; }
    public float   getSaturation()   { return saturation; }
    public float   getLightness()    { return lightness; }
    public boolean isDarkMode()      { return darkMode; }
    public Color   getAccentColor()  { return accentColor; }

    public boolean isStripedRows()          { return stripedRows; }
    public boolean isHighContrast()         { return highContrast; }
    public boolean isHighContrastInverted() { return highContrastInverted; }
    public boolean isTintText()             { return tintText; }

    public void addChangeListener(Runnable r) { listeners.add(r); }
    public void notify_() {
        for (Runnable r : listeners) r.run();
    }
}
