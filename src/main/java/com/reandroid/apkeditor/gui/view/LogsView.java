package com.reandroid.apkeditor.gui.view;

import com.reandroid.apkeditor.gui.components.LogConsole;
import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.theme.*;

import javax.swing.*;
import java.awt.*;

public final class LogsView extends JPanel {
    public LogsView(LogConsole console) {
        setLayout(new BorderLayout(0, 10));
        setBackground(Colors.BACKGROUND);
        setBorder(Theme.empty(24));

        JPanel header = Theme.panel(new BorderLayout());
        JLabel title = new JLabel(I18n.t("logs.title"));
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT);

        NeonButton clear = new NeonButton(I18n.t("logs.clear"));
        clear.addActionListener(e -> console.clear());

        header.add(title, BorderLayout.WEST);
        header.add(clear, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(console, BorderLayout.CENTER);
    }
}
