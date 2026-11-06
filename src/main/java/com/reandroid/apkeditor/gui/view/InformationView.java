package com.reandroid.apkeditor.gui.view;

import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.components.PanelCard;
import com.reandroid.apkeditor.gui.i18n.I18n;
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
 * Información técnica del APK cargado.
 */
public final class InformationView extends JPanel {
    private final AppState state;
    private final ApplicationInfoPanel information = new ApplicationInfoPanel();
    private final StatisticsPanel statistics = new StatisticsPanel();
    private final Map<String, JLabel> technicalValues = new LinkedHashMap<>();
    private final PropertyChangeListener analysisListener;
    private final PropertyChangeListener inputListener;

    public InformationView(AppState state, Consumer<Route> navigate) {
        super(new BorderLayout(0, 16));
        this.state = state;
        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel(I18n.t("info.title"));
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT);
        JLabel subtitle = new JLabel(I18n.t("info.subtitle"));
        subtitle.setFont(Fonts.SMALL);
        subtitle.setForeground(Colors.TEXT_MUTED);

        JPanel titles = new JPanel(new BorderLayout(0, 3));
        titles.setOpaque(false);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);

        NeonButton open = new NeonButton(I18n.t("info.openWorkspace"),
                IconFactory.icon("code", 14, Colors.BACKGROUND));
        open.setVariant(NeonButton.Variant.PRIMARY);
        open.addActionListener(e -> navigate.accept(Route.DECOMPILE));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(titles, BorderLayout.WEST);
        header.add(open, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;

        c.gridx = 0;
        c.weightx = 0.34;
        c.insets = new Insets(0, 0, 0, 14);
        grid.add(information, c);

        c.gridx = 1;
        c.weightx = 0.32;
        grid.add(statistics, c);

        c.gridx = 2;
        c.weightx = 0.34;
        c.insets = new Insets(0, 0, 0, 0);
        grid.add(createTechnical(), c);
        add(grid, BorderLayout.CENTER);

        analysisListener = event -> SwingUtilities.invokeLater(
                () -> applyAnalysis((ApkAnalysis) event.getNewValue()));
        inputListener = event -> SwingUtilities.invokeLater(this::applyInput);
        state.addPropertyChangeListener(AppState.ANALYSIS_CHANGED, analysisListener);
        state.addPropertyChangeListener(AppState.INPUT_CHANGED, inputListener);
        applyAnalysis(state.getAnalysis());
        applyInput();
    }

    private JComponent createTechnical() {
        PanelCard card = new PanelCard(I18n.t("info.detailTitle"), "binary");
        card.setSubtitle(I18n.t("info.detailSubtitle"));

        JPanel body = card.getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        for (String key : new String[]{
                I18n.t("info.field.file"),
                I18n.t("info.field.path"),
                I18n.t("info.field.size"),
                I18n.t("info.field.files"),
                I18n.t("info.field.versionCode"),
                I18n.t("info.field.classes"),
                I18n.t("info.field.permissions")}) {
            addRow(body, key);
        }
        body.add(Box.createVerticalGlue());
        return card;
    }

    private void addRow(JPanel body, String key) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
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
        technicalValues.put(key, value);
        body.add(row);
    }

    private void applyInput() {
        File input = state.getCurrentInput();
        set(I18n.t("info.field.file"), input == null
                ? I18n.t("info.demoFile") : input.getName());
        set(I18n.t("info.field.path"), input == null ? "—" : input.getAbsolutePath());
        applyAnalysis(state.getAnalysis());
    }

    private void applyAnalysis(ApkAnalysis analysis) {
        information.setAnalysis(analysis);
        statistics.setAnalysis(analysis);
        set(I18n.t("info.field.size"), analysis.getSizeLabel());
        set(I18n.t("info.field.files"), String.valueOf(analysis.getFileCount()));
        set(I18n.t("info.field.versionCode"), String.valueOf(analysis.getVersionCode()));
        set(I18n.t("info.field.classes"), String.valueOf(analysis.getClasses()));
        set(I18n.t("info.field.permissions"), String.valueOf(analysis.getPermissions()));
    }

    private void set(String key, String value) {
        JLabel label = technicalValues.get(key);
        if (label != null) {
            label.setText(value);
            label.setToolTipText(value);
        }
    }

    @Override
    public void removeNotify() {
        state.removePropertyChangeListener(analysisListener);
        state.removePropertyChangeListener(inputListener);
        super.removeNotify();
    }
}
