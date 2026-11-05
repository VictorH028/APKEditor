package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Tarjeta de acción con icono, título, descripción y estado hover.
 */
public final class ActionTile extends JPanel {
    private final JLabel iconLabel = new JLabel();
    private final JLabel titleLabel = new JLabel();
    private final JLabel descriptionLabel = new JLabel();
    private final String iconName;
    private final List<ActionListener> actionListeners = new ArrayList<>();
    private boolean hover;

    public ActionTile(String icon, String title, String description) {
        super(new BorderLayout(0, 4));
        this.iconName = icon;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        iconLabel.setIcon(IconFactory.icon(icon, 20, Colors.PRIMARY));
        titleLabel.setText(title);
        titleLabel.setFont(Fonts.BODY.deriveFont(Font.BOLD));
        titleLabel.setForeground(Colors.TEXT);
        descriptionLabel.setText("<html>" + description + "</html>");
        descriptionLabel.setFont(Fonts.SMALL);
        descriptionLabel.setForeground(Colors.TEXT_MUTED);

        JPanel text = new JPanel(new BorderLayout(0, 2));
        text.setOpaque(false);
        text.add(titleLabel, BorderLayout.NORTH);
        text.add(descriptionLabel, BorderLayout.CENTER);

        add(iconLabel, BorderLayout.NORTH);
        add(text, BorderLayout.CENTER);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    for (ActionListener listener : actionListeners) {
                        listener.actionPerformed(new java.awt.event.ActionEvent(
                                ActionTile.this, java.awt.event.ActionEvent.ACTION_PERFORMED, "click"));
                    }
                }
            }
        });
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? Colors.PANEL_HOVER : Colors.alpha(Colors.PANEL_SECONDARY, 170));
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 9, 9);
            g2.setColor(hover ? Colors.BORDER_ACTIVE : Colors.BORDER);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 9, 9);
            iconLabel.setIcon(IconFactory.icon(iconName, 20,
                    hover ? Colors.PRIMARY_BRIGHT : Colors.PRIMARY));
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
