package com.reandroid.apkeditor.gui.editor;

import com.reandroid.apkeditor.gui.model.SourceFile;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Editor de código real basado en JTextPane + StyledDocument.
 * Incluye numeración de líneas, línea actual y resaltado Smali.
 */
public final class CodeEditor extends JPanel {
    private final SourceFile sourceFile;
    private final JTextPane textPane = new JTextPane();
    private final JScrollPane scrollPane;
    private final LineNumberGutter gutter;
    private final Timer highlightTimer;
    private Object lineHighlight;
    private Consumer<int[]> caretConsumer;

    public CodeEditor(SourceFile sourceFile) {
        super(new BorderLayout());
        this.sourceFile = sourceFile;
        setBackground(Colors.SURFACE);

        textPane.setFont(Fonts.CODE);
        textPane.setForeground(Colors.TEXT);
        textPane.setBackground(Colors.SURFACE);
        textPane.setCaretColor(Colors.PRIMARY_BRIGHT);
        textPane.setSelectionColor(Colors.PANEL_HOVER);
        textPane.setSelectedTextColor(Colors.TEXT);
        textPane.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        textPane.setMargin(new Insets(8, 10, 8, 10));
        textPane.getDocument().putProperty(PlainDocument.tabSizeAttribute, 4);

        scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Colors.SURFACE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        gutter = new LineNumberGutter(textPane, scrollPane);
        scrollPane.setRowHeaderView(gutter);

        textPane.setText(sourceFile.getContent());
        doc().putProperty(PlainDocument.tabSizeAttribute, 4);

        highlightTimer = new Timer(130, e -> {
            SmaliHighlighter.highlight(textPane, sourceFile.getLanguage());
            gutter.refresh();
            applyLineHighlight();
        });
        highlightTimer.setRepeats(false);

        installListeners();
        add(scrollPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            SmaliHighlighter.highlight(textPane, sourceFile.getLanguage());
            gutter.refresh();
            textPane.setCaretPosition(0);
            applyLineHighlight();
        });
    }

    private void installListeners() {
        doc().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Cambios de estilo no requieren re-resaltado.
            }
        });
        textPane.addCaretListener(e -> {
            gutter.repaint();
            applyLineHighlight();
            if (caretConsumer != null) {
                caretConsumer.accept(getLineColumn());
            }
        });
        scrollPane.getViewport().addChangeListener(e -> gutter.repaint());
        textPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                textPane.requestFocusInWindow();
            }
        });
    }

    private void changed() {
        gutter.refresh();
        highlightTimer.restart();
    }

    private void applyLineHighlight() {
        Highlighter highlighter = textPane.getHighlighter();
        if (lineHighlight != null) {
            highlighter.removeHighlight(lineHighlight);
            lineHighlight = null;
        }
        int position = textPane.getCaretPosition();
        Element root = doc().getDefaultRootElement();
        int index = root.getElementIndex(position);
        Element line = root.getElement(index);
        int start = line.getStartOffset();
        int end = Math.min(line.getEndOffset(), doc().getLength());
        if (end > start) {
            try {
                lineHighlight = highlighter.addHighlight(start, end,
                        new DefaultHighlighter.DefaultHighlightPainter(
                                Colors.alpha(Colors.PRIMARY, 16)));
            } catch (BadLocationException ignored) {
            }
        }
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public JTextPane getTextPane() {
        return textPane;
    }

    public StyledDocument doc() {
        return (StyledDocument) textPane.getDocument();
    }

    public void setCaretConsumer(Consumer<int[]> caretConsumer) {
        this.caretConsumer = caretConsumer;
    }

    public void addCaretListener(CaretListener listener) {
        textPane.addCaretListener(listener);
    }

    public int[] getLineColumn() {
        int position = textPane.getCaretPosition();
        Element root = doc().getDefaultRootElement();
        int line = root.getElementIndex(position);
        int start = root.getElement(line).getStartOffset();
        return new int[]{line + 1, position - start + 1};
    }

    public void goToLine(int oneBasedLine) {
        Element root = doc().getDefaultRootElement();
        int line = Math.max(0, Math.min(root.getElementCount() - 1, oneBasedLine - 1));
        int offset = root.getElement(line).getStartOffset();
        textPane.setCaretPosition(offset);
        textPane.requestFocusInWindow();
    }

    public boolean find(String query, boolean forward) {
        if (query == null || query.isEmpty()) {
            return false;
        }
        String text = textPane.getText();
        int caret = textPane.getCaretPosition();
        int index;
        if (forward) {
            index = text.indexOf(query, caret);
            if (index < 0) {
                index = text.indexOf(query);
            }
        } else {
            index = text.lastIndexOf(query, Math.max(0, caret - 1));
            if (index < 0) {
                index = text.lastIndexOf(query);
            }
        }
        if (index < 0) {
            return false;
        }
        textPane.select(index, index + query.length());
        return true;
    }

    public void copy() {
        String selected = textPane.getSelectedText();
        if (selected == null || selected.isEmpty()) {
            selected = textPane.getText();
        }
        java.awt.datatransfer.StringSelection selection =
                new java.awt.datatransfer.StringSelection(selected);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(selection, selection);
    }

    /**
     * Gutter con numeración y desplazamiento sincronizado con el viewport.
     */
    private static final class LineNumberGutter extends JComponent {
        private final JTextPane textPane;
        private final JScrollPane scrollPane;
        private int lastDigits;

        private LineNumberGutter(JTextPane textPane, JScrollPane scrollPane) {
            this.textPane = textPane;
            this.scrollPane = scrollPane;
            setOpaque(false);
            setPreferredSize(new Dimension(48, 0));
        }

        private void refresh() {
            int lines = textPane.getDocument().getDefaultRootElement().getElementCount();
            int digits = Math.max(2, String.valueOf(lines).length());
            if (digits != lastDigits) {
                lastDigits = digits;
                FontMetrics fm = getFontMetrics(Fonts.MONO_SMALL);
                setPreferredSize(new Dimension(16 + fm.charWidth('0') * digits, 0));
                revalidate();
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Colors.BACKGROUND_SECONDARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Colors.BORDER);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

                FontMetrics fm = g2.getFontMetrics(Fonts.MONO_SMALL);
                g2.setFont(Fonts.MONO_SMALL);
                int lineHeight = Math.max(1, fm.getHeight());
                int viewY = scrollPane.getViewport().getViewPosition().y;
                int startLine = Math.max(0, viewY / lineHeight);
                int endLine = startLine + getHeight() / lineHeight + 2;
                int total = textPane.getDocument().getDefaultRootElement().getElementCount();
                int caretLine = textPane.getDocument().getDefaultRootElement()
                        .getElementIndex(textPane.getCaretPosition());

                for (int line = startLine; line < Math.min(total, endLine); line++) {
                    int y = (line * lineHeight) - viewY + fm.getAscent() + 6;
                    if (y < -lineHeight || y > getHeight() + lineHeight) {
                        continue;
                    }
                    g2.setColor(line == caretLine ? Colors.PRIMARY_BRIGHT : Colors.TEXT_MUTED);
                    g2.setFont(line == caretLine
                            ? Fonts.MONO_SMALL.deriveFont(Font.BOLD)
                            : Fonts.MONO_SMALL);
                    String value = String.valueOf(line + 1);
                    int x = getWidth() - 10 - fm.stringWidth(value);
                    g2.drawString(value, x, y);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
