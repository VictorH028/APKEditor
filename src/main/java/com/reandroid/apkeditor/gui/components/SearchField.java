package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Campo de búsqueda con icono y placeholder dibujado, sin dependencias externas.
 */
public final class SearchField extends JPanel {
    private final PlaceholderField field;

    public SearchField(String placeholder) {
        super(new BorderLayout(6, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));

        JLabel icon = new JLabel(IconFactory.icon("search", 15, Colors.TEXT_MUTED));
        field = new PlaceholderField(placeholder);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setFont(Fonts.SMALL);
        field.setForeground(Colors.TEXT);
        field.setCaretColor(Colors.PRIMARY);
        field.setSelectionColor(Colors.PANEL_HOVER);
        field.setSelectedTextColor(Colors.PRIMARY_BRIGHT);

        add(icon, BorderLayout.WEST);
        add(field, BorderLayout.CENTER);
    }

    public JTextField getField() {
        return field;
    }

    public String getText() {
        return field.getText();
    }

    public void setText(String text) {
        field.setText(text);
    }

    public void addDocumentListener(DocumentListener listener) {
        field.getDocument().addDocumentListener(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Colors.BACKGROUND_SECONDARY);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);
            g2.setColor(field.hasFocus() ? Colors.BORDER_ACTIVE : Colors.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    private static final class PlaceholderField extends JTextField {
        private final String placeholder;

        private PlaceholderField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(Colors.TEXT_MUTED);
                    g2.setFont(getFont());
                    Insets insets = getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(placeholder, insets.left + 2, y);
                } finally {
                    g2.dispose();
                }
            }
        }
    }
}
