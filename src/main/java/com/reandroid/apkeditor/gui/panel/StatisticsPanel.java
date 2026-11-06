package com.reandroid.apkeditor.gui.panel;

import com.reandroid.apkeditor.gui.components.PanelCard;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estadísticas del APK cargado. Componente reutilizable en dashboard e info.
 */
public final class StatisticsPanel extends PanelCard {
    private final Map<String, JLabel> values = new LinkedHashMap<>();

    public StatisticsPanel() {
        super(I18n.t("statistics.title"), "binary");
        setSubtitle(I18n.t("statistics.subtitle"));

        JPanel body = getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        addRow(body, I18n.t("statistics.smaliFiles"), "file-code", Colors.PRIMARY);
        addRow(body, I18n.t("statistics.classes"), "layers", Colors.CYAN);
        addRow(body, I18n.t("statistics.resources"), "folder", Colors.WARNING);
        addRow(body, I18n.t("statistics.permissions"), "shield", Colors.SUCCESS);
        addRow(body, I18n.t("statistics.components"), "blocks", Colors.PURPLE);
        setAnalysis(ApkAnalysis.demo());
    }

    private void addRow(JPanel body, String labelText, String icon, Color color) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));

        JLabel iconLabel = new JLabel(IconFactory.icon(icon, 13, color));
        JLabel label = new JLabel(labelText);
        label.setFont(Fonts.SMALL);
        label.setForeground(Colors.TEXT_SECONDARY);

        JLabel value = new JLabel("0");
        value.setFont(Fonts.BODY_MEDIUM);
        value.setForeground(Colors.TEXT);
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        left.setOpaque(false);
        left.add(iconLabel);
        left.add(label);

        row.add(left, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        values.put(labelText, value);
        body.add(row);
    }

    public void setAnalysis(ApkAnalysis analysis) {
        if (analysis == null) {
            return;
        }
        setValue(I18n.t("statistics.smaliFiles"), analysis.getSmaliFiles());
        setValue(I18n.t("statistics.classes"), analysis.getClasses());
        setValue(I18n.t("statistics.resources"), analysis.getResources());
        setValue(I18n.t("statistics.permissions"), analysis.getPermissions());
        setValue(I18n.t("statistics.components"), analysis.getComponents());
    }

    private void setValue(String key, int value) {
        JLabel label = values.get(key);
        if (label != null) {
            label.setText(String.valueOf(value));
        }
    }
}
