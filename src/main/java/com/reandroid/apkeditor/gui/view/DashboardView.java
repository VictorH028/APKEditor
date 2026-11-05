package com.reandroid.apkeditor.gui.view;

import com.reandroid.apkeditor.gui.components.ActionTile;
import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.components.PanelCard;
import com.reandroid.apkeditor.gui.components.StatusDot;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.navigation.Route;
import com.reandroid.apkeditor.gui.panel.ApplicationInfoPanel;
import com.reandroid.apkeditor.gui.panel.StatisticsPanel;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Centro de control: estado del proyecto, estadísticas y accesos directos.
 */
public final class DashboardView extends JPanel {
    private final AppState state;
    private final Consumer<Route> navigate;
    private final ApplicationInfoPanel information = new ApplicationInfoPanel();
    private final StatisticsPanel statistics = new StatisticsPanel();
    private final Map<String, JLabel> projectValues = new LinkedHashMap<>();
    private final StatusDot inputDot = new StatusDot(Colors.SUCCESS);
    private final JLabel inputStatus = new JLabel("Listo");
    private final PropertyChangeListener analysisListener;
    private final PropertyChangeListener inputListener;

    public DashboardView(AppState state, Consumer<Route> navigate) {
        super(new BorderLayout(0, 16));
        this.state = state;
        this.navigate = navigate;
        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        analysisListener = event -> SwingUtilities.invokeLater(
                () -> applyAnalysis((ApkAnalysis) event.getNewValue()));
        inputListener = event -> SwingUtilities.invokeLater(this::applyInput);
        state.addPropertyChangeListener(AppState.ANALYSIS_CHANGED, analysisListener);
        state.addPropertyChangeListener(AppState.INPUT_CHANGED, inputListener);

        applyAnalysis(state.getAnalysis());
        applyInput();
    }

    private JComponent createHeader() {
        JLabel title = new JLabel("Dashboard");
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT);

        JLabel subtitle = new JLabel("Centro de control del proyecto · APKEditor core");
        subtitle.setFont(Fonts.SMALL);
        subtitle.setForeground(Colors.TEXT_MUTED);

        JPanel titles = new JPanel(new BorderLayout(0, 3));
        titles.setOpaque(false);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);

        NeonButton open = new NeonButton("Abrir workspace",
                IconFactory.icon("code", 14, Colors.BACKGROUND));
        open.setVariant(NeonButton.Variant.PRIMARY);
        open.addActionListener(e -> navigate.accept(Route.DECOMPILE));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(titles, BorderLayout.WEST);
        header.add(open, BorderLayout.EAST);
        return header;
    }

    private JComponent createContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;

        c.gridx = 0;
        c.weightx = 0.34;
        c.insets = new Insets(0, 0, 0, 14);
        top.add(information, c);

        c.gridx = 1;
        c.weightx = 0.32;
        top.add(statistics, c);

        c.gridx = 2;
        c.weightx = 0.34;
        c.insets = new Insets(0, 0, 0, 0);
        top.add(createProjectCard(), c);

        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0;
        outer.gridy = 0;
        outer.weightx = 1.0;
        outer.weighty = 0.45;
        outer.fill = GridBagConstraints.BOTH;
        content.add(top, outer);

        outer.gridy = 1;
        outer.weighty = 0.55;
        outer.insets = new Insets(14, 0, 0, 0);
        content.add(createActions(), outer);
        return content;
    }

    private JComponent createProjectCard() {
        PanelCard card = new PanelCard("Estado del proyecto", "package");
        card.setSubtitle("Sesión actual");

        JPanel body = card.getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        status.setOpaque(false);
        inputStatus.setFont(Fonts.SMALL);
        inputStatus.setForeground(Colors.SUCCESS);
        status.add(inputDot);
        status.add(inputStatus);
        status.setAlignmentX(LEFT_ALIGNMENT);
        body.add(status);
        body.add(Box.createVerticalStrut(8));

        addProjectRow(body, "APK actual");
        addProjectRow(body, "Ruta");
        addProjectRow(body, "Tamaño");
        addProjectRow(body, "Salida");
        body.add(Box.createVerticalGlue());

        JLabel version = new JLabel("APKEDITOR v1.0.0");
        version.setFont(Fonts.TINY);
        version.setForeground(Colors.TEXT_MUTED);
        version.setAlignmentX(LEFT_ALIGNMENT);
        body.add(version);
        return card;
    }

    private void addProjectRow(JPanel body, String key) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel(key);
        label.setFont(Fonts.SMALL);
        label.setForeground(Colors.TEXT_MUTED);

        JLabel value = new JLabel("—");
        value.setFont(Fonts.SMALL);
        value.setForeground(Colors.TEXT);
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        projectValues.put(key, value);
        body.add(row);
    }

    private JComponent createActions() {
        PanelCard card = new PanelCard("Acciones rápidas", "workflow");
        card.setSubtitle("Flujo de trabajo del motor");

        JPanel grid = new JPanel(new GridLayout(2, 4, 10, 10));
        grid.setOpaque(false);

        addAction(grid, "file-code", "Descompilar",
                "APK a proyecto smali", Route.DECOMPILE);
        addAction(grid, "scan-search", "Analizar",
                "Manifest, permisos y componentes", Route.ANALYZE);
        addAction(grid, "binary", "Smali Viewer",
                "Explora y edita código", Route.SMALI_VIEWER);
        addAction(grid, "package", "Build",
                "Proyecto a APK", Route.BUILD);
        addAction(grid, "git-merge", "Merge",
                "Combina módulos APK", Route.MERGE);
        addAction(grid, "workflow", "Refactor",
                "Renombra recursos", Route.REFACTOR);
        addAction(grid, "shield", "Proteger",
                "Ofusca el paquete final", Route.PROTECT);
        addAction(grid, "info", "Información",
                "Detalle técnico del APK", Route.INFORMATION);

        card.getBody().setLayout(new BorderLayout());
        card.getBody().add(grid, BorderLayout.CENTER);
        return card;
    }

    private void addAction(JPanel parent, String icon, String title,
                           String description, Route route) {
        ActionTile tile = new ActionTile(icon, title, description);
        tile.addActionListener(e -> navigate.accept(route));
        parent.add(tile);
    }

    private void applyInput() {
        File input = state.getCurrentInput();
        projectValues.get("APK actual").setText(input == null ? "app-debug.apk" : input.getName());
        projectValues.get("Ruta").setText(input == null ? "demo workspace" : input.getParent());
        projectValues.get("Ruta").setToolTipText(input == null ? null : input.getAbsolutePath());
        projectValues.get("Tamaño").setText(input == null
                ? "4.6 MB" : state.getAnalysis().getSizeLabel());
        File output = state.getCurrentOutput();
        projectValues.get("Salida").setText(output == null ? "—" : output.getName());
        projectValues.get("Salida").setToolTipText(output == null ? null : output.getAbsolutePath());
        inputStatus.setText(input == null ? "Demo" : "Cargado");
        inputDot.setColor(input == null ? Colors.WARNING : Colors.SUCCESS);
        inputStatus.setForeground(input == null ? Colors.WARNING : Colors.SUCCESS);
    }

    private void applyAnalysis(ApkAnalysis analysis) {
        information.setAnalysis(analysis);
        statistics.setAnalysis(analysis);
    }

    @Override
    public void removeNotify() {
        state.removePropertyChangeListener(analysisListener);
        state.removePropertyChangeListener(inputListener);
        super.removeNotify();
    }
}
