package com.reandroid.apkeditor.gui.view;

import com.reandroid.apkeditor.core.LegacyOptionsOperation;
import com.reandroid.apkeditor.core.Operation;
import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.components.PanelCard;
import com.reandroid.apkeditor.gui.log.LogPanel;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.panel.AnalysisProgressPanel;
import com.reandroid.apkeditor.gui.service.LogService;
import com.reandroid.apkeditor.gui.service.OperationService;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.apkeditor.gui.theme.IconFactory;
import com.reandroid.apkeditor.gui.theme.Theme;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Locale;

/**
 * Vista genérica de operaciones del motor. El formulario y el estado son
 * independientes de la implementación concreta de cada operación.
 */
public final class OperationView extends JPanel {
    private final String operationId;
    private final Operation operation;
    private final AppState state;
    private final OperationService operations;
    private final LogService logs;

    private final JTextField inputField = new JTextField();
    private final JTextField outputField = new JTextField();
    private final NeonButton runButton = new NeonButton("Ejecutar");
    private final AnalysisProgressPanel progressPanel =
            new AnalysisProgressPanel(new String[]{"Preparando", "Ejecutando", "Finalizando"});
    private final LogPanel logPanel;

    private boolean inputDirectory;
    private boolean outputDirectory;
    private boolean running;
    private Timer activityTimer;
    private final int[] activity = {5};

    public OperationView(String operationId, Operation operation, AppState state,
                         OperationService operations, LogService logs) {
        super(new BorderLayout(0, 16));
        this.operationId = operationId;
        this.operation = operation;
        this.state = state;
        this.operations = operations;
        this.logs = logs;
        this.logPanel = new LogPanel(logs);

        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        prefill();
    }

    private JComponent createHeader() {
        JLabel title = new JLabel(titleFor(operationId));
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT);

        JLabel subtitle = new JLabel(descriptionFor(operationId));
        subtitle.setFont(Fonts.SMALL);
        subtitle.setForeground(Colors.TEXT_MUTED);

        JPanel heading = new JPanel(new BorderLayout(0, 3));
        heading.setOpaque(false);
        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.SOUTH);
        return heading;
    }

    private JComponent createBody() {
        JPanel container = new JPanel(new BorderLayout(0, 14));
        container.setOpaque(false);

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;

        c.gridx = 0;
        c.weightx = 0.40;
        c.insets = new Insets(0, 0, 0, 14);
        columns.add(createForm(), c);

        c.gridx = 1;
        c.weightx = 0.60;
        c.insets = new Insets(0, 0, 0, 0);
        logPanel.setMinimumSize(new Dimension(320, 220));
        columns.add(logPanel, c);

        progressPanel.setPreferredSize(new Dimension(0, 158));
        progressPanel.setMinimumSize(new Dimension(0, 158));

        container.add(columns, BorderLayout.CENTER);
        container.add(progressPanel, BorderLayout.SOUTH);
        return container;
    }

    private JComponent createForm() {
        PanelCard form = new PanelCard("Configuración de la operación", "settings");
        form.setSubtitle("APKEditor core · " + operationId);

        JPanel body = form.getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        inputDirectory = "build".equals(operationId);
        outputDirectory = false;

        body.add(fieldBlock(inputDirectory ? "Directorio de entrada" : "APK de entrada",
                inputField, inputDirectory));
        body.add(Box.createVerticalStrut(12));
        body.add(fieldBlock("Salida", outputField, outputDirectory));
        body.add(Box.createVerticalStrut(16));

        JLabel hint = new JLabel("<html>La salida se genera automáticamente si dejas "
                + "el campo vacío.<br>Se utilizan las opciones existentes del motor "
                + "sin duplicar lógica.</html>");
        hint.setFont(Fonts.SMALL);
        hint.setForeground(Colors.TEXT_MUTED);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        body.add(hint);
        body.add(Box.createVerticalStrut(16));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);

        runButton.setVariant(NeonButton.Variant.PRIMARY);
        runButton.setIcon(IconFactory.icon("play", 13, Colors.BACKGROUND));
        runButton.addActionListener(e -> run());
        NeonButton reset = new NeonButton("Restablecer");
        reset.setVariant(NeonButton.Variant.GHOST);
        reset.addActionListener(e -> prefill());

        actions.add(runButton);
        actions.add(reset);
        body.add(actions);
        return form;
    }

    private JComponent fieldBlock(String labelText, JTextField field, boolean directory) {
        JPanel block = new JPanel(new BorderLayout(0, 5));
        block.setOpaque(false);
        block.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(Fonts.SMALL);
        label.setForeground(Colors.TEXT_MUTED);

        field.setFont(Fonts.BODY);
        field.setBackground(Colors.BACKGROUND_SECONDARY);
        field.setForeground(Colors.TEXT);
        field.setCaretColor(Colors.PRIMARY);
        field.setSelectionColor(Colors.PANEL_HOVER);
        field.setSelectedTextColor(Colors.PRIMARY_BRIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        field.setToolTipText(directory ? "Ruta del directorio" : "Ruta del archivo");

        NeonButton browse = new NeonButton("Examinar");
        browse.setVariant(NeonButton.Variant.GHOST);
        browse.setIcon(IconFactory.icon("folder-open", 14, Colors.TEXT_SECONDARY));
        browse.addActionListener(e -> browse(field, directory));

        JPanel line = new JPanel(new BorderLayout(7, 0));
        line.setOpaque(false);
        line.add(field, BorderLayout.CENTER);
        line.add(browse, BorderLayout.EAST);

        block.add(label, BorderLayout.NORTH);
        block.add(line, BorderLayout.CENTER);
        return block;
    }

    private void browse(JTextField field, boolean directory) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(directory
                ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        if (!directory) {
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "APK / ZIP / AAB", "apk", "zip", "aab"));
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void prefill() {
        if (inputField.getText().isBlank()) {
            File input = state.getCurrentInput();
            if (input != null && !"build".equals(operationId)) {
                inputField.setText(input.getAbsolutePath());
            }
        }
        File output = state.getCurrentOutput();
        if (output != null && outputField.getText().isBlank()) {
            outputField.setText(output.getAbsolutePath());
        }
    }

    private void run() {
        if (running) {
            logs.warning("La operación ya está en ejecución.");
            return;
        }
        File input = readFile(inputField);
        if (input == null) {
            logs.warning("Selecciona la entrada antes de ejecutar.");
            inputField.requestFocusInWindow();
            return;
        }
        File output = readFile(outputField);

        if (operation instanceof LegacyOptionsOperation legacy) {
            legacy.getOptions().inputFile = input;
            legacy.getOptions().outputFile = output;
        }

        running = true;
        runButton.setEnabled(false);
        progressPanel.resetForRun();
        progressPanel.setDescription("Ejecutando " + operation.getName() + "...");
        logs.info("Operación solicitada: " + operation.getName());

        activity[0] = 5;
        if (activityTimer != null) {
            activityTimer.stop();
        }
        activityTimer = new Timer(420, e -> {
            if (!running) {
                activityTimer.stop();
                return;
            }
            activity[0] = Math.min(90, activity[0] + 1);
            updateProgress(activity[0]);
        });
        activityTimer.start();

        boolean started = operations.execute(operationId, input, output,
                busy -> {
                    if (busy) {
                        state.setBusy(true);
                    }
                },
                this::updateProgress,
                (success, error) -> {
                    running = false;
                    if (activityTimer != null) {
                        activityTimer.stop();
                    }
                    runButton.setEnabled(true);
                    state.setBusy(false);
                    progressPanel.finish(success);
                    if (success && operation instanceof LegacyOptionsOperation legacy
                            && legacy.getOptions().outputFile != null) {
                        state.setCurrentOutput(legacy.getOptions().outputFile);
                    }
                    progressPanel.setDescription(success
                            ? "Operación finalizada correctamente."
                            : "La operación terminó con errores. Revisa el log.");
                });
        if (!started) {
            running = false;
            if (activityTimer != null) {
                activityTimer.stop();
            }
            runButton.setEnabled(true);
            state.setBusy(false);
            progressPanel.finish(false);
        }
    }

    private void updateProgress(int percent) {
        progressPanel.setProgress(percent);
        if (percent < 20) {
            step(0);
        } else if (percent < 88) {
            step(1);
        } else {
            step(2);
        }
    }

    private void step(int active) {
        for (int i = 0; i < 3; i++) {
            if (i < active) {
                progressPanel.setStepStatus(i, AnalysisProgressPanel.StepStatus.COMPLETED);
            } else if (i == active) {
                progressPanel.setStepStatus(i, AnalysisProgressPanel.StepStatus.ACTIVE);
            } else {
                progressPanel.setStepStatus(i, AnalysisProgressPanel.StepStatus.PENDING);
            }
        }
    }

    private File readFile(JTextField field) {
        String text = field.getText().trim();
        return text.isEmpty() ? null : new File(text);
    }

    private String titleFor(String id) {
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "build" -> "Construir APK";
            case "merge" -> "Fusionar APKs";
            case "refactor" -> "Refactorizar recursos";
            case "protect" -> "Proteger APK";
            case "info" -> "Información del APK";
            default -> operation.getName();
        };
    }

    private String descriptionFor(String id) {
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "build" -> "Compila un proyecto descompilado de vuelta a un APK firmado.";
            case "merge" -> "Combina módulos y recursos en un único paquete.";
            case "refactor" -> "Renombra y normaliza recursos, clases y rutas.";
            case "protect" -> "Ofusca nombres y protege el APK resultante.";
            case "info" -> "Extrae información técnica del manifiesto y recursos.";
            default -> "Operación del motor APKEditor.";
        };
    }

    @Override
    public void removeNotify() {
        if (activityTimer != null) {
            activityTimer.stop();
        }
        super.removeNotify();
    }
}
