package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;

import javax.swing.*;
import java.awt.*;

public final class StatusDot extends JComponent {
    private Color color;

    public StatusDot(Color color) {
        this.color = color;
        setPreferredSize(new Dimension(10, 10));
        setMinimumSize(new Dimension(10, 10));
        setMaximumSize(new Dimension(10, 10));
    }

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setColor(Colors.alpha(color, 45));
            g2.fillOval(x - 3, y - 3, size + 6, size + 6);
            g2.setColor(color);
            g2.fillOval(x + 1, y + 1, size - 2, size - 2);
        } finally {
            g2.dispose();
        }
    }
}
