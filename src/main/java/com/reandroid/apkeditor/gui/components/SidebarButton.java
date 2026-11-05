package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Elemento de navegación lateral con indicador neón y estado activo.
 */
public final class SidebarButton extends JButton {
    private final String label;
    private final String iconName;
    private boolean active;

    public SidebarButton(String label, String iconName) {
        this.label = label;
        this.iconName = iconName;
        setText("");
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(Fonts.BODY);
        setPreferredSize(new Dimension(208, 40));
        setMinimumSize(new Dimension(120, 40));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        setToolTipText(label);
    }

    public String getLabelText() {
        return label;
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();
            if (active) {
                g2.setColor(Colors.NAV_ACTIVE_BG);
                g2.fillRoundRect(6, 2, getWidth() - 10, getHeight() - 4, 8, 8);
                g2.setColor(Colors.alpha(Colors.PRIMARY, 60));
                g2.drawRoundRect(6, 2, getWidth() - 10, getHeight() - 4, 8, 8);
                g2.setColor(Colors.NAV_ACTIVE_TEXT);
                g2.fillRoundRect(0, 9, 3, getHeight() - 18, 3, 3);
            } else if (hover) {
                g2.setColor(Colors.alpha(Colors.PANEL_HOVER, 165));
                g2.fillRoundRect(6, 2, getWidth() - 10, getHeight() - 4, 8, 8);
            }

            Color iconColor = active ? Colors.NAV_ACTIVE_TEXT
                    : (hover ? Colors.PRIMARY_BRIGHT : Colors.TEXT_SECONDARY);
            Icon icon = IconFactory.icon(iconName, 18, iconColor);
            icon.paintIcon(this, g2, 16, (getHeight() - 18) / 2);

            g2.setFont(active ? Fonts.BODY.deriveFont(Font.BOLD) : Fonts.BODY);
            g2.setColor(active ? Colors.NAV_ACTIVE_TEXT
                    : (hover ? Colors.TEXT : Colors.TEXT_SECONDARY));
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(label, 46, y);

            if (active) {
                g2.setColor(Colors.alpha(Colors.PRIMARY_BRIGHT, 180));
                int dot = 4;
                g2.fillOval(getWidth() - 18, (getHeight() - dot) / 2, dot, dot);
            }
        } finally {
            g2.dispose();
        }
    }
}
