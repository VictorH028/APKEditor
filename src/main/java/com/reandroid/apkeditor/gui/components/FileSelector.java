package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public final class FileSelector extends JPanel {
    private final JTextField field = new JTextField();

    public FileSelector(String label) {
        setLayout(new BorderLayout(8, 4));
        setBackground(Colors.BACKGROUND);

        JLabel title = new JLabel(label);
        title.setFont(Fonts.SMALL);
        title.setForeground(Colors.TEXT_MUTED);

        NeonButton browse = new NeonButton("BROWSE");
        browse.addActionListener(e -> choose());

        field.setFont(Fonts.BODY);
        field.setBorder(BorderFactory.createLineBorder(Colors.BORDER));

        JPanel center = new JPanel(new BorderLayout(6, 0));
        center.setOpaque(false);
        center.add(field, BorderLayout.CENTER);
        center.add(browse, BorderLayout.EAST);

        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void choose() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public File getFile() {
        String value = field.getText().trim();
        return value.isEmpty() ? null : new File(value);
    }

    public void setFile(File file) {
        field.setText(file == null ? "" : file.getAbsolutePath());
    }
}
