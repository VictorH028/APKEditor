package com.reandroid.apkeditor.gui.panel;

import com.reandroid.apkeditor.gui.components.PanelCard;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Información del APK cargado. Los valores se actualizan desde el modelo,
 * no están hardcodeados en el componente.
 */
public final class ApplicationInfoPanel extends PanelCard {
    private final Map<String, JLabel> values = new LinkedHashMap<>();

    public ApplicationInfoPanel() {
        super(I18n.t("applicationInfo.title"), "android");
        setSubtitle(I18n.t("applicationInfo.subtitle"));

        JPanel body = getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        addRow(body, I18n.t("applicationInfo.name"));
        addRow(body, I18n.t("applicationInfo.version"));
        addRow(body, I18n.t("applicationInfo.package"));
        addRow(body, I18n.t("applicationInfo.minSdk"));
        addRow(body, I18n.t("applicationInfo.targetSdk"));
        setAnalysis(ApkAnalysis.demo());
    }

    private void addRow(JPanel body, String labelText) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        JLabel label = new JLabel(labelText);
        label.setFont(Fonts.SMALL);
        label.setForeground(Colors.TEXT_MUTED);

        JLabel value = new JLabel("—");
        value.setFont(Fonts.BODY_MEDIUM);
        value.setForeground(Colors.TEXT);
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        values.put(labelText, value);
        body.add(row);
    }

    public void setAnalysis(ApkAnalysis analysis) {
        if (analysis == null) {
            return;
        }
        setValue(I18n.t("applicationInfo.name"), analysis.getAppName());
        setValue(I18n.t("applicationInfo.version"), analysis.getVersionName());
        setValue(I18n.t("applicationInfo.package"), analysis.getPackageName());
        setValue(I18n.t("applicationInfo.minSdk"), String.valueOf(analysis.getMinSdk()));
        setValue(I18n.t("applicationInfo.targetSdk"), String.valueOf(analysis.getTargetSdk()));
    }

    private void setValue(String key, String value) {
        JLabel label = values.get(key);
        if (label != null) {
            label.setText(value == null || value.isBlank() ? "—" : value);
            label.setToolTipText(label.getText());
        }
    }
}
