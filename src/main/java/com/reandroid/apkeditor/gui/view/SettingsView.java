package com.reandroid.apkeditor.gui.view;

import com.reandroid.apkeditor.core.OperationRegistry;
import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.components.PanelCard;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuración de apariencia y rutas de trabajo. Los cambios de tema se
 * publican en AppState y la ventana reconstruye la interfaz.
 */
public final class SettingsView extends JPanel {
    private final AppState state;
    private final Map<String, JLabel> pathValues = new LinkedHashMap<>();
    private final PropertyChangeListener inputListener;
    private final PropertyChangeListener outputListener;

    public SettingsView(AppState state, OperationRegistry registry) {
        super(new BorderLayout(0, 16));
        this.state = state;
        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel(I18n.t("settings.title"));
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT);
        JLabel subtitle = new JLabel(I18n.t("settings.subtitle"));
        subtitle.setFont(Fonts.SMALL);
        subtitle.setForeground(Colors.TEXT_MUTED);

        JPanel titles = new JPanel(new BorderLayout(0, 3));
        titles.setOpaque(false);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);
        add(titles, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.52;

        c.gridx = 0;
        c.weightx = 0.34;
        c.insets = new Insets(0, 0, 0, 14);
        grid.add(createAppearance(), c);

        c.gridx = 1;
        c.weightx = 0.32;
        grid.add(createPaths(), c);

        c.gridx = 2;
        c.weightx = 0.34;
        c.insets = new Insets(0, 0, 0, 0);
        grid.add(createEngine(registry), c);

        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 0.48;
        c.insets = new Insets(14, 0, 0, 0);
        grid.add(createShortcuts(), c);

        add(grid, BorderLayout.CENTER);

        inputListener = event -> SwingUtilities.invokeLater(this::applyInput);
        outputListener = event -> SwingUtilities.invokeLater(this::applyOutput);
        state.addPropertyChangeListener(AppState.INPUT_CHANGED, inputListener);
        state.addPropertyChangeListener(AppState.OUTPUT_CHANGED, outputListener);
        applyInput();
        applyOutput();
    }

    private JComponent createAppearance() {
        PanelCard card = new PanelCard(I18n.t("settings.appearance"), "settings");
        card.setSubtitle(I18n.t("settings.themeSubtitle", Theme.current()));

        JPanel themes = new JPanel(new GridLayout(2, 2, 8, 8));
        themes.setOpaque(false);
        for (String name : Theme.NAMES) {
            NeonButton button = new NeonButton(name);
            button.setVariant(name.equals(state.getThemeName())
                    ? NeonButton.Variant.PRIMARY : NeonButton.Variant.SECONDARY);
            button.addActionListener(e -> state.setThemeName(name));
            themes.add(button);
        }

        JLabel languageLabel = new JLabel(I18n.t("settings.language"));
        languageLabel.setFont(Fonts.SMALL);
        languageLabel.setForeground(Colors.TEXT_SECONDARY);

        JPanel languages = new JPanel(new GridLayout(1, 2, 8, 8));
        languages.setOpaque(false);
        for (I18n.Language language : I18n.Language.values()) {
            NeonButton button = new NeonButton(language.getLabel());
            button.setFont(Fonts.SMALL);
            button.setVariant(language.getCode().equals(state.getLanguageCode())
                    ? NeonButton.Variant.PRIMARY : NeonButton.Variant.SECONDARY);
            button.addActionListener(e -> state.setLanguageCode(language.getCode()));
            languages.add(button);
        }

        JLabel themeHint = new JLabel("<html>" + I18n.t("settings.themeHint") + "</html>");
        themeHint.setFont(Fonts.SMALL);
        themeHint.setForeground(Colors.TEXT_MUTED);

        JLabel languageHint = new JLabel(I18n.t("settings.languageHint"));
        languageHint.setFont(Fonts.SMALL);
        languageHint.setForeground(Colors.TEXT_MUTED);

        JPanel languageBlock = new JPanel(new BorderLayout(0, 6));
        languageBlock.setOpaque(false);
        languageBlock.add(languageLabel, BorderLayout.NORTH);
        languageBlock.add(languages, BorderLayout.CENTER);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        themeHint.setAlignmentX(LEFT_ALIGNMENT);
        languageBlock.setAlignmentX(LEFT_ALIGNMENT);
        languageHint.setAlignmentX(LEFT_ALIGNMENT);
        texts.add(themeHint);
        texts.add(Box.createVerticalStrut(12));
        texts.add(languageBlock);
        texts.add(Box.createVerticalStrut(4));
        texts.add(languageHint);

        JPanel body = card.getBody();
        body.setLayout(new BorderLayout(0, 12));
        body.add(themes, BorderLayout.NORTH);
        body.add(texts, BorderLayout.CENTER);
        return card;
    }

    private JComponent createPaths() {
        PanelCard card = new PanelCard(I18n.t("settings.paths"), "folder-open");
        card.setSubtitle(I18n.t("settings.currentSession"));

        JPanel body = card.getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        addPathRow(body, I18n.t("settings.input"));
        addPathRow(body, I18n.t("settings.output"));
        addPathRow(body, I18n.t("settings.workspace"));
        body.add(Box.createVerticalGlue());
        return card;
    }

    private void addPathRow(JPanel body, String key) {
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
        pathValues.put(key, value);
        body.add(row);
    }

    private JComponent createEngine(OperationRegistry registry) {
        PanelCard card = new PanelCard(I18n.t("settings.engine"), "layers");
        card.setSubtitle("APKEditor core");

        JPanel body = card.getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        addStaticRow(body, I18n.t("settings.version"), "1.4.9");
        addStaticRow(body, I18n.t("settings.operations"),
                String.valueOf(registry.all().size()));
        addStaticRow(body, I18n.t("settings.java"),
                System.getProperty("java.version", "—"));
        addStaticRow(body, I18n.t("settings.interface"), "Swing · Java2D");
        addStaticRow(body, I18n.t("settings.editor"), "Smali highlighter");
        body.add(Box.createVerticalGlue());
        return card;
    }

    private void addStaticRow(JPanel body, String key, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel(key);
        label.setFont(Fonts.SMALL);
        label.setForeground(Colors.TEXT_MUTED);

        JLabel labelValue = new JLabel(value);
        labelValue.setFont(Fonts.SMALL);
        labelValue.setForeground(Colors.TEXT);
        labelValue.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(label, BorderLayout.WEST);
        row.add(labelValue, BorderLayout.CENTER);
        body.add(row);
    }

    private JComponent createShortcuts() {
        PanelCard card = new PanelCard(I18n.t("settings.shortcuts"), "terminal");
        card.setSubtitle(I18n.t("settings.productivity"));

        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 6));
        grid.setOpaque(false);
        addShortcut(grid, "F5", I18n.t("settings.shortcut.decompile"));
        addShortcut(grid, "Ctrl + O", I18n.t("settings.shortcut.selectApk"));
        addShortcut(grid, "Ctrl + F", I18n.t("settings.shortcut.search"));
        addShortcut(grid, "Ctrl + G", I18n.t("settings.shortcut.gotoLine"));
        addShortcut(grid, "Ctrl + S", I18n.t("settings.shortcut.save"));
        addShortcut(grid, "Ctrl + W", I18n.t("settings.shortcut.closeTab"));
        addShortcut(grid, "Ctrl + 1", I18n.t("settings.shortcut.workspace"));
        addShortcut(grid, "Esc", I18n.t("settings.shortcut.escape"));

        card.getBody().setLayout(new BorderLayout());
        card.getBody().add(grid, BorderLayout.CENTER);
        return card;
    }

    private void addShortcut(JPanel parent, String key, String description) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(Fonts.MONO_SMALL);
        keyLabel.setForeground(Colors.PRIMARY_BRIGHT);
        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(Fonts.SMALL);
        descriptionLabel.setForeground(Colors.TEXT_MUTED);
        row.add(keyLabel, BorderLayout.WEST);
        row.add(descriptionLabel, BorderLayout.CENTER);
        parent.add(row);
    }

    private void applyInput() {
        File input = state.getCurrentInput();
        setPath(I18n.t("settings.input"), input == null
                ? I18n.t("settings.demoApk") : input.getAbsolutePath());
        setPath(I18n.t("settings.workspace"), input == null
                ? I18n.t("settings.demoWorkspace") : input.getParent());
    }

    private void applyOutput() {
        File output = state.getCurrentOutput();
        setPath(I18n.t("settings.output"), output == null
                ? I18n.t("settings.auto") : output.getAbsolutePath());
    }

    private void setPath(String key, String value) {
        JLabel label = pathValues.get(key);
        if (label != null) {
            label.setText(shorten(value));
            label.setToolTipText(value);
        }
    }

    private String shorten(String value) {
        if (value == null) {
            return "—";
        }
        if (value.length() <= 30) {
            return value;
        }
        return "…" + value.substring(value.length() - 29);
    }

    @Override
    public void removeNotify() {
        state.removePropertyChangeListener(inputListener);
        state.removePropertyChangeListener(outputListener);
        super.removeNotify();
    }
}
