package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Contenedor glass/neon reutilizable con cabecera y acciones.
 */
public class PanelCard extends JPanel {
    private final JPanel header = new JPanel(new BorderLayout(8, 0));
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    private final JPanel body = new JPanel(new BorderLayout());
    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final JLabel iconLabel = new JLabel();
    private Color background;
    private Color borderColor;
    private int radius = 12;

    public PanelCard(String title, String iconName) {
        this(title, iconName, null);
    }

    public PanelCard(String title, String iconName, String subtitle) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        background = Colors.SURFACE;
        borderColor = Colors.BORDER;

        iconLabel.setIcon(IconFactory.icon(iconName, 17, Colors.PRIMARY));
        titleLabel.setText(title);
        titleLabel.setFont(Fonts.SECTION);
        titleLabel.setForeground(Colors.TEXT);

        subtitleLabel.setText(subtitle == null ? "" : subtitle);
        subtitleLabel.setFont(Fonts.TINY);
        subtitleLabel.setForeground(Colors.TEXT_MUTED);
        subtitleLabel.setVisible(subtitle != null && !subtitle.isBlank());

        JPanel titles = new JPanel(new BorderLayout(0, 1));
        titles.setOpaque(false);
        titles.add(titleLabel, BorderLayout.NORTH);
        titles.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(iconLabel);
        left.add(titles);

        actions.setOpaque(false);
        header.setOpaque(false);
        header.add(left, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        body.setOpaque(false);
        super.add(header, BorderLayout.NORTH);
        super.add(body, BorderLayout.CENTER);
    }

    public JPanel getBody() {
        return body;
    }

    public JPanel getHeader() {
        return header;
    }

    public JPanel getActions() {
        return actions;
    }

    public JLabel getTitleLabel() {
        return titleLabel;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle == null ? "" : subtitle);
        subtitleLabel.setVisible(subtitle != null && !subtitle.isBlank());
    }

    public void setIcon(String iconName) {
        iconLabel.setIcon(IconFactory.icon(iconName, 17, Colors.PRIMARY));
    }

    public void addHeaderAction(JComponent component) {
        actions.add(component);
    }

    public void setBackgroundColor(Color background) {
        this.background = background;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
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
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            Paint oldPaint = g2.getPaint();
            g2.setPaint(new GradientPaint(0, 0, Colors.alpha(Colors.PRIMARY, 30),
                    0, Math.min(70, getHeight()), Colors.alpha(Colors.PRIMARY, 0)));
            g2.drawLine(radius, 1, Math.max(radius, getWidth() - radius), 1);
            g2.setPaint(oldPaint);

            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
