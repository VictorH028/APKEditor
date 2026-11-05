package com.reandroid.apkeditor.gui.panel;

import com.reandroid.apkeditor.gui.components.PanelCard;
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
        super("Información de la aplicación", "android");
        setSubtitle("Manifest");

        JPanel body = getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        addRow(body, "Nombre");
        addRow(body, "Versión");
        addRow(body, "Package");
        addRow(body, "Min SDK");
        addRow(body, "Target SDK");
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
        setValue("Nombre", analysis.getAppName());
        setValue("Versión", analysis.getVersionName());
        setValue("Package", analysis.getPackageName());
        setValue("Min SDK", String.valueOf(analysis.getMinSdk()));
        setValue("Target SDK", String.valueOf(analysis.getTargetSdk()));
    }

    private void setValue(String key, String value) {
        JLabel label = values.get(key);
        if (label != null) {
            label.setText(value == null || value.isBlank() ? "—" : value);
            label.setToolTipText(label.getText());
        }
    }
}
