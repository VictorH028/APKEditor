package com.reandroid.apkeditor.gui.tree;

import com.reandroid.apkeditor.gui.components.PanelCard;
import com.reandroid.apkeditor.gui.components.SearchField;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.FileNode;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Explorador de archivos funcional: expande, contrae, filtra y abre
 * documentos reales en el editor.
 */
public final class FileExplorer extends PanelCard {
    private final SearchField search = new SearchField(I18n.t("explorer.search"));
    private final JTree tree = new JTree();
    private final Consumer<FileNode> onOpen;
    private final Set<String> expandedPaths = new HashSet<>();
    private FileNode originalRoot;
    private String selectedPath;
    private boolean restoring;

    public FileExplorer(FileNode root, Consumer<FileNode> onOpen) {
        super(I18n.t("explorer.title"), "folder");
        this.onOpen = onOpen;
        getTitleLabel().setFont(Fonts.SECTION);
        setSubtitle(I18n.t("explorer.subtitle"));

        tree.setOpaque(false);
        tree.setRowHeight(26);
        tree.setFont(Fonts.BODY);
        tree.setBackground(new Color(0, 0, 0, 0));
        tree.setForeground(Colors.TEXT);
        tree.setCellRenderer(new ExplorerRenderer());
        tree.setShowsRootHandles(true);
        tree.setToggleClickCount(1);
        tree.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        tree.getSelectionModel().setSelectionMode(
                javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setToolTipText("");

        tree.addTreeSelectionListener(e -> {
            if (restoring) {
                return;
            }
            TreePath path = tree.getSelectionPath();
            if (path == null) {
                return;
            }
            Object value = path.getLastPathComponent();
            if (value instanceof FileNode node) {
                selectedPath = node.getSourcePath();
                if (!node.isContainer() && onOpen != null) {
                    onOpen.accept(node);
                }
            }
        });
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                Object value = event.getPath().getLastPathComponent();
                if (value instanceof FileNode node && node.getSourcePath() != null) {
                    expandedPaths.add(node.getSourcePath());
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
                Object value = event.getPath().getLastPathComponent();
                if (value instanceof FileNode node && node.getSourcePath() != null) {
                    expandedPaths.remove(node.getSourcePath());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tree);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        search.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        JPanel body = getBody();
        body.setLayout(new BorderLayout(0, 8));
        body.add(search, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);

        setRoot(root, root != null && "app-debug.apk".equals(root.getName()));
    }

    public JTree getTree() {
        return tree;
    }

    public void setRoot(FileNode root, boolean expandSample) {
        this.originalRoot = root;
        this.tree.setModel(new DefaultTreeModel(root));
        this.expandedPaths.clear();
        this.selectedPath = null;
        restoring = true;
        try {
            if (expandSample && root != null) {
                expandSamplePath();
            } else if (root != null) {
                tree.expandPath(new TreePath(root.getPath()));
            }
        } finally {
            restoring = false;
        }
        applyFilter();
    }

    public void focusSearch() {
        search.getField().requestFocusInWindow();
        search.getField().selectAll();
    }

    public void selectPath(String path) {
        if (path == null || originalRoot == null) {
            return;
        }
        FileNode node = find(originalRoot, path);
        if (node == null) {
            return;
        }
        restoring = true;
        try {
            TreePath treePath = new TreePath(node.getPath());
            tree.expandPath(treePath.getParentPath());
            tree.setSelectionPath(treePath);
            tree.scrollPathToVisible(treePath);
        } finally {
            restoring = false;
        }
        selectedPath = path;
    }

    public void openSelected() {
        TreePath path = tree.getSelectionPath();
        if (path != null && path.getLastPathComponent() instanceof FileNode node
                && !node.isContainer() && onOpen != null) {
            onOpen.accept(node);
        }
    }

    private void expandSamplePath() {
        FileNode root = originalRoot;
        tree.expandPath(new TreePath(root.getPath()));
        expandByName(root, new TreePath(root.getPath()), "smali");
        FileNode smali = root.findChild("smali");
        if (smali != null) {
            FileNode com = smali.findChild("com");
            FileNode example = com == null ? null : com.findChild("example");
            FileNode myapp = example == null ? null : example.findChild("myapp");
            if (myapp != null) {
                tree.expandPath(new TreePath(myapp.getPath()));
                FileNode main = myapp.findChild("MainActivity.smali");
                if (main != null) {
                    TreePath mainPath = new TreePath(main.getPath());
                    tree.setSelectionPath(mainPath);
                    tree.scrollPathToVisible(mainPath);
                    selectedPath = main.getSourcePath();
                }
            }
        }
    }

    private void expandByName(FileNode node, TreePath path, String name) {
        for (FileNode child : node.childNodes()) {
            TreePath childPath = path.pathByAddingChild(child);
            if (child.getName().equals(name) || child.getSourcePath() != null
                    && child.getSourcePath().endsWith("/" + name)) {
                tree.expandPath(childPath);
                return;
            }
        }
    }

    private void applyFilter() {
        if (originalRoot == null) {
            return;
        }
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        FileNode root = query.isEmpty() ? originalRoot : filter(originalRoot, query);
        if (root == null) {
            root = originalRoot.shallowCopy();
        }
        restoring = true;
        try {
            tree.setModel(new DefaultTreeModel(root));
            restoreExpanded(root, new TreePath(root.getPath()));
            if (selectedPath != null) {
                FileNode selected = find(root, selectedPath);
                if (selected != null) {
                    TreePath path = new TreePath(selected.getPath());
                    tree.expandPath(path.getParentPath());
                    tree.setSelectionPath(path);
                }
            }
        } finally {
            restoring = false;
        }
    }

    private void restoreExpanded(FileNode root, TreePath path) {
        if (expandedPaths.contains(root.getSourcePath())) {
            tree.expandPath(path);
        }
        for (FileNode child : root.childNodes()) {
            restoreExpanded(child, path.pathByAddingChild(child));
        }
    }

    private FileNode filter(FileNode node, String query) {
        boolean selfMatch = node.getName().toLowerCase(Locale.ROOT).contains(query);
        if (!node.isContainer()) {
            return selfMatch ? node.shallowCopy() : null;
        }
        FileNode copy = null;
        for (FileNode child : node.childNodes()) {
            FileNode filtered = filter(child, query);
            if (filtered != null) {
                if (copy == null) {
                    copy = node.shallowCopy();
                }
                copy.add(filtered);
            }
        }
        if (copy != null) {
            return copy;
        }
        return selfMatch ? node.shallowCopy() : null;
    }

    private FileNode find(FileNode node, String path) {
        if (path.equals(node.getSourcePath())) {
            return node;
        }
        for (FileNode child : node.childNodes()) {
            FileNode result = find(child, path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private final class ExplorerRenderer extends DefaultTreeCellRenderer {
        private ExplorerRenderer() {
            setOpaque(false);
            setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
            setBackgroundSelectionColor(Colors.PANEL_HOVER);
            setTextNonSelectionColor(Colors.TEXT_SECONDARY);
            setTextSelectionColor(Colors.PRIMARY_BRIGHT);
            setBorderSelectionColor(null);
            setFont(Fonts.BODY);
            setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 6));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, false);
            setFont(Fonts.BODY);
            setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 6));
            if (value instanceof FileNode node) {
                setText(node.getName());
                setIcon(iconFor(node, expanded));
                setToolTipText(node.getSourcePath());
                if (selected) {
                    setForeground(Colors.PRIMARY_BRIGHT);
                } else if (node.isContainer()) {
                    setForeground(Colors.TEXT);
                } else {
                    setForeground(Colors.TEXT_SECONDARY);
                }
            }
            return this;
        }

        private Icon iconFor(FileNode node, boolean expanded) {
            Color color = switch (node.getType()) {
                case SMALI -> Colors.PRIMARY;
                case XML -> Colors.WARNING;
                case DEX -> Colors.MAGENTA;
                case RESOURCE -> Colors.PURPLE;
                case IMAGE -> Colors.SUCCESS;
                case LIBRARY -> Colors.WARNING;
                case MANIFEST -> Colors.CYAN;
                case ASSETS -> Colors.TEXT_SECONDARY;
                case ARCHIVE -> Colors.PRIMARY_BRIGHT;
                case FOLDER -> expanded ? Colors.PRIMARY : Colors.TEXT_SECONDARY;
                default -> Colors.TEXT_MUTED;
            };
            String icon = switch (node.getType()) {
                case ARCHIVE -> "archive";
                case FOLDER -> expanded ? "folder-open" : "folder";
                case SMALI, XML -> "file-code";
                case MANIFEST -> "file";
                case IMAGE -> "layers";
                case LIBRARY -> "blocks";
                case DEX -> "binary";
                case RESOURCE -> "database";
                default -> "file";
            };
            return IconFactory.icon(icon, 15, color);
        }
    }
}
