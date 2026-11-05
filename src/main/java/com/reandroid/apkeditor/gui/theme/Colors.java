package com.reandroid.apkeditor.gui.theme;

import java.awt.Color;

/**
 * Paleta visual compartida por todos los componentes de la interfaz.
 * Los valores son mutables para permitir el cambio de tema en caliente.
 */
public final class Colors {
    private Colors() {}

    public static Color BACKGROUND = new Color(0x020B18);
    public static Color BACKGROUND_SECONDARY = new Color(0x041426);
    public static Color SURFACE = new Color(0x06182A);
    public static Color SURFACE_2 = new Color(0x071D32);
    public static Color PANEL = new Color(0x06182A);
    public static Color PANEL_SECONDARY = new Color(0x071D32);
    public static Color PANEL_HOVER = new Color(0x0B2945);
    public static Color BORDER = new Color(0x123653);
    public static Color BORDER_ACTIVE = new Color(0x087FBC);

    public static Color PRIMARY = new Color(0x00BFFF);
    public static Color PRIMARY_BRIGHT = new Color(0x00D5FF);
    public static Color CYAN = new Color(0x00E5FF);
    public static Color SECONDARY = new Color(0xA855F7);
    public static Color PURPLE = new Color(0xA855F7);
    public static Color MAGENTA = new Color(0xD946EF);
    public static Color SUCCESS = new Color(0x00E6A0);
    public static Color GREEN = new Color(0x00E6A0);
    public static Color WARNING = new Color(0xFFB547);
    public static Color ERROR = new Color(0xFF4668);

    public static Color TEXT = new Color(0xE6F4FF);
    public static Color TEXT_SECONDARY = new Color(0x9CB4CC);
    public static Color TEXT_MUTED = new Color(0x64829F);

    public static Color NAV_ACTIVE_BG = new Color(0x07304C);
    public static Color NAV_ACTIVE_TEXT = new Color(0x00D5FF);
    public static Color GLOW = new Color(0x00BFFF);

    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color mix(Color a, Color b, float ratio) {
        float r = Math.min(1f, Math.max(0f, ratio));
        return new Color(
                Math.round(a.getRed() * (1f - r) + b.getRed() * r),
                Math.round(a.getGreen() * (1f - r) + b.getGreen() * r),
                Math.round(a.getBlue() * (1f - r) + b.getBlue() * r),
                Math.round(a.getAlpha() * (1f - r) + b.getAlpha() * r));
    }
}
