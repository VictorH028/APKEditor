package com.reandroid.apkeditor.gui;

import com.reandroid.apkeditor.core.OperationRegistry;
import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.components.SidebarButton;
import com.reandroid.apkeditor.gui.components.StatusDot;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.navigation.Route;
import com.reandroid.apkeditor.gui.panel.AppStatusBar;
import com.reandroid.apkeditor.gui.service.ApkService;
import com.reandroid.apkeditor.gui.service.LogService;
import com.reandroid.apkeditor.gui.service.OperationService;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;
import com.reandroid.apkeditor.gui.theme.Theme;
import com.reandroid.apkeditor.gui.view.DashboardView;
import com.reandroid.apkeditor.gui.view.DecompileView;
import com.reandroid.apkeditor.gui.view.InformationView;
import com.reandroid.apkeditor.gui.view.OperationView;
import com.reandroid.apkeditor.gui.view.SettingsView;
import com.reandroid.apkeditor.operations.OperationCatalog;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ventana principal. Compone la barra superior, el sidebar, las vistas y
 * la barra de estado. No contiene lógica de descompilación.
 */
public final class MainWindow extends JFrame {
    private static final int TOPBAR_HEIGHT = 74;
    private static final int SIDEBAR_WIDTH = 236;
    private static final String WORKSPACE = "WORKSPACE";

    private final OperationRegistry registry;
    private final AppState state = new AppState();
    private final LogService logs = LogService.get();
    private final OperationService operations;
    private final Map<Route, SidebarButton> navButtons = new LinkedHashMap<>();
    private final PropertyChangeListener inputListener;
    private final PropertyChangeListener themeListener;

    private CardLayout cards;
    private JPanel content;
    private DecompileView workspace;
    private NeonButton apkButton;
    private JComboBox<String> themeSelector;
    private Route currentRoute = Route.DECOMPILE;
    private boolean rebuilding;
    private boolean maximized;
    private Rectangle restoredBounds;

    public MainWindow() {
        super("APKEDITOR - Reverse Engineering");
        Theme.install();
        registry = OperationCatalog.create();
        operations = new OperationService(registry, logs);
        logs.seedDemoIfEmpty();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(1200, 800));
        configureWindowBounds();
        buildUI();
        installShortcuts();

        inputListener = event -> SwingUtilities.invokeLater(this::updateApkButton);
        themeListener = event -> SwingUtilities.invokeLater(
                () -> applyTheme((String) event.getNewValue()));
        state.addPropertyChangeListener(AppState.INPUT_CHANGED, inputListener);
        state.addPropertyChangeListener(AppState.THEME_CHANGED, themeListener);
        updateApkButton();
    }

    private void configureWindowBounds() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(1536, Math.max(1000, screen.width - 40));
        int height = Math.min(1024, Math.max(720, screen.height - 70));
        setSize(width, height);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Colors.BACKGROUND);
        root.add(createTopBar(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Colors.BACKGROUND);
        center.add(createSidebar(), BorderLayout.WEST);

        cards = new CardLayout();
        content = new JPanel(cards);
        content.setBackground(Colors.BACKGROUND);
        buildCards();
        center.add(content, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        root.add(new AppStatusBar(state), BorderLayout.SOUTH);
        setContentPane(root);
        revalidate();
        repaint();
        showRoute(currentRoute, false);
    }

    private JComponent createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(18, 0));
        bar.setPreferredSize(new Dimension(0, TOPBAR_HEIGHT));
        bar.setMinimumSize(new Dimension(0, TOPBAR_HEIGHT));
        bar.setBackground(Colors.BACKGROUND_SECONDARY);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.BORDER),
                BorderFactory.createEmptyBorder(0, 18, 0, 14)));

        bar.add(createBrand(), BorderLayout.WEST);
        bar.add(createApkSelector(), BorderLayout.CENTER);
        bar.add(createTopActions(), BorderLayout.EAST);
        installDrag(bar);
        return bar;
    }

    private JComponent createBrand() {
        JLabel logo = new JLabel(IconFactory.icon("package", 28, Colors.PRIMARY));
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JLabel name = new JLabel("APKEDITOR");
        name.setFont(Fonts.TITLE.deriveFont(Font.BOLD, 20f));
        name.setForeground(Colors.TEXT);

        JLabel subtitle = new JLabel("Reverse Engineering");
        subtitle.setFont(Fonts.SMALL);
        subtitle.setForeground(Colors.TEXT_SECONDARY);

        JLabel dot = new JLabel("·");
        dot.setFont(Fonts.SMALL);
        dot.setForeground(Colors.TEXT_MUTED);

        JLabel tagline = new JLabel("Decode · Build · Analyze");
        tagline.setFont(Fonts.TINY);
        tagline.setForeground(Colors.TEXT_MUTED);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        meta.setOpaque(false);
        meta.add(subtitle);
        meta.add(dot);
        meta.add(tagline);

        JPanel text = new JPanel(new BorderLayout(0, 1));
        text.setOpaque(false);
        text.add(name, BorderLayout.NORTH);
        text.add(meta, BorderLayout.SOUTH);

        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.add(logo, BorderLayout.WEST);
        brand.add(text, BorderLayout.CENTER);
        installDrag(brand);
        installDrag(logo);
        installDrag(name);
        installDrag(meta);
        return brand;
    }

    private JComponent createApkSelector() {
        apkButton = new NeonButton("MyApplication.apk",
                IconFactory.icon("file-code", 15, Colors.PRIMARY));
        apkButton.setVariant(NeonButton.Variant.SECONDARY);
        apkButton.setFont(Fonts.BODY_MEDIUM);
        apkButton.setHorizontalAlignment(SwingConstants.LEFT);
        apkButton.setIconTextGap(9);
        apkButton.setPreferredSize(new Dimension(330, 36));
        apkButton.setMaximumSize(new Dimension(380, 36));
        apkButton.setToolTipText("Seleccionar APK (Ctrl+O)");
        apkButton.addActionListener(e -> chooseApk());

        JLabel chevron = new JLabel(IconFactory.icon("chevron-down", 12,
                Colors.TEXT_SECONDARY));
        chevron.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
        chevron.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chevron.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                apkButton.doClick();
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(apkButton, BorderLayout.CENTER);
        wrapper.add(chevron, BorderLayout.EAST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        center.add(wrapper);
        return center;
    }

    private JComponent createTopActions() {
        JLabel themeLabel = new JLabel("TEMA");
        themeLabel.setFont(Fonts.TINY);
        themeLabel.setForeground(Colors.TEXT_MUTED);

        themeSelector = new JComboBox<>(Theme.NAMES);
        themeSelector.setSelectedItem(state.getThemeName());
        themeSelector.setPreferredSize(new Dimension(128, 30));
        themeSelector.setFont(Fonts.SMALL);
        themeSelector.setBackground(Colors.SURFACE_2);
        themeSelector.setForeground(Colors.TEXT);
        themeSelector.setFocusable(false);
        themeSelector.setBorder(BorderFactory.createLineBorder(Colors.BORDER));
        themeSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focus);
                label.setFont(Fonts.SMALL);
                label.setBackground(selected ? Colors.PANEL_HOVER : Colors.SURFACE_2);
                label.setForeground(selected ? Colors.PRIMARY_BRIGHT : Colors.TEXT);
                label.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
                return label;
            }
        });
        themeSelector.addActionListener(e -> {
            if (rebuilding) {
                return;
            }
            Object selected = themeSelector.getSelectedItem();
            if (selected != null && !selected.equals(state.getThemeName())) {
                state.setThemeName(String.valueOf(selected));
            }
        });

        NeonButton minimize = windowButton("minimize", "Minimizar");
        minimize.addActionListener(e -> setState(ICONIFIED));
        NeonButton maximize = windowButton("maximize", "Maximizar");
        maximize.addActionListener(e -> toggleMaximize());
        NeonButton close = windowButton("close", "Cerrar");
        close.addActionListener(e -> dispatchEvent(
                new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);
        actions.add(themeLabel);
        actions.add(themeSelector);
        actions.add(Box.createHorizontalStrut(6));
        actions.add(minimize);
        actions.add(maximize);
        actions.add(close);
        return actions;
    }

    private NeonButton windowButton(String icon, String tooltip) {
        NeonButton button = new NeonButton(IconFactory.icon(icon, 15,
                Colors.TEXT_SECONDARY));
        button.setVariant(NeonButton.Variant.GHOST);
        button.setCompact(true);
        button.setToolTipText(tooltip);
        return button;
    }

    private JComponent createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setBackground(Colors.BACKGROUND_SECONDARY);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Colors.BORDER));

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Colors.BACKGROUND_SECONDARY);
        nav.setBorder(BorderFactory.createEmptyBorder(16, 12, 12, 12));

        addNav(nav, "Dashboard", "house", Route.DASHBOARD);
        addNav(nav, "Descompilar", "file-code", Route.DECOMPILE);
        addNav(nav, "Analizar", "scan-search", Route.ANALYZE);
        addNav(nav, "Smali Viewer", "binary", Route.SMALI_VIEWER);
        addNav(nav, "Build", "package", Route.BUILD);
        addNav(nav, "Merge", "git-merge", Route.MERGE);
        addNav(nav, "Refactor", "workflow", Route.REFACTOR);
        addNav(nav, "Proteger", "shield", Route.PROTECT);
        addNav(nav, "Información", "info", Route.INFORMATION);
        addNav(nav, "Configuración", "settings", Route.SETTINGS);
        nav.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(nav);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        sidebar.add(scroll, BorderLayout.CENTER);
        sidebar.add(createSidebarLogo(), BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNav(JPanel parent, String label, String icon, Route route) {
        SidebarButton button = new SidebarButton(label, icon);
        button.addActionListener(e -> showRoute(route, true));
        navButtons.put(route, button);
        parent.add(button);
        parent.add(Box.createVerticalStrut(2));
    }

    private JComponent createSidebarLogo() {
        JPanel logo = new JPanel();
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setBackground(Colors.BACKGROUND_SECONDARY);
        logo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Colors.BORDER),
                BorderFactory.createEmptyBorder(14, 18, 18, 18)));

        JLabel icon = new JLabel(IconFactory.icon("package", 22, Colors.PRIMARY));
        JLabel name = new JLabel("APKEDITOR");
        name.setFont(Fonts.BODY.deriveFont(Font.BOLD));
        name.setForeground(Colors.TEXT);
        JLabel version = new JLabel("v1.0.0");
        version.setFont(Fonts.TINY);
        version.setForeground(Colors.TEXT_MUTED);
        JLabel description = new JLabel("Reverse engineering en tus manos");
        description.setFont(Fonts.TINY);
        description.setForeground(Colors.TEXT_MUTED);

        icon.setAlignmentX(LEFT_ALIGNMENT);
        name.setAlignmentX(LEFT_ALIGNMENT);
        version.setAlignmentX(LEFT_ALIGNMENT);
        description.setAlignmentX(LEFT_ALIGNMENT);

        logo.add(icon);
        logo.add(Box.createVerticalStrut(7));
        logo.add(name);
        logo.add(version);
        logo.add(Box.createVerticalStrut(4));
        logo.add(description);
        return logo;
    }

    private void buildCards() {
        content.add(new DashboardView(state, this::navigate), Route.DASHBOARD.name());

        workspace = new DecompileView(state, operations, logs, this::loadApk);
        content.add(workspace, WORKSPACE);

        for (Route route : new Route[]{Route.BUILD, Route.MERGE,
                Route.REFACTOR, Route.PROTECT}) {
            String id = route.name().toLowerCase();
            content.add(new OperationView(id, registry.get(id), state, operations, logs),
                    route.name());
        }

        content.add(new InformationView(state, this::navigate), Route.INFORMATION.name());
        content.add(new SettingsView(state, registry), Route.SETTINGS.name());
    }

    public void navigate(Route route) {
        showRoute(route, true);
    }

    private void showRoute(Route route, boolean explicit) {
        if (route == Route.DECODE) {
            route = Route.DECOMPILE;
        }
        if (route == null || content == null) {
            return;
        }
        currentRoute = route;
        switch (route) {
            case DASHBOARD, BUILD, MERGE, REFACTOR, PROTECT, INFORMATION, SETTINGS ->
                    cards.show(content, route.name());
            case ANALYZE -> {
                cards.show(content, WORKSPACE);
                workspace.showAnalysis();
            }
            case SMALI_VIEWER -> {
                cards.show(content, WORKSPACE);
                workspace.showEditor();
            }
            default -> {
                cards.show(content, WORKSPACE);
                if (explicit) {
                    workspace.showStructure();
                }
            }
        }
        for (Map.Entry<Route, SidebarButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey() == route);
        }
        state.setActiveRoute(route.name().toLowerCase());
    }

    private void chooseApk() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar APK");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "APK / ZIP / AAB", "apk", "zip", "aab"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadApk(chooser.getSelectedFile());
        }
    }

    public void loadApk(File file) {
        if (file == null || !file.exists()) {
            logs.warning("El APK seleccionado no existe.");
            return;
        }
        state.setCurrentInput(file);
        logs.info("Cargando APK: " + file.getName());
        new SwingWorker<ApkService.Result, Void>() {
            @Override
            protected ApkService.Result doInBackground() throws Exception {
                return ApkService.analyze(file);
            }

            @Override
            protected void done() {
                try {
                    ApkService.Result result = get();
                    state.setAnalysis(result.getAnalysis());
                    state.setCurrentTree(result.getTree());
                    for (String message : result.getMessages()) {
                        logs.info(message);
                    }
                    logs.success("APK cargado: " + result.getAnalysis().getAppName()
                            + " · " + result.getAnalysis().getFileCount() + " archivos");
                    showRoute(Route.DECOMPILE, true);
                } catch (Exception error) {
                    logs.error("No se pudo analizar el APK: " + shortMessage(error));
                }
            }
        }.execute();
    }

    private void updateApkButton() {
        if (apkButton == null) {
            return;
        }
        File input = state.getCurrentInput();
        apkButton.setText(input == null ? "MyApplication.apk" : input.getName());
        apkButton.setToolTipText(input == null
                ? "Seleccionar APK (Ctrl+O)" : input.getAbsolutePath());
    }

    private void applyTheme(String name) {
        if (rebuilding || name == null || name.isBlank()) {
            return;
        }
        rebuilding = true;
        try {
            Theme.apply(name);
            Theme.install();
            buildUI();
        } finally {
            rebuilding = false;
        }
    }

    private void installShortcuts() {
        JRootPane root = getRootPane();
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "decode",
                e -> {
                    showRoute(Route.DECOMPILE, true);
                    workspace.runDecode();
                });
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK),
                "openApk", e -> chooseApk());
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK),
                "workspace", e -> showRoute(Route.DECOMPILE, true));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_DOWN_MASK),
                "settings", e -> showRoute(Route.SETTINGS, true));
    }

    private void bind(JRootPane root, KeyStroke key, String name,
                      java.awt.event.ActionListener listener) {
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, name);
        root.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        });
    }

    private void installDrag(Component component) {
        MouseAdapter drag = new MouseAdapter() {
            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !maximized) {
                    origin = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin == null || maximized) {
                    return;
                }
                Point location = e.getLocationOnScreen();
                setLocation(location.x - origin.x, location.y - origin.y);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                origin = null;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    toggleMaximize();
                }
            }
        };
        component.addMouseListener(drag);
        component.addMouseMotionListener(drag);
    }

    private void toggleMaximize() {
        if (!maximized) {
            restoredBounds = getBounds();
            GraphicsConfiguration configuration = getGraphicsConfiguration();
            Rectangle screen = configuration.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
            setBounds(screen.x + insets.left,
                    screen.y + insets.top,
                    screen.width - insets.left - insets.right,
                    screen.height - insets.top - insets.bottom);
            maximized = true;
        } else if (restoredBounds != null) {
            setBounds(restoredBounds);
            maximized = false;
        }
    }

    private String shortMessage(Throwable error) {
        String message = error.getMessage();
        return message == null || message.isBlank()
                ? error.getClass().getSimpleName() : message;
    }
}
