package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Botón redondeado con acento neón. Soporta variantes para acciones
 * primarias, secundarias y botones de icono.
 */
public class NeonButton extends JButton {
    public enum Variant { PRIMARY, SECONDARY, GHOST, DANGER }

    private Variant variant = Variant.SECONDARY;
    private boolean accent;
    private boolean compact;
    private int radius = 7;

    public NeonButton(String text) {
        super(text);
        init();
    }

    public NeonButton(String text, Icon icon) {
        super(text);
        setIcon(icon);
        init();
    }

    public NeonButton(Icon icon) {
        super(icon);
        init();
        compact = true;
        setBorder(new EmptyBorder(6, 6, 6, 6));
    }

    private void init() {
        setFont(Fonts.BODY_MEDIUM);
        setForeground(Colors.TEXT);
        setBackground(Colors.SURFACE_2);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(8, 14, 8, 14));
    }

    public void setAccent(boolean accent) {
        this.accent = accent;
        this.variant = accent ? Variant.PRIMARY : Variant.SECONDARY;
        setForeground(accent ? Colors.BACKGROUND : Colors.TEXT);
        repaint();
    }

    public NeonButton setVariant(Variant variant) {
        this.variant = variant == null ? Variant.SECONDARY : variant;
        this.accent = variant == Variant.PRIMARY;
        return this;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
        setBorder(compact ? new EmptyBorder(5, 5, 5, 5)
                : new EmptyBorder(8, 14, 8, 14));
        revalidate();
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();
            boolean pressed = getModel().isPressed();
            boolean enabled = isEnabled();

            Color bg;
            Color border;
            Color fg;
            switch (variant) {
                case PRIMARY -> {
                    bg = hover ? Colors.PRIMARY_BRIGHT : Colors.PRIMARY;
                    if (pressed) {
                        bg = Colors.PRIMARY.darker();
                    }
                    border = Colors.PRIMARY_BRIGHT;
                    fg = Colors.BACKGROUND;
                }
                case DANGER -> {
                    bg = hover ? Colors.alpha(Colors.ERROR, 55)
                            : Colors.alpha(Colors.ERROR, 25);
                    border = Colors.alpha(Colors.ERROR, 180);
                    fg = Colors.ERROR;
                }
                case GHOST -> {
                    bg = hover ? Colors.alpha(Colors.PANEL_HOVER, 180)
                            : Colors.alpha(Colors.SURFACE, 0);
                    border = hover ? Colors.BORDER_ACTIVE : Colors.alpha(Colors.BORDER, 0);
                    fg = hover ? Colors.PRIMARY_BRIGHT : Colors.TEXT_SECONDARY;
                }
                default -> {
                    bg = hover ? Colors.PANEL_HOVER : Colors.SURFACE_2;
                    border = hover ? Colors.BORDER_ACTIVE : Colors.BORDER;
                    fg = hover ? Colors.PRIMARY_BRIGHT : Colors.TEXT;
                }
            }
            if (!enabled) {
                bg = Colors.alpha(Colors.SURFACE_2, 120);
                border = Colors.alpha(Colors.BORDER, 120);
                fg = Colors.TEXT_MUTED;
            }

            if (hover && enabled) {
                g2.setColor(Colors.alpha(Colors.GLOW, 35));
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 1,
                        radius + 2, radius + 2);
            }

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            if (variant == Variant.PRIMARY && enabled) {
                g2.setPaint(new GradientPaint(0, 0, Colors.PRIMARY_BRIGHT,
                        0, getHeight(), Colors.PRIMARY));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            }

            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            setForeground(fg);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
