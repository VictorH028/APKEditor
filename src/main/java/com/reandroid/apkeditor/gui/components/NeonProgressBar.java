package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;

import javax.swing.*;
import java.awt.*;

/**
 * Barra de progreso neón con brillo y porcentaje opcional.
 */
public final class NeonProgressBar extends JComponent {
    private int value;
    private boolean showPercentage = true;
    private boolean animated = true;
    private int shimmer;
    private final Timer timer;
    private Color accent = Colors.PRIMARY;

    public NeonProgressBar() {
        setFont(getFont());
        setPreferredSize(new Dimension(240, 18));
        timer = new Timer(55, e -> {
            shimmer = (shimmer + 5) % 200;
            repaint();
        });
    }

    public void setValue(int value) {
        this.value = Math.max(0, Math.min(100, value));
        repaint();
    }

    public int getValue() {
        return value;
    }

    public void setShowPercentage(boolean showPercentage) {
        this.showPercentage = showPercentage;
        repaint();
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
        if (!animated) {
            timer.stop();
        }
    }

    public void setAccent(Color accent) {
        this.accent = accent == null ? Colors.PRIMARY : accent;
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (animated && value < 100) {
            timer.start();
        }
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int labelWidth = showPercentage ? 42 : 0;
            int barWidth = Math.max(20, getWidth() - labelWidth);
            int barHeight = Math.min(9, Math.max(6, getHeight() - 4));
            int y = (getHeight() - barHeight) / 2;

            g2.setColor(Colors.BACKGROUND_SECONDARY);
            g2.fillRoundRect(0, y, barWidth, barHeight, barHeight, barHeight);
            g2.setColor(Colors.alpha(Colors.BORDER, 180));
            g2.drawRoundRect(0, y, barWidth - 1, barHeight - 1, barHeight, barHeight);

            int fill = Math.round(barWidth * (value / 100f));
            if (fill > 3) {
                g2.setPaint(new GradientPaint(0, y, Colors.alpha(accent, 220),
                        barWidth, y, Colors.PRIMARY_BRIGHT));
                g2.fillRoundRect(0, y, fill, barHeight, barHeight, barHeight);
                g2.setColor(Colors.alpha(accent, 80));
                g2.fillRoundRect(0, y - 2, fill, barHeight + 4, barHeight + 4, barHeight + 4);

                if (animated) {
                    int shine = (int) ((shimmer / 200f) * Math.max(1, fill));
                    g2.setColor(Colors.alpha(Color.WHITE, 55));
                    g2.fillRoundRect(Math.min(fill - 6, shine), y,
                            Math.min(28, fill), barHeight, barHeight, barHeight);
                }
            }

            if (showPercentage) {
                g2.setFont(getFont());
                g2.setColor(Colors.PRIMARY_BRIGHT);
                FontMetrics fm = g2.getFontMetrics();
                String text = value + "%";
                int tx = barWidth + (labelWidth - fm.stringWidth(text)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, tx, ty);
            }

            if (animated && value < 100 && isShowing() && !timer.isRunning()) {
                timer.start();
            } else if ((!animated || value >= 100) && timer.isRunning()) {
                timer.stop();
            }
        } finally {
            g2.dispose();
        }
    }
}
