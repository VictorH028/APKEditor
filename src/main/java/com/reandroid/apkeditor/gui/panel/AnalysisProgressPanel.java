package com.reandroid.apkeditor.gui.panel;

import com.reandroid.apkeditor.gui.components.NeonProgressBar;
import com.reandroid.apkeditor.gui.components.PanelCard;
import com.reandroid.apkeditor.gui.components.StatusDot;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel inferior de progreso con descripción y etapas reales.
 */
public final class AnalysisProgressPanel extends PanelCard {
    public enum StepStatus { COMPLETED, ACTIVE, PENDING }

    private final JLabel description = new JLabel();
    private final NeonProgressBar progress = new NeonProgressBar();
    private final JLabel stateLabel = new JLabel(I18n.t("progress.state.running"));
    private final StatusDot stateDot = new StatusDot(Colors.WARNING);
    private final List<StepChip> steps = new ArrayList<>();
    private final JPanel stepsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));

    public AnalysisProgressPanel(String[] stepNames) {
        super(I18n.t("progress.title"), "workflow");
        setSubtitle(I18n.t("progress.subtitle"));

        description.setText(I18n.t("progress.description"));
        description.setFont(Fonts.SMALL);
        description.setForeground(Colors.TEXT_SECONDARY);

        progress.setValue(68);
        progress.setPreferredSize(new Dimension(240, 18));

        stateLabel.setFont(Fonts.TINY);
        stateLabel.setForeground(Colors.WARNING);
        JPanel state = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        state.setOpaque(false);
        state.add(stateDot);
        state.add(stateLabel);
        addHeaderAction(state);

        stepsPanel.setOpaque(false);
        if (stepNames != null) {
            for (int i = 0; i < stepNames.length; i++) {
                StepChip chip = new StepChip(stepNames[i]);
                chip.setStatus(i < stepNames.length - 1
                        ? StepStatus.COMPLETED : StepStatus.PENDING);
                steps.add(chip);
                stepsPanel.add(chip);
            }
        }

        JPanel body = getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        description.setAlignmentX(LEFT_ALIGNMENT);
        progress.setAlignmentX(LEFT_ALIGNMENT);
        stepsPanel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(description);
        body.add(Box.createVerticalStrut(8));
        body.add(progress);
        body.add(Box.createVerticalStrut(10));
        body.add(stepsPanel);
    }

    public void setProgress(int value) {
        progress.setValue(value);
    }

    public int getProgress() {
        return progress.getValue();
    }

    public void setDescription(String text) {
        description.setText(text);
    }

    public void setState(String text, Color color) {
        stateLabel.setText(text);
        stateLabel.setForeground(color);
        stateDot.setColor(color);
    }

    public void setStepStatus(int index, StepStatus status) {
        if (index >= 0 && index < steps.size()) {
            steps.get(index).setStatus(status);
        }
    }

    public void resetForRun() {
        setProgress(0);
        setState(I18n.t("progress.state.running"), Colors.WARNING);
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setStatus(i == 0 ? StepStatus.ACTIVE : StepStatus.PENDING);
        }
    }

    public void finish(boolean success) {
        setProgress(success ? 100 : 0);
        setState(I18n.t(success ? "progress.state.completed" : "progress.state.failed"),
                success ? Colors.SUCCESS : Colors.ERROR);
        for (StepChip step : steps) {
            step.setStatus(success ? StepStatus.COMPLETED : StepStatus.PENDING);
        }
    }

    private static final class StepChip extends JPanel {
        private final JLabel icon = new JLabel();
        private final JLabel label = new JLabel();
        private StepStatus status = StepStatus.PENDING;

        private StepChip(String text) {
            super(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setOpaque(false);
            label.setText(text);
            label.setFont(Fonts.TINY);
            add(icon);
            add(label);
            setStatus(status);
        }

        private void setStatus(StepStatus status) {
            this.status = status;
            Color color = switch (status) {
                case COMPLETED -> Colors.SUCCESS;
                case ACTIVE -> Colors.PRIMARY_BRIGHT;
                default -> Colors.TEXT_MUTED;
            };
            icon.setIcon(IconFactory.icon(status == StepStatus.COMPLETED ? "check" : "clock",
                    12, color));
            label.setForeground(color);
            revalidate();
            repaint();
        }
    }
}
