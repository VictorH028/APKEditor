package com.reandroid.apkeditor.gui.theme;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tipografías de la aplicación. Usa Inter / JetBrains Mono cuando están
 * instaladas y cae en las fuentes lógicas del sistema cuando no lo están.
 */
public final class Fonts {
    private Fonts() {}

    private static final String SANS = resolve("Inter", "Segoe UI", "SansSerif");
    private static final String MONO_FAMILY = resolveMonospace();

    public static final Font TITLE = new Font(SANS, Font.BOLD, 22);
    public static final Font SECTION = new Font(SANS, Font.BOLD, 14);
    public static final Font SUBTITLE = new Font(SANS, Font.PLAIN, 12);
    public static final Font BODY = new Font(SANS, Font.PLAIN, 13);
    public static final Font BODY_MEDIUM = new Font(SANS, Font.BOLD, 12);
    public static final Font SMALL = new Font(SANS, Font.PLAIN, 11);
    public static final Font TINY = new Font(SANS, Font.PLAIN, 10);
    public static final Font MONO = new Font(MONO_FAMILY, Font.PLAIN, 13);
    public static final Font MONO_SMALL = new Font(MONO_FAMILY, Font.PLAIN, 11);
    public static final Font CODE = new Font(MONO_FAMILY, Font.PLAIN, 13);

    private static String resolve(String... families) {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getAvailableFontFamilyNames()));
        for (String family : families) {
            if (available.contains(family)) {
                return family;
            }
        }
        return "SansSerif";
    }

    private static String resolveMonospace() {
        return resolve("JetBrains Mono", "Cascadia Code", "Fira Code",
                "Source Code Pro", "Consolas", "Monospaced");
    }
}
