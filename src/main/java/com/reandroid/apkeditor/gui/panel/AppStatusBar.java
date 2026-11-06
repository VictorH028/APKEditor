package com.reandroid.apkeditor.gui.panel;

import com.reandroid.apkeditor.gui.components.NeonProgressBar;
import com.reandroid.apkeditor.gui.components.StatusDot;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;

import javax.swing.*;
import java.awt.*;

/**
 * Barra de estado global con memoria y mensajes del sistema.
 */
public final class AppStatusBar extends JPanel {
    private final AppState state;
    private final JLabel system = new JLabel(I18n.t("status.ready"));
    private final StatusDot dot = new StatusDot(Colors.SUCCESS);
    private final JLabel memory = new JLabel(I18n.t("status.memory", "—"));
    private final NeonProgressBar memoryBar = new NeonProgressBar();
    private final Timer timer;

    public AppStatusBar(AppState state) {
        super(new BorderLayout(14, 0));
        this.state = state;
        setPreferredSize(new Dimension(0, 48));
        setMinimumSize(new Dimension(0, 48));
        setOpaque(true);
        setBackground(Colors.BACKGROUND_SECONDARY);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Colors.BORDER),
                BorderFactory.createEmptyBorder(0, 18, 0, 18)));

        system.setFont(Fonts.SMALL);
        system.setForeground(Colors.SUCCESS);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        left.setOpaque(false);
        left.add(dot);
        left.add(system);
        left.add(separator());
        memory.setFont(Fonts.SMALL);
        memory.setForeground(Colors.TEXT_MUTED);
        left.add(memory);

        memoryBar.setShowPercentage(false);
        memoryBar.setAnimated(false);
        memoryBar.setPreferredSize(new Dimension(130, 12));
        memoryBar.setMinimumSize(new Dimension(90, 12));
        memoryBar.setAccent(Colors.SUCCESS);
        left.add(memoryBar);

        JLabel footer = new JLabel(I18n.t("status.footer"));
        footer.setFont(Fonts.TINY.deriveFont(Font.BOLD));
        footer.setForeground(Colors.TEXT_MUTED);

        add(left, BorderLayout.WEST);
        add(footer, BorderLayout.EAST);

        state.addPropertyChangeListener(AppState.BUSY_CHANGED, e ->
                SwingUtilities.invokeLater(() -> setBusy((Boolean) e.getNewValue())));

        timer = new Timer(2500, e -> updateMemory());
        timer.start();
        updateMemory();
    }

    private JLabel separator() {
        JLabel label = new JLabel("│");
        label.setForeground(Colors.BORDER);
        label.setFont(Fonts.SMALL);
        return label;
    }

    public void setBusy(boolean busy) {
        if (busy) {
            system.setText(I18n.t("status.processing"));
            system.setForeground(Colors.WARNING);
            dot.setColor(Colors.WARNING);
        } else {
            system.setText(I18n.t("status.ready"));
            system.setForeground(Colors.SUCCESS);
            dot.setColor(Colors.SUCCESS);
        }
    }

    public void setSystemStatus(String text, Color color) {
        system.setText(text);
        system.setForeground(color);
        dot.setColor(color);
    }

    private void updateMemory() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        memory.setText(I18n.t("status.memory", format(used) + " / " + format(max)));
        memoryBar.setValue(max <= 0 ? 0 : (int) Math.min(100, used * 100 / max));
    }

    private String format(long bytes) {
        double mb = bytes / (1024.0 * 1024.0);
        if (mb >= 1024) {
            return String.format("%.1f GB", mb / 1024.0);
        }
        return String.format("%.0f MB", mb);
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }
}
