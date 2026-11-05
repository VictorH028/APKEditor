package com.reandroid.apkeditor.gui.editor;

import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.components.SearchField;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.model.SourceFile;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Pestañas de edición con editor real por documento y barra de estado.
 */
public final class EditorTabs extends JPanel {
    private final AppState state;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final Map<String, CodeEditor> editors = new LinkedHashMap<>();
    private final Map<String, TabComponent> tabs = new LinkedHashMap<>();
    private final JLabel caretLabel = new JLabel("Ln 1, Col 1");
    private final JLabel languageLabel = new JLabel("Smali");
    private final JLabel spacesLabel = new JLabel("Spaces: 4");
    private final JLabel encodingLabel = new JLabel("UTF-8");
    private final SearchField searchField = new SearchField("Buscar en el archivo...");
    private final JPanel searchBar = new JPanel(new BorderLayout());
    private final JPanel emptyState = new JPanel(new GridBagLayout());
    private String activeKey;
    private final List<Consumer<SourceFile>> activeFileListeners = new ArrayList<>();

    public EditorTabs(AppState state, SourceFile initial) {
        super(new BorderLayout());
        this.state = state;
        setBackground(Colors.SURFACE);

        tabBar.setOpaque(false);
        tabBar.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(9, 12, 7, 12));
        header.add(tabBar, BorderLayout.CENTER);
        header.add(createToolbar(), BorderLayout.EAST);

        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(0, 12, 8, 12));
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.setVisible(false);
        searchField.getField().addActionListener(e -> findNext());
        searchField.getField().getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hideSearch");
        searchField.getField().getActionMap().put("hideSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSearch(false);
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(searchBar, BorderLayout.SOUTH);

        content.setOpaque(false);
        emptyState.setOpaque(false);
        JLabel empty = new JLabel("Abre un archivo desde el explorador", SwingConstants.CENTER);
        empty.setForeground(Colors.TEXT_MUTED);
        empty.setFont(Fonts.BODY);
        emptyState.add(empty);
        content.add(emptyState, "__empty__");

        add(top, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        List<SourceFile> previous = state.getOpenFiles();
        if (!previous.isEmpty()) {
            for (SourceFile file : previous) {
                openInternal(file, false);
            }
            String selected = state.getSelectedFilePath();
            if (selected != null && editors.containsKey(selected)) {
                select(selected);
            } else if (!editors.isEmpty()) {
                select(editors.keySet().iterator().next());
            }
        } else if (initial != null) {
            openInternal(initial, true);
        }
        updateEmptyState();
    }

    public void addActiveFileListener(Consumer<SourceFile> listener) {
        activeFileListeners.add(listener);
    }

    private JComponent createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        toolbar.setOpaque(false);
        NeonButton search = iconButton("search", "Buscar en el archivo (Ctrl+F)");
        search.addActionListener(e -> toggleSearch(true));
        NeonButton copy = iconButton("copy", "Copiar selección");
        copy.addActionListener(e -> {
            CodeEditor editor = getActiveEditor();
            if (editor != null) {
                editor.copy();
            }
        });
        NeonButton more = iconButton("settings", "Opciones del editor");
        more.addActionListener(e -> showEditorMenu(more));
        toolbar.add(search);
        toolbar.add(copy);
        toolbar.add(more);
        return toolbar;
    }

    private NeonButton iconButton(String icon, String tooltip) {
        NeonButton button = new NeonButton(IconFactory.icon(icon, 15, Colors.TEXT_SECONDARY));
        button.setVariant(NeonButton.Variant.GHOST);
        button.setCompact(true);
        button.setToolTipText(tooltip);
        return button;
    }

    private JComponent createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(true);
        bar.setBackground(Colors.BACKGROUND_SECONDARY);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Colors.BORDER),
                new EmptyBorder(4, 12, 4, 12)));

        caretLabel.setFont(Fonts.MONO_SMALL);
        caretLabel.setForeground(Colors.TEXT_SECONDARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        for (JLabel label : new JLabel[]{spacesLabel, encodingLabel, languageLabel}) {
            label.setFont(Fonts.MONO_SMALL);
            label.setForeground(Colors.TEXT_MUTED);
        }
        languageLabel.setForeground(Colors.PRIMARY_BRIGHT);
        right.add(spacesLabel);
        right.add(separator());
        right.add(encodingLabel);
        right.add(separator());
        right.add(languageLabel);

        bar.add(caretLabel, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JLabel separator() {
        JLabel label = new JLabel("•");
        label.setForeground(Colors.TEXT_MUTED);
        label.setFont(Fonts.TINY);
        return label;
    }

    public void open(SourceFile file) {
        openInternal(file, true);
    }

    private void openInternal(SourceFile file, boolean activate) {
        if (file == null) {
            return;
        }
        String key = file.getPath();
        if (editors.containsKey(key)) {
            if (activate) {
                select(key);
            }
            return;
        }
        CodeEditor editor = new CodeEditor(file);
        editors.put(key, editor);
        content.add(editor, key);

        TabComponent tab = new TabComponent(file);
        tabs.put(key, tab);
        tabBar.add(tab);
        tabBar.revalidate();
        tabBar.repaint();

        editor.setCaretConsumer(values -> {
            if (key.equals(activeKey)) {
                updateCaret(values);
            }
        });
        installEditorKeys(editor);

        state.addOpenFile(file);
        state.setSelectedFilePath(key);

        if (activate) {
            select(key);
        }
        updateEmptyState();
    }

    private void installEditorKeys(CodeEditor editor) {
        JTextPane pane = editor.getTextPane();
        pane.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "find");
        pane.getActionMap().put("find", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSearch(true);
            }
        });
        pane.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK), "goto");
        pane.getActionMap().put("goto", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gotoLineDialog();
            }
        });
        pane.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hideSearch");
        pane.getActionMap().put("hideSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSearch(false);
            }
        });
    }

    public void close(SourceFile file) {
        if (file == null) {
            return;
        }
        String key = file.getPath();
        CodeEditor editor = editors.remove(key);
        TabComponent tab = tabs.remove(key);
        if (editor == null) {
            return;
        }
        content.remove(editor);
        if (tab != null) {
            tabBar.remove(tab);
        }
        if (key.equals(activeKey)) {
            activeKey = null;
            if (!editors.isEmpty()) {
                select(editors.keySet().iterator().next());
            }
        }
        state.setOpenFiles(new ArrayList<>(editors.values().stream()
                .map(CodeEditor::getSourceFile).toList()));
        state.setSelectedFilePath(activeKey);
        tabBar.revalidate();
        tabBar.repaint();
        updateEmptyState();
    }

    public void closeActive() {
        CodeEditor editor = getActiveEditor();
        if (editor != null) {
            close(editor.getSourceFile());
        }
    }

    public void select(String key) {
        CodeEditor editor = editors.get(key);
        if (editor == null) {
            return;
        }
        activeKey = key;
        cards.show(content, key);
        for (Map.Entry<String, TabComponent> entry : tabs.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(key));
        }
        SourceFile file = editor.getSourceFile();
        languageLabel.setText(displayLanguage(file.getLanguage()));
        state.setSelectedFilePath(key);
        updateCaret(editor.getLineColumn());
        for (Consumer<SourceFile> listener : activeFileListeners) {
            listener.accept(file);
        }
    }

    public void selectFile(String path) {
        if (path != null && editors.containsKey(path)) {
            select(path);
        }
    }

    public CodeEditor getActiveEditor() {
        return activeKey == null ? null : editors.get(activeKey);
    }

    public List<SourceFile> getOpenFiles() {
        return editors.values().stream().map(CodeEditor::getSourceFile).toList();
    }

    private void updateCaret(int[] values) {
        caretLabel.setText("Ln " + values[0] + ", Col " + values[1]);
    }

    private void toggleSearch(boolean show) {
        searchBar.setVisible(show);
        revalidate();
        repaint();
        if (show) {
            searchField.getField().requestFocusInWindow();
            searchField.getField().selectAll();
        } else {
            CodeEditor editor = getActiveEditor();
            if (editor != null) {
                editor.getTextPane().requestFocusInWindow();
            }
        }
    }

    private void findNext() {
        CodeEditor editor = getActiveEditor();
        if (editor == null) {
            return;
        }
        String query = searchField.getText();
        if (query == null || query.isBlank()) {
            return;
        }
        if (!editor.find(query, true)) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        }
    }

    private void gotoLineDialog() {
        CodeEditor editor = getActiveEditor();
        if (editor == null) {
            return;
        }
        String value = JOptionPane.showInputDialog(this, "Número de línea:",
                "Ir a línea", JOptionPane.PLAIN_MESSAGE);
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            editor.goToLine(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
        }
    }

    private void showEditorMenu(Component anchor) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Colors.PANEL_SECONDARY);
        menu.setBorder(BorderFactory.createLineBorder(Colors.BORDER));
        menu.add(menuItem("Ir a línea...", e -> gotoLineDialog()));
        menu.add(menuItem("Seleccionar todo", e -> {
            CodeEditor editor = getActiveEditor();
            if (editor != null) {
                editor.getTextPane().selectAll();
            }
        }));
        menu.addSeparator();
        menu.add(menuItem("Cerrar pestaña", e -> closeActive()));
        menu.show(anchor, 0, anchor.getHeight());
    }

    private JMenuItem menuItem(String text, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(Colors.PANEL_SECONDARY);
        item.setForeground(Colors.TEXT);
        item.setFont(Fonts.BODY);
        item.addActionListener(listener);
        return item;
    }

    private void updateEmptyState() {
        if (editors.isEmpty()) {
            cards.show(content, "__empty__");
        }
    }

    private String displayLanguage(String language) {
        return switch (language == null ? "" : language) {
            case "smali" -> "Smali";
            case "xml" -> "XML";
            case "json" -> "JSON";
            case "hex" -> "Hex";
            case "java" -> "Java";
            default -> "Text";
        };
    }

    private final class TabComponent extends JPanel {
        private final SourceFile file;
        private boolean active;

        private TabComponent(SourceFile file) {
            super(new FlowLayout(FlowLayout.LEFT, 6, 0));
            this.file = file;
            setOpaque(false);
            setBorder(new EmptyBorder(6, 10, 6, 7));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(file.getPath());

            JLabel icon = new JLabel(IconFactory.icon(iconFor(file), 13,
                    active ? Colors.PRIMARY_BRIGHT : Colors.TEXT_MUTED));
            JLabel name = new JLabel(file.getName());
            name.setFont(Fonts.SMALL);
            name.setForeground(Colors.TEXT_SECONDARY);

            JLabel close = new JLabel(IconFactory.icon("close", 11, Colors.TEXT_MUTED));
            close.setBorder(new EmptyBorder(0, 4, 0, 0));
            close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            close.setToolTipText("Cerrar " + file.getName());
            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    close(file);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    close.setIcon(IconFactory.icon("close", 11, Colors.ERROR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(IconFactory.icon("close", 11,
                            active ? Colors.PRIMARY_BRIGHT : Colors.TEXT_MUTED));
                }
            });

            add(icon);
            add(name);
            add(close);

            MouseAdapter selectListener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        select(TabComponent.this.file.getPath());
                    }
                }
            };
            addMouseListener(selectListener);
            icon.addMouseListener(selectListener);
            name.addMouseListener(selectListener);
            setActive(false);
        }

        private String iconFor(SourceFile file) {
            return switch (file.getLanguage()) {
                case "xml" -> "file";
                case "hex" -> "archive";
                default -> "file-code";
            };
        }

        private void setActive(boolean active) {
            this.active = active;
            if (getComponentCount() >= 3) {
                ((JLabel) getComponent(0)).setIcon(IconFactory.icon(iconFor(file), 13,
                        active ? Colors.PRIMARY_BRIGHT : Colors.TEXT_MUTED));
                ((JLabel) getComponent(1)).setForeground(
                        active ? Colors.PRIMARY_BRIGHT : Colors.TEXT_SECONDARY);
                ((JLabel) getComponent(2)).setIcon(IconFactory.icon("close", 11,
                        active ? Colors.PRIMARY_BRIGHT : Colors.TEXT_MUTED));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(Colors.PANEL_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                    g2.setColor(Colors.alpha(Colors.PRIMARY, 100));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);
                    g2.setColor(Colors.PRIMARY_BRIGHT);
                    g2.fillRoundRect(8, getHeight() - 3, getWidth() - 16, 2, 2, 2);
                } else {
                    g2.setColor(Colors.alpha(Colors.PANEL_HOVER, 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                }
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
