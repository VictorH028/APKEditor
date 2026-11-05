package com.reandroid.apkeditor.gui.log;

import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.components.PanelCard;
import com.reandroid.apkeditor.gui.model.LogEntry;
import com.reandroid.apkeditor.gui.model.LogLevel;
import com.reandroid.apkeditor.gui.service.LogService;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Panel de log con timestamps, niveles coloreados, auto-scroll,
 * limpieza y exportación a archivo.
 */
public final class LogPanel extends PanelCard implements LogService.Listener {
    private final LogService logService;
    private final JTextPane textPane = new JTextPane();
    private final StyledDocument document;
    private final JFileChooser chooser = new JFileChooser();
    private final JScrollPane scroll;

    public LogPanel(LogService logService) {
        super("Panel de Log", "terminal");
        this.logService = logService;
        setSubtitle("Eventos del motor en tiempo real");

        textPane.setEditable(false);
        textPane.setFont(Fonts.MONO_SMALL);
        textPane.setBackground(Colors.SURFACE);
        textPane.setForeground(Colors.TEXT);
        textPane.setBorder(new EmptyBorder(6, 8, 6, 8));
        textPane.setCaretColor(Colors.PRIMARY);
        textPane.setSelectionColor(Colors.PANEL_HOVER);
        this.document = textPane.getStyledDocument();
        ensureStyles();

        scroll = new JScrollPane(textPane);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Colors.SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        NeonButton clear = new NeonButton("Limpiar",
                IconFactory.icon("trash", 13, Colors.TEXT_SECONDARY));
        clear.setVariant(NeonButton.Variant.GHOST);
        clear.setFont(Fonts.SMALL);
        clear.setCompact(true);
        clear.addActionListener(e -> logService.clear());

        NeonButton export = new NeonButton("Exportar",
                IconFactory.icon("download", 13, Colors.TEXT_SECONDARY));
        export.setVariant(NeonButton.Variant.GHOST);
        export.setFont(Fonts.SMALL);
        export.setCompact(true);
        export.addActionListener(e -> exportLogs());

        addHeaderAction(clear);
        addHeaderAction(export);

        getBody().setLayout(new BorderLayout());
        getBody().add(scroll, BorderLayout.CENTER);

        List<LogEntry> entries = logService.getEntries();
        for (LogEntry entry : entries) {
            appendEntry(entry);
        }
        logService.addListener(this);
        scrollToBottom();
    }

    @Override
    public void onEntry(LogEntry entry) {
        appendEntry(entry);
        scrollToBottom();
    }

    @Override
    public void onCleared() {
        textPane.setText("");
    }

    private void appendEntry(LogEntry entry) {
        try {
            int start = document.getLength();
            document.insertString(start, "[" + entry.getTimestamp() + "] ",
                    document.getStyle("timestamp"));
            document.insertString(document.getLength(), "● ", styleFor(entry.getLevel()));
            document.insertString(document.getLength(), entry.getMessage() + "\n",
                    styleFor(entry.getLevel()));
        } catch (BadLocationException ignored) {
        }
    }

    private Style styleFor(LogLevel level) {
        String name = switch (level) {
            case SUCCESS -> "success";
            case WARNING -> "warning";
            case ERROR -> "error";
            case COMMAND -> "command";
            case DEBUG -> "debug";
            default -> "info";
        };
        return document.getStyle(name);
    }

    private void ensureStyles() {
        Style base = document.addStyle("base", null);
        StyleConstants.setForeground(base, Colors.TEXT);
        StyleConstants.setFontFamily(base, Fonts.MONO_SMALL.getFamily());
        StyleConstants.setFontSize(base, Fonts.MONO_SMALL.getSize());

        Style timestamp = document.addStyle("timestamp", base);
        StyleConstants.setForeground(timestamp, Colors.TEXT_MUTED);

        Style info = document.addStyle("info", base);
        StyleConstants.setForeground(info, Colors.TEXT);

        Style success = document.addStyle("success", base);
        StyleConstants.setForeground(success, Colors.SUCCESS);

        Style warning = document.addStyle("warning", base);
        StyleConstants.setForeground(warning, Colors.WARNING);

        Style error = document.addStyle("error", base);
        StyleConstants.setForeground(error, Colors.ERROR);

        Style command = document.addStyle("command", base);
        StyleConstants.setForeground(command, Colors.CYAN);

        Style debug = document.addStyle("debug", base);
        StyleConstants.setForeground(debug, Colors.TEXT_MUTED);
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            textPane.setCaretPosition(document.getLength());
        });
    }

    @Override
    public void removeNotify() {
        logService.removeListener(this);
        super.removeNotify();
    }

    private void exportLogs() {
        chooser.setSelectedFile(new File("apkeditor-log.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            logService.export(file);
            logService.success("Log exportado: " + file.getAbsolutePath());
        } catch (IOException error) {
            logService.error("No se pudo exportar el log: " + error.getMessage());
        }
    }
}
