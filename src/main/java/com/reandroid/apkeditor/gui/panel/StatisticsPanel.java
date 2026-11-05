package com.reandroid.apkeditor.gui.panel;

import com.reandroid.apkeditor.gui.components.PanelCard;
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
        super("Estadísticas", "binary");
        setSubtitle("Análisis");

        JPanel body = getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        addRow(body, "Archivos Smali", "file-code", Colors.PRIMARY);
        addRow(body, "Clases", "layers", Colors.CYAN);
        addRow(body, "Recursos", "folder", Colors.WARNING);
        addRow(body, "Permisos", "shield", Colors.SUCCESS);
        addRow(body, "Componentes", "blocks", Colors.PURPLE);
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
        setValue("Archivos Smali", analysis.getSmaliFiles());
        setValue("Clases", analysis.getClasses());
        setValue("Recursos", analysis.getResources());
        setValue("Permisos", analysis.getPermissions());
        setValue("Componentes", analysis.getComponents());
    }

    private void setValue(String key, int value) {
        JLabel label = values.get(key);
        if (label != null) {
            label.setText(String.valueOf(value));
        }
    }
}
