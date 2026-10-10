package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.Options;
import com.reandroid.apkeditor.ResourceStrings;
import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;
import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.OptionArg;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class OperationOptionsPanel extends JPanel {
    private final List<OptionControl> controls = new ArrayList<>();

    public OperationOptionsPanel(Options options) {
        setLayout(new GridBagLayout());
        setOpaque(false);

        JLabel heading = new JLabel("ENGINE OPTIONS");
        heading.setFont(Fonts.SECTION);
        heading.setForeground(Colors.TEXT);
        GridBagConstraints headingConstraints = new GridBagConstraints();
        headingConstraints.gridx = 0;
        headingConstraints.gridy = 0;
        headingConstraints.gridwidth = 2;
        headingConstraints.weightx = 1;
        headingConstraints.fill = GridBagConstraints.HORIZONTAL;
        headingConstraints.insets = new Insets(8, 8, 12, 8);
        add(heading, headingConstraints);

        int row = 1;
        for (Field field : options.getClass().getFields()) {
            OptionArg option = field.getAnnotation(OptionArg.class);
            ChoiceArg choice = field.getAnnotation(ChoiceArg.class);
            if ((option == null && choice == null)
                    || "inputFile".equals(field.getName())
                    || "outputFile".equals(field.getName())) {
                continue;
            }

            try {
                Object value = field.get(options);
                JComponent editor = createEditor(field, choice, value);
                if (editor == null) {
                    continue;
                }
                String name = choice == null ? option.name() : choice.name();
                String description = choice == null ? option.description() : choice.description();
                String tooltip = ResourceStrings.INSTANCE.getString(description);
                editor.setToolTipText(tooltip);

                if (editor instanceof JCheckBox) {
                    ((JCheckBox) editor).setText(name);
                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.gridx = 0;
                    constraints.gridy = row++;
                    constraints.gridwidth = 2;
                    constraints.weightx = 1;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    constraints.insets = new Insets(4, 8, 4, 8);
                    add(editor, constraints);
                } else {
                    JLabel label = new JLabel(name);
                    label.setFont(Fonts.SMALL);
                    label.setForeground(Colors.TEXT_MUTED);
                    label.setToolTipText(tooltip);

                    GridBagConstraints labelConstraints = new GridBagConstraints();
                    labelConstraints.gridx = 0;
                    labelConstraints.gridy = row;
                    labelConstraints.anchor = GridBagConstraints.NORTHWEST;
                    labelConstraints.insets = new Insets(7, 8, 4, 12);
                    add(label, labelConstraints);

                    GridBagConstraints editorConstraints = new GridBagConstraints();
                    editorConstraints.gridx = 1;
                    editorConstraints.gridy = row++;
                    editorConstraints.weightx = 1;
                    editorConstraints.fill = GridBagConstraints.HORIZONTAL;
                    editorConstraints.insets = new Insets(4, 0, 4, 8);
                    add(editor, editorConstraints);
                }
                controls.add(new OptionControl(field, editor));
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Cannot read option " + field.getName(), exception);
            }
        }

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = row;
        filler.weighty = 1;
        filler.gridwidth = 2;
        add(Box.createVerticalGlue(), filler);
    }

    public void applyTo(Options target) throws IllegalAccessException {
        for (OptionControl control : controls) {
            control.apply(target);
        }
    }

    private JComponent createEditor(Field field, ChoiceArg choice, Object value) {
        Class<?> type = field.getType();
        if (type == boolean.class || type == Boolean.class) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(Boolean.TRUE.equals(value));
            checkBox.setFont(Fonts.BODY);
            checkBox.setForeground(Colors.TEXT);
            checkBox.setOpaque(false);
            return checkBox;
        }
        if (choice != null) {
            JComboBox<String> comboBox = new JComboBox<>();
            comboBox.addItem(null);
            for (String choiceValue : choice.values()) {
                comboBox.addItem(choiceValue);
            }
            comboBox.setSelectedItem(value);
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object item,
                                                               int index, boolean selected,
                                                               boolean focus) {
                    super.getListCellRendererComponent(list, item, index, selected, focus);
                    setText(item == null ? "Default / automatic" : item.toString());
                    return this;
                }
            });
            return comboBox;
        }
        if (Collection.class.isAssignableFrom(type)) {
            JTextArea area = new JTextArea(3, 24);
            area.setFont(Fonts.BODY);
            area.setText(join((Collection<?>) value));
            return new JScrollPane(area);
        }
        if (File.class.isAssignableFrom(type)) {
            JTextField path = new JTextField(value == null ? "" : value.toString());
            JPanel fieldPanel = new JPanel(new BorderLayout(6, 0));
            fieldPanel.setOpaque(false);
            fieldPanel.add(path, BorderLayout.CENTER);
            NeonButton browse = new NeonButton("BROWSE");
            browse.addActionListener(event -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    path.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            });
            fieldPanel.add(browse, BorderLayout.EAST);
            return fieldPanel;
        }
        if (type == int.class) {
            int initialValue = value == null ? 0 : ((Number) value).intValue();
            return new JSpinner(new SpinnerNumberModel(initialValue,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        }
        if (type == Integer.class) {
            return new JTextField(value == null ? "" : value.toString());
        }
        if (type == String.class) {
            return new JTextField(value == null ? "" : value.toString());
        }
        return null;
    }

    private static String join(Collection<?> values) {
        StringBuilder result = new StringBuilder();
        for (Object value : values) {
            if (result.length() > 0) {
                result.append('\n');
            }
            result.append(value);
        }
        return result.toString();
    }

    private static final class OptionControl {
        private final Field field;
        private final JComponent editor;

        private OptionControl(Field field, JComponent editor) {
            this.field = field;
            this.editor = editor;
        }

        @SuppressWarnings("unchecked")
        private void apply(Options target) throws IllegalAccessException {
            Class<?> type = field.getType();
            if (type == boolean.class || type == Boolean.class) {
                field.set(target, ((JCheckBox) editor).isSelected());
            } else if (editor instanceof JComboBox) {
                field.set(target, ((JComboBox<String>) editor).getSelectedItem());
            } else if (Collection.class.isAssignableFrom(type)) {
                Collection<Object> values = (Collection<Object>) field.get(target);
                values.clear();
                Type genericType = field.getGenericType();
                Type itemType = genericType instanceof ParameterizedType
                        ? ((ParameterizedType) genericType).getActualTypeArguments()[0]
                        : String.class;
                String[] lines = ((JTextArea) ((JScrollPane) editor).getViewport()
                        .getView()).getText().split("\\r?\\n");
                for (String line : lines) {
                    String item = line.trim();
                    if (!item.isEmpty()) {
                        values.add(itemType == File.class ? new File(item) : item);
                    }
                }
            } else if (File.class.isAssignableFrom(type)) {
                String path = ((JTextField) ((JPanel) editor).getComponent(0)).getText().trim();
                field.set(target, path.isEmpty() ? null : new File(path));
            } else if (type == int.class) {
                field.setInt(target, ((Number) ((JSpinner) editor).getValue()).intValue());
            } else if (type == Integer.class) {
                String value = ((JTextField) editor).getText().trim();
                field.set(target, value.isEmpty() ? null : Integer.valueOf(value));
            } else if (type == String.class) {
                field.set(target, ((JTextField) editor).getText().trim());
            }
        }
    }
}