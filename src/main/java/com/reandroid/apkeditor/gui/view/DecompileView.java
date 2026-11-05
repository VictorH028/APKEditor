package com.reandroid.apkeditor.gui.view;

import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.editor.EditorTabs;
import com.reandroid.apkeditor.gui.log.LogPanel;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import com.reandroid.apkeditor.gui.model.FileNode;
import com.reandroid.apkeditor.gui.model.SourceFile;
import com.reandroid.apkeditor.gui.panel.BottomDock;
import com.reandroid.apkeditor.gui.service.ApkService;
import com.reandroid.apkeditor.gui.service.LogService;
import com.reandroid.apkeditor.gui.service.OperationService;
import com.reandroid.apkeditor.gui.service.SampleProject;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;
import com.reandroid.apkeditor.gui.tree.FileExplorer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Espacio de trabajo principal de descompilación: explorador, editor,
 * logs, progreso, información y estadísticas.
 */
public final class DecompileView extends JPanel {
    private final AppState state;
    private final OperationService operations;
    private final LogService logService;
    private final Consumer<File> apkLoader;

    private final FileExplorer explorer;
    private final EditorTabs editor;
    private final LogPanel logPanel;
    private final BottomDock bottom = new BottomDock();
    private final JLabel subtitle = new JLabel();
    private final JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    private final List<WorkspaceTab> tabs = new java.util.ArrayList<>();
    private WorkspaceTab activeTab;
    private Timer activityTimer;

    private final PropertyChangeListener treeListener;
    private final PropertyChangeListener analysisListener;
    private final PropertyChangeListener inputListener;

    public DecompileView(AppState state, OperationService operations,
                         LogService logService, Consumer<File> apkLoader) {
        super(new BorderLayout(0, 14));
        this.state = state;
        this.operations = operations;
        this.logService = logService;
        this.apkLoader = apkLoader;

        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        FileNode initialTree = state.getCurrentTree();
        if (initialTree == null) {
            initialTree = SampleProject.buildTree();
        }
        explorer = new FileExplorer(initialTree, this::openNode);
        SourceFile initialFile = new SourceFile(
                "MainActivity.smali",
                SampleProject.MAIN_ACTIVITY_PATH,
                SampleProject.mainActivity(), "smali", false);
        editor = new EditorTabs(state, initialFile);
        logPanel = new LogPanel(logService);
        bottom.setAnalysis(state.getAnalysis());

        add(createHeader(), BorderLayout.NORTH);
        add(createWorkspace(), BorderLayout.CENTER);

        treeListener = event -> {
            FileNode root = (FileNode) event.getNewValue();
            if (root == null) {
                root = SampleProject.buildTree();
            }
            FileNode finalRoot = root;
            SwingUtilities.invokeLater(() -> explorer.setRoot(finalRoot,
                    finalRoot.getName().equals("app-debug.apk")));
        };
        analysisListener = event -> SwingUtilities.invokeLater(
                () -> bottom.setAnalysis((ApkAnalysis) event.getNewValue()));
        inputListener = event -> SwingUtilities.invokeLater(this::updateSubtitle);

        state.addPropertyChangeListener(AppState.TREE_CHANGED, treeListener);
        state.addPropertyChangeListener(AppState.ANALYSIS_CHANGED, analysisListener);
        state.addPropertyChangeListener(AppState.INPUT_CHANGED, inputListener);
        updateSubtitle();
        setActiveTab("Estructura");
    }

    private JComponent createHeader() {
        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);

        JLabel title = new JLabel("Descompilación y Análisis");
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT);
        subtitle.setFont(Fonts.SMALL);
        subtitle.setForeground(Colors.TEXT_MUTED);

        JPanel titles = new JPanel(new BorderLayout(0, 2));
        titles.setOpaque(false);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);

        NeonButton search = headerButton("search", "Buscar archivos (Ctrl+F)");
        search.addActionListener(e -> explorer.focusSearch());
        NeonButton close = headerButton("close", "Restablecer espacio de trabajo");
        close.addActionListener(e -> resetWorkspace());

        NeonButton run = new NeonButton("Descompilar",
                IconFactory.icon("play", 13, Colors.BACKGROUND));
        run.setVariant(NeonButton.Variant.PRIMARY);
        run.setFont(Fonts.BODY_MEDIUM);
        run.setToolTipText("Iniciar descompilación (F5)");
        run.addActionListener(e -> runDecompile());

        actions.add(search);
        actions.add(close);
        actions.add(run);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(titles, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        tabsPanel.setOpaque(false);
        for (String name : new String[]{"Estructura", "Código", "Manifest", "Recursos", "Análisis"}) {
            WorkspaceTab tab = new WorkspaceTab(name);
            tab.addActionListener(e -> onTab(name));
            tabs.add(tab);
            tabsPanel.add(tab);
        }

        container.add(header, BorderLayout.NORTH);
        container.add(tabsPanel, BorderLayout.SOUTH);
        return container;
    }

    private NeonButton headerButton(String icon, String tooltip) {
        NeonButton button = new NeonButton(IconFactory.icon(icon, 16, Colors.TEXT_SECONDARY));
        button.setVariant(NeonButton.Variant.GHOST);
        button.setCompact(true);
        button.setToolTipText(tooltip);
        return button;
    }

    private JComponent createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(0, 14));
        workspace.setOpaque(false);

        explorer.setMinimumSize(new Dimension(240, 200));
        editor.setMinimumSize(new Dimension(360, 200));
        logPanel.setMinimumSize(new Dimension(260, 200));

        JSplitPane right = splitPane(editor, logPanel, 0.58, 360, 260);
        JSplitPane columns = splitPane(explorer, right, 0.228, 240, 620);

        bottom.setPreferredSize(new Dimension(0, 175));
        bottom.setMinimumSize(new Dimension(0, 175));

        workspace.add(columns, BorderLayout.CENTER);
        workspace.add(bottom, BorderLayout.SOUTH);
        return workspace;
    }

    private JSplitPane splitPane(Component first, Component second,
                                 double ratio, int minFirst, int minSecond) {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, first, second) {
            private int lastWidth = -1;

            @Override
            public void doLayout() {
                int width = getWidth();
                int available = width - getDividerSize();
                if (width > 0 && available > 0) {
                    int current = getDividerLocation();
                    int target;
                    if (lastWidth <= 0) {
                        target = (int) Math.round(available * ratio);
                    } else if (lastWidth != width) {
                        double currentRatio = (double) current
                                / Math.max(1, lastWidth - getDividerSize());
                        target = (int) Math.round(available * currentRatio);
                    } else {
                        target = current;
                    }
                    target = Math.max(minFirst, Math.min(available - minSecond, target));
                    if (target != current) {
                        setDividerLocation(target);
                    }
                    lastWidth = width;
                }
                super.doLayout();
            }
        };
        split.setResizeWeight(ratio);
        split.setDividerSize(14);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(false);
        split.setBackground(Colors.BACKGROUND);
        split.setDividerLocation((int) Math.round(1000 * ratio));
        return split;
    }

    private void onTab(String name) {
        setActiveTab(name);
        switch (name) {
            case "Estructura" -> {
                explorer.getTree().requestFocusInWindow();
                if (state.getSelectedFilePath() != null) {
                    explorer.selectPath(state.getSelectedFilePath());
                }
            }
            case "Código" -> {
                if (editor.getActiveEditor() == null) {
                    openSampleFile();
                } else {
                    editor.getActiveEditor().getTextPane().requestFocusInWindow();
                }
            }
            case "Manifest" -> openNamed("AndroidManifest.xml");
            case "Recursos" -> {
                if (!openNamed("strings.xml")) {
                    if (!openNamed("resources.arsc")) {
                        logService.info("No se encontraron recursos en el árbol actual.");
                    }
                }
            }
            case "Análisis" -> openAnalysisReport();
            default -> {
            }
        }
    }

    private void setActiveTab(String name) {
        for (WorkspaceTab tab : tabs) {
            tab.setActive(tab.getText().equals(name));
            if (tab.getText().equals(name)) {
                activeTab = tab;
            }
        }
        tabsPanel.repaint();
    }

    public void showEditor() {
        setActiveTab("Código");
        onTab("Código");
    }

    public void showStructure() {
        setActiveTab("Estructura");
        onTab("Estructura");
    }

    public void showAnalysis() {
        setActiveTab("Análisis");
        onTab("Análisis");
    }

    public EditorTabs getEditorTabs() {
        return editor;
    }

    public FileExplorer getFileExplorer() {
        return explorer;
    }

    private void openSampleFile() {
        editor.open(new SourceFile("MainActivity.smali",
                SampleProject.MAIN_ACTIVITY_PATH, SampleProject.mainActivity(),
                "smali", false));
    }

    private boolean openNamed(String fileName) {
        FileNode root = state.getCurrentTree();
        if (root == null) {
            root = SampleProject.buildTree();
        }
        FileNode node = findByName(root, fileName);
        if (node == null && "AndroidManifest.xml".equals(fileName)) {
            node = findByName(root, "AndroidManifest.xml");
        }
        if (node != null) {
            explorer.selectPath(node.getSourcePath());
            openNode(node);
            return true;
        }
        return false;
    }

    private FileNode findByName(FileNode node, String name) {
        if (name.equalsIgnoreCase(node.getName())) {
            return node;
        }
        for (FileNode child : node.childNodes()) {
            FileNode result = findByName(child, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private void openNode(FileNode node) {
        if (node == null || node.isContainer()) {
            return;
        }
        setActiveTab("Código");
        if (node.getContent() != null) {
            editor.open(new SourceFile(node.getName(), node.getSourcePath(),
                    node.getContent(), languageOf(node), node.isBinary()));
            return;
        }
        File source = state.getCurrentInput();
        if (source == null || node.getSourcePath() == null) {
            String message = "; " + node.getSourcePath() + "\n"
                    + "; Archivo binario / sin contenido de texto disponible.\n";
            editor.open(new SourceFile(node.getName(), node.getSourcePath(),
                    message, "hex", node.isBinary()));
            return;
        }
        new SwingWorker<SourceFile, Void>() {
            @Override
            protected SourceFile doInBackground() throws Exception {
                return ApkService.read(source, node.getSourcePath());
            }

            @Override
            protected void done() {
                try {
                    SourceFile file = get();
                    if (file == null) {
                        logService.warning("No se pudo abrir: " + node.getSourcePath());
                        return;
                    }
                    editor.open(file);
                    explorer.selectPath(node.getSourcePath());
                } catch (Exception error) {
                    logService.error("Error al leer " + node.getName() + ": "
                            + error.getMessage());
                }
            }
        }.execute();
    }

    private void openAnalysisReport() {
        ApkAnalysis analysis = state.getAnalysis();
        String content = """
                APKEDITOR :: REPORTE DE ANÁLISIS
                =========================================
                Aplicación      : %s
                Paquete         : %s
                Versión         : %s (code %d)
                Min SDK         : %d
                Target SDK      : %d
                Tamaño APK      : %s
                Archivos        : %d
                Archivos Smali  : %d
                Clases          : %d
                Recursos        : %d
                Permisos        : %d
                Componentes     : %d

                Estado del motor
                -----------------------------------------
                Entorno         : listo
                Motor           : APKEditor core
                Interfaz        : Futuristic neon UI
                Editor          : Smali syntax highlighting
                """.formatted(
                analysis.getAppName(),
                analysis.getPackageName(),
                analysis.getVersionName(),
                analysis.getVersionCode(),
                analysis.getMinSdk(),
                analysis.getTargetSdk(),
                analysis.getSizeLabel(),
                analysis.getFileCount(),
                analysis.getSmaliFiles(),
                analysis.getClasses(),
                analysis.getResources(),
                analysis.getPermissions(),
                analysis.getComponents());
        String path = "apkeditor://analysis-report/"
                + Integer.toHexString((analysis.getPackageName() + analysis.getVersionName()
                + analysis.getFileCount() + analysis.getClasses()).hashCode());
        editor.open(new SourceFile("analysis-report.txt", path, content, "text", false));
    }

    public void runDecode() {
        runDecompile();
    }

    private void runDecompile() {
        if (state.isBusy()) {
            logService.warning("Ya hay una operación en curso.");
            return;
        }
        File input = state.getCurrentInput();
        if (input == null || !input.exists()) {
            input = chooseApk();
            if (input == null) {
                return;
            }
            state.setCurrentInput(input);
            if (apkLoader != null) {
                apkLoader.accept(input);
            }
        }
        File output = uniqueOutput(input);

        bottom.getProgressPanel().resetForRun();
        bottom.getProgressPanel().setDescription(
                "Extrayendo recursos, generando código smali y analizando la aplicación...");
        logService.info("Descompilación iniciada: " + input.getName());

        int[] simulated = {4};
        if (activityTimer != null) {
            activityTimer.stop();
        }
        activityTimer = new Timer(420, e -> {
            if (!state.isBusy()) {
                activityTimer.stop();
                return;
            }
            simulated[0] = Math.min(90, simulated[0] + 1);
            updateProgress(simulated[0]);
        });
        activityTimer.start();

        boolean started = operations.execute("decode", input, output,
                busy -> state.setBusy(busy),
                this::updateProgress,
                (success, error) -> {
                    if (activityTimer != null) {
                        activityTimer.stop();
                    }
                    bottom.getProgressPanel().finish(success);
                    if (success) {
                        state.setCurrentOutput(output);
                        bottom.getProgressPanel().setDescription(
                                "Proyecto generado en " + output.getAbsolutePath());
                        logService.success("Proyecto listo: " + output.getAbsolutePath());
                    } else {
                        bottom.getProgressPanel().setDescription(
                                "La descompilación no se completó. Revisa el panel de log.");
                    }
                });
        if (!started) {
            if (activityTimer != null) {
                activityTimer.stop();
            }
            bottom.getProgressPanel().finish(false);
        }
    }

    private void updateProgress(int percent) {
        bottom.getProgressPanel().setProgress(percent);
        if (percent < 18) {
            setSteps(0);
        } else if (percent < 62) {
            setSteps(1);
        } else if (percent < 88) {
            setSteps(2);
        } else {
            setSteps(3);
        }
    }

    private void setSteps(int active) {
        for (int i = 0; i < 4; i++) {
            if (i < active) {
                bottom.getProgressPanel().setStepStatus(i,
                        com.reandroid.apkeditor.gui.panel.AnalysisProgressPanel.StepStatus.COMPLETED);
            } else if (i == active) {
                bottom.getProgressPanel().setStepStatus(i,
                        com.reandroid.apkeditor.gui.panel.AnalysisProgressPanel.StepStatus.ACTIVE);
            } else {
                bottom.getProgressPanel().setStepStatus(i,
                        com.reandroid.apkeditor.gui.panel.AnalysisProgressPanel.StepStatus.PENDING);
            }
        }
    }

    private File chooseApk() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar APK");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "APK / ZIP / AAB", "apk", "zip", "aab"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private File uniqueOutput(File input) {
        File parent = input.getParentFile();
        if (parent == null) {
            parent = new File(".");
        }
        String name = input.getName();
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        File output = new File(parent, name + "_decompile");
        int index = 1;
        while (output.exists()) {
            output = new File(parent, name + "_decompile_" + index);
            index++;
        }
        return output;
    }

    private String languageOf(FileNode node) {
        String name = node.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".smali")) {
            return "smali";
        }
        if (name.endsWith(".xml")) {
            return "xml";
        }
        if (name.endsWith(".json")) {
            return "json";
        }
        if (node.isBinary()) {
            return "hex";
        }
        return "text";
    }

    private void resetWorkspace() {
        if (state.isBusy()) {
            logService.warning("Espera a que termine la operación en curso.");
            return;
        }
        state.setCurrentInput(null);
        state.setCurrentOutput(null);
        state.setAnalysis(ApkAnalysis.demo());
        state.setCurrentTree(SampleProject.buildTree());
        logService.info("Espacio de trabajo restablecido.");
    }

    private void updateSubtitle() {
        File input = state.getCurrentInput();
        if (input == null) {
            subtitle.setText("app-debug.apk  ·  espacio de demostración");
            subtitle.setToolTipText(null);
        } else {
            subtitle.setText(input.getName() + "  ·  " + input.getAbsolutePath());
            subtitle.setToolTipText(input.getAbsolutePath());
        }
    }

    @Override
    public void removeNotify() {
        if (activityTimer != null) {
            activityTimer.stop();
        }
        state.removePropertyChangeListener(treeListener);
        state.removePropertyChangeListener(analysisListener);
        state.removePropertyChangeListener(inputListener);
        super.removeNotify();
    }

    private final class WorkspaceTab extends JButton {
        private boolean active;

        private WorkspaceTab(String text) {
            super(text);
            setFont(Fonts.BODY_MEDIUM);
            setForeground(Colors.TEXT_SECONDARY);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        }

        private void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Colors.PRIMARY_BRIGHT : Colors.TEXT_SECONDARY);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(Colors.alpha(Colors.NAV_ACTIVE_BG, 150));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                    g2.setColor(Colors.PRIMARY_BRIGHT);
                    g2.fillRoundRect(10, getHeight() - 3, getWidth() - 20, 2, 2, 2);
                } else if (getModel().isRollover()) {
                    g2.setColor(Colors.alpha(Colors.PANEL_HOVER, 120));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                }
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
