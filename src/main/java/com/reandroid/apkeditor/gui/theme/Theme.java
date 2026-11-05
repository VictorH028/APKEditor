package com.reandroid.apkeditor.gui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Sistema de temas y utilidades de estilos. No contiene lógica de negocio.
 */
public final class Theme {
    private Theme() {
    }

    public static final String[] NAMES = {"Futuristic", "Cyberpunk", "Dark", "Midnight"};
    private static String current = "Futuristic";

    public static void install() {
        UIManager.put("Panel.background", Colors.BACKGROUND);
        UIManager.put("Label.foreground", Colors.TEXT);
        UIManager.put("TextField.background", Colors.SURFACE_2);
        UIManager.put("TextField.foreground", Colors.TEXT);
        UIManager.put("TextField.caretForeground", Colors.PRIMARY);
        UIManager.put("TextPane.background", Colors.SURFACE);
        UIManager.put("TextPane.foreground", Colors.TEXT);
        UIManager.put("TextArea.background", Colors.SURFACE);
        UIManager.put("TextArea.foreground", Colors.TEXT);
        UIManager.put("Button.background", Colors.SURFACE_2);
        UIManager.put("Button.foreground", Colors.TEXT);
        UIManager.put("Button.font", Fonts.BODY);
        UIManager.put("ComboBox.background", Colors.SURFACE_2);
        UIManager.put("ComboBox.foreground", Colors.TEXT);
        UIManager.put("ComboBox.selectionBackground", Colors.PANEL_HOVER);
        UIManager.put("ComboBox.selectionForeground", Colors.PRIMARY_BRIGHT);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumb", Colors.BORDER);
        UIManager.put("ScrollBar.track", Colors.BACKGROUND_SECONDARY);
        UIManager.put("Tree.background", Colors.PANEL);
        UIManager.put("Tree.foreground", Colors.TEXT);
        UIManager.put("Tree.selectionBackground", Colors.PANEL_HOVER);
        UIManager.put("Tree.selectionForeground", Colors.PRIMARY_BRIGHT);
        UIManager.put("ToolTip.background", Colors.PANEL_SECONDARY);
        UIManager.put("ToolTip.foreground", Colors.TEXT);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(Colors.BORDER));
        UIManager.put("SplitPane.background", Colors.BACKGROUND);
        UIManager.put("SplitPane.dividerSize", 14);
        UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
        UIManager.put("SplitPaneDivider.draggingColor", Colors.BORDER_ACTIVE);
        apply(current);
    }

    public static String current() {
        return current;
    }

    public static void apply(String name) {
        if (name == null || name.isBlank()) {
            name = "Futuristic";
        }
        current = name;
        switch (name) {
            case "Cyberpunk" -> cyberpunk();
            case "Dark" -> dark();
            case "Midnight" -> midnight();
            default -> futuristic();
        }
    }

    private static void futuristic() {
        Colors.BACKGROUND = new Color(0x020B18);
        Colors.BACKGROUND_SECONDARY = new Color(0x041426);
        Colors.SURFACE = new Color(0x06182A);
        Colors.SURFACE_2 = new Color(0x071D32);
        Colors.PANEL = new Color(0x06182A);
        Colors.PANEL_SECONDARY = new Color(0x071D32);
        Colors.PANEL_HOVER = new Color(0x0B2945);
        Colors.BORDER = new Color(0x123653);
        Colors.BORDER_ACTIVE = new Color(0x087FBC);
        Colors.PRIMARY = new Color(0x00BFFF);
        Colors.PRIMARY_BRIGHT = new Color(0x00D5FF);
        Colors.CYAN = new Color(0x00E5FF);
        Colors.SECONDARY = new Color(0xA855F7);
        Colors.PURPLE = new Color(0xA855F7);
        Colors.MAGENTA = new Color(0xD946EF);
        Colors.SUCCESS = new Color(0x00E6A0);
        Colors.GREEN = new Color(0x00E6A0);
        Colors.WARNING = new Color(0xFFB547);
        Colors.ERROR = new Color(0xFF4668);
        Colors.TEXT = new Color(0xE6F4FF);
        Colors.TEXT_SECONDARY = new Color(0x9CB4CC);
        Colors.TEXT_MUTED = new Color(0x64829F);
        Colors.NAV_ACTIVE_BG = new Color(0x07304C);
        Colors.NAV_ACTIVE_TEXT = new Color(0x00D5FF);
        Colors.GLOW = new Color(0x00BFFF);
    }

    private static void cyberpunk() {
        Colors.BACKGROUND = new Color(0x090012);
        Colors.BACKGROUND_SECONDARY = new Color(0x12001E);
        Colors.SURFACE = new Color(0x1A0526);
        Colors.SURFACE_2 = new Color(0x240833);
        Colors.PANEL = new Color(0x1A0526);
        Colors.PANEL_SECONDARY = new Color(0x240833);
        Colors.PANEL_HOVER = new Color(0x351047);
        Colors.BORDER = new Color(0x4A1B6B);
        Colors.BORDER_ACTIVE = new Color(0xB52BFF);
        Colors.PRIMARY = new Color(0xFF2FB3);
        Colors.PRIMARY_BRIGHT = new Color(0xFF6FD8);
        Colors.CYAN = new Color(0x00E5FF);
        Colors.SECONDARY = new Color(0xA855F7);
        Colors.PURPLE = new Color(0xA855F7);
        Colors.MAGENTA = new Color(0xFF2FB3);
        Colors.SUCCESS = new Color(0x00FF9C);
        Colors.GREEN = new Color(0x00FF9C);
        Colors.WARNING = new Color(0xFFC857);
        Colors.ERROR = new Color(0xFF3355);
        Colors.TEXT = new Color(0xFFE8FA);
        Colors.TEXT_SECONDARY = new Color(0xC9A0DC);
        Colors.TEXT_MUTED = new Color(0x8A6A9E);
        Colors.NAV_ACTIVE_BG = new Color(0x3B0D58);
        Colors.NAV_ACTIVE_TEXT = new Color(0xFF6FD8);
        Colors.GLOW = new Color(0xFF2FB3);
    }

    private static void dark() {
        Colors.BACKGROUND = new Color(0x0B0F14);
        Colors.BACKGROUND_SECONDARY = new Color(0x0E131A);
        Colors.SURFACE = new Color(0x121820);
        Colors.SURFACE_2 = new Color(0x171F29);
        Colors.PANEL = new Color(0x121820);
        Colors.PANEL_SECONDARY = new Color(0x171F29);
        Colors.PANEL_HOVER = new Color(0x1E2A37);
        Colors.BORDER = new Color(0x2A3A4A);
        Colors.BORDER_ACTIVE = new Color(0x2B7DA8);
        Colors.PRIMARY = new Color(0x38BDF8);
        Colors.PRIMARY_BRIGHT = new Color(0x7DD3FC);
        Colors.CYAN = new Color(0x22D3EE);
        Colors.SECONDARY = new Color(0xA78BFA);
        Colors.PURPLE = new Color(0xA78BFA);
        Colors.MAGENTA = new Color(0xE879F9);
        Colors.SUCCESS = new Color(0x34D399);
        Colors.GREEN = new Color(0x34D399);
        Colors.WARNING = new Color(0xFBBF24);
        Colors.ERROR = new Color(0xF87171);
        Colors.TEXT = new Color(0xE5EDF5);
        Colors.TEXT_SECONDARY = new Color(0x9FB0C0);
        Colors.TEXT_MUTED = new Color(0x6B7C8F);
        Colors.NAV_ACTIVE_BG = new Color(0x1B3648);
        Colors.NAV_ACTIVE_TEXT = new Color(0x7DD3FC);
        Colors.GLOW = new Color(0x38BDF8);
    }

    private static void midnight() {
        Colors.BACKGROUND = new Color(0x030616);
        Colors.BACKGROUND_SECONDARY = new Color(0x050B22);
        Colors.SURFACE = new Color(0x08102B);
        Colors.SURFACE_2 = new Color(0x0B1638);
        Colors.PANEL = new Color(0x08102B);
        Colors.PANEL_SECONDARY = new Color(0x0B1638);
        Colors.PANEL_HOVER = new Color(0x132155);
        Colors.BORDER = new Color(0x1E3270);
        Colors.BORDER_ACTIVE = new Color(0x4C7DFF);
        Colors.PRIMARY = new Color(0x4C7DFF);
        Colors.PRIMARY_BRIGHT = new Color(0x6C9BFF);
        Colors.CYAN = new Color(0x29D3FF);
        Colors.SECONDARY = new Color(0x8B5CF6);
        Colors.PURPLE = new Color(0x8B5CF6);
        Colors.MAGENTA = new Color(0xE879F9);
        Colors.SUCCESS = new Color(0x2CE0B0);
        Colors.GREEN = new Color(0x2CE0B0);
        Colors.WARNING = new Color(0xFFC24B);
        Colors.ERROR = new Color(0xFF5D7A);
        Colors.TEXT = new Color(0xE7EEFF);
        Colors.TEXT_SECONDARY = new Color(0xA0B0D0);
        Colors.TEXT_MUTED = new Color(0x65779E);
        Colors.NAV_ACTIVE_BG = new Color(0x172A63);
        Colors.NAV_ACTIVE_TEXT = new Color(0x8FB0FF);
        Colors.GLOW = new Color(0x4C7DFF);
    }

    public static JPanel panel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Colors.BACKGROUND);
        return panel;
    }

    public static Border empty(int size) {
        return new EmptyBorder(size, size, size, size);
    }

    public static Border empty(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static void card(JComponent component) {
        component.setOpaque(true);
        component.setBackground(Colors.SURFACE);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                new EmptyBorder(16, 16, 16, 16)));
    }

    public static void styleField(JTextField field) {
        field.setBackground(Colors.SURFACE_2);
        field.setForeground(Colors.TEXT);
        field.setCaretColor(Colors.PRIMARY);
        field.setSelectionColor(Colors.PANEL_HOVER);
        field.setSelectedTextColor(Colors.PRIMARY_BRIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                new EmptyBorder(6, 8, 6, 8)));
    }
}
