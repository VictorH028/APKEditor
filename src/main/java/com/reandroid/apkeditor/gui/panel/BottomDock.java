package com.reandroid.apkeditor.gui.panel;

import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import com.reandroid.apkeditor.gui.theme.Colors;

import javax.swing.*;
import java.awt.*;

/**
 * Fila inferior de paneles: progreso, información y estadísticas.
 */
public final class BottomDock extends JPanel {
    private final AnalysisProgressPanel progress;
    private final ApplicationInfoPanel information;
    private final StatisticsPanel statistics;

    public BottomDock() {
        super(new GridBagLayout());
        setOpaque(false);

        progress = new AnalysisProgressPanel(new String[]{
                I18n.t("progress.step.loading"),
                I18n.t("progress.step.decompiling"),
                I18n.t("progress.step.analysis"),
                I18n.t("progress.step.report")});
        information = new ApplicationInfoPanel();
        statistics = new StatisticsPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.insets = new Insets(0, 0, 0, 14);

        c.gridx = 0;
        c.weightx = 0.55;
        add(progress, c);
        c.gridx = 1;
        c.weightx = 0.25;
        add(information, c);
        c.gridx = 2;
        c.weightx = 0.20;
        c.insets = new Insets(0, 0, 0, 0);
        add(statistics, c);

        setBackground(Colors.BACKGROUND);
    }

    public AnalysisProgressPanel getProgressPanel() {
        return progress;
    }

    public ApplicationInfoPanel getInformationPanel() {
        return information;
    }

    public StatisticsPanel getStatisticsPanel() {
        return statistics;
    }

    public void setAnalysis(ApkAnalysis analysis) {
        information.setAnalysis(analysis);
        statistics.setAnalysis(analysis);
    }
}
