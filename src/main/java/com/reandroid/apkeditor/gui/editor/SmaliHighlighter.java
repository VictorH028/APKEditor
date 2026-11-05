package com.reandroid.apkeditor.gui.editor;

import com.reandroid.apkeditor.gui.theme.Colors;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Font;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resaltador de sintaxis real para Smali, XML, JSON y volcados hex.
 * Trabaja sobre StyledDocument, por lo que el editor sigue siendo editable.
 */
public final class SmaliHighlighter {
    private SmaliHighlighter() {}

    private static final Pattern DIRECTIVE = Pattern.compile("(?<![\\w.])\\.[A-Za-z][\\w-]*");
    private static final Pattern TOKEN = Pattern.compile("\\b[a-z][a-z0-9]*(?:[-/][a-z0-9]+)*\\b");
    private static final Pattern REGISTER = Pattern.compile("\\b[vp]\\d+\\b");
    private static final Pattern LABEL = Pattern.compile(":[A-Za-z_][\\w-]*");
    private static final Pattern TYPE = Pattern.compile("L[\\w/$]+;");
    private static final Pattern PRIMITIVE = Pattern.compile("\\b[VZBSCIJFD]\\b");
    private static final Pattern NUMBER = Pattern.compile("-?0x[0-9a-fA-F]+L?|-?\\b\\d+L?\\b");
    private static final Pattern STRING = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"");
    private static final Pattern XML_COMMENT = Pattern.compile("<!--[\\s\\S]*?-->");
    private static final Pattern XML_TAG = Pattern.compile("</?[\\w:.-]+");
    private static final Pattern XML_ATTRIBUTE = Pattern.compile("\\b[\\w:.-]+(?=\\s*=)");
    private static final Pattern JSON_KEY = Pattern.compile("\"[^\"]*\"(?=\\s*:)");
    private static final Pattern HEX_COMMENT = Pattern.compile("^\\s*;.*$");

    private static final Set<String> KEYWORDS = Set.of(
            "nop", "move", "move-result", "move-exception", "return", "return-void",
            "return-object", "return-wide", "const", "const-string", "const-class",
            "const-wide", "const/4", "const/16", "const/high16", "const-wide/16",
            "const-wide/32", "const-wide/high16", "monitor-enter", "monitor-exit",
            "check-cast", "instance-of", "array-length", "new-instance", "new-array",
            "filled-new-array", "filled-new-array/range", "fill-array-data",
            "throw", "goto", "goto/16", "goto/32", "packed-switch", "sparse-switch",
            "cmpl-float", "cmpg-float", "cmpl-double", "cmpg-double", "cmp-long",
            "if-eq", "if-ne", "if-lt", "if-ge", "if-gt", "if-le", "if-eqz", "if-nez",
            "if-ltz", "if-gez", "if-gtz", "if-lez", "aget", "aget-wide", "aget-object",
            "aget-boolean", "aget-byte", "aget-char", "aget-short", "aput", "aput-wide",
            "aput-object", "aput-boolean", "aput-byte", "aput-char", "aput-short",
            "iget", "iget-wide", "iget-object", "iget-boolean", "iget-byte", "iget-char",
            "iget-short", "iput", "iput-wide", "iput-object", "iput-boolean", "iput-byte",
            "iput-char", "iput-short", "sget", "sget-wide", "sget-object", "sget-boolean",
            "sget-byte", "sget-char", "sget-short", "sput", "sput-wide", "sput-object",
            "sput-boolean", "sput-byte", "sput-char", "sput-short", "invoke-virtual",
            "invoke-super", "invoke-direct", "invoke-static", "invoke-interface",
            "invoke-virtual/range", "invoke-super/range", "invoke-direct/range",
            "invoke-static/range", "invoke-interface/range", "invoke-polymorphic",
            "invoke-custom", "invoke-custom/range", "neg-int", "not-int", "neg-long",
            "not-long", "neg-float", "neg-double", "int-to-long", "int-to-float",
            "int-to-double", "long-to-int", "long-to-float", "long-to-double",
            "float-to-int", "float-to-long", "float-to-double", "double-to-int",
            "double-to-long", "double-to-float", "int-to-byte", "int-to-char",
            "int-to-short", "add-int", "sub-int", "mul-int", "div-int", "rem-int",
            "and-int", "or-int", "xor-int", "shl-int", "shr-int", "ushr-int",
            "add-long", "sub-long", "mul-long", "div-long", "rem-long", "and-long",
            "or-long", "xor-long", "shl-long", "shr-long", "ushr-long", "add-float",
            "sub-float", "mul-float", "div-float", "rem-float", "add-double",
            "sub-double", "mul-double", "div-double", "rem-double", "new", "array",
            "public", "private", "protected", "static", "final", "abstract", "native",
            "synchronized", "volatile", "transient", "bridge", "synthetic", "enum",
            "interface", "constructor", "strictfp", "varargs", "declared", "input",
            "output", "subannotation", "annotation", "field", "method", "class", "super",
            "source", "line", "locals", "registers", "param", "end", "prologue",
            "epilogue", "restart", "enable", "disable", "catch", "array-data",
            "packed-switch-data", "sparse-switch-data", "debug", "local", "end-local",
            "true", "false", "null", "void", "boolean", "byte", "char", "short", "int",
            "long", "float", "double", "object");

    public static void highlight(javax.swing.JTextPane pane, String language) {
        StyledDocument doc = pane.getStyledDocument();
        try {
            int length = doc.getLength();
            if (length == 0) {
                return;
            }
            String text = doc.getText(0, length);
            ensureStyles(doc);
            doc.setCharacterAttributes(0, length, doc.getStyle("base"), true);
            switch (language == null ? "smali" : language) {
                case "xml" -> highlightXml(doc, text);
                case "json" -> highlightJson(doc, text);
                case "hex" -> highlightHex(doc, text);
                case "smali" -> highlightSmali(doc, text);
                default -> {
                }
            }
        } catch (Exception ignored) {
            // Un error de resaltado no debe interrumpir la edición.
        }
    }

    private static void highlightSmali(StyledDocument doc, String text) {
        int lineStart = 0;
        while (lineStart <= text.length()) {
            int lineEnd = text.indexOf('\n', lineStart);
            if (lineEnd < 0) {
                lineEnd = text.length();
            }
            highlightSmaliLine(doc, text, lineStart, lineEnd);
            lineStart = lineEnd + 1;
        }
    }

    private static void highlightSmaliLine(StyledDocument doc, String text, int start, int end) {
        if (start >= end) {
            return;
        }
        String line = text.substring(start, end);
        int comment = indexOfComment(line);
        int activeEnd = comment >= 0 ? comment : line.length();

        apply(doc, line, start, activeEnd, DIRECTIVE, "directive");
        applyTokenKeywords(doc, line, start, activeEnd);
        apply(doc, line, start, activeEnd, LABEL, "label");
        apply(doc, line, start, activeEnd, REGISTER, "register");
        apply(doc, line, start, activeEnd, NUMBER, "number");
        apply(doc, line, start, activeEnd, TYPE, "type");
        apply(doc, line, start, activeEnd, PRIMITIVE, "type");
        apply(doc, line, start, activeEnd, STRING, "string");

        if (comment >= 0) {
            int commentStart = start + comment;
            doc.setCharacterAttributes(commentStart, end - commentStart,
                    doc.getStyle("comment"), true);
        }
    }

    private static int indexOfComment(String line) {
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\' && inString) {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
            } else if (c == '#' && !inString) {
                return i;
            }
        }
        return -1;
    }

    private static void applyTokenKeywords(StyledDocument doc, String line, int start, int end) {
        Matcher matcher = TOKEN.matcher(line);
        while (matcher.find()) {
            if (matcher.start() >= end) {
                break;
            }
            if (matcher.end() > end) {
                continue;
            }
            String token = matcher.group();
            if (KEYWORDS.contains(token)) {
                doc.setCharacterAttributes(start + matcher.start(),
                        matcher.end() - matcher.start(), doc.getStyle("keyword"), true);
            }
        }
    }

    private static void highlightXml(StyledDocument doc, String text) {
        apply(doc, text, 0, text.length(), XML_TAG, "directive");
        apply(doc, text, 0, text.length(), XML_ATTRIBUTE, "type");
        apply(doc, text, 0, text.length(), STRING, "string");
        m(doc, text, XML_COMMENT, "comment");
    }

    private static void highlightJson(StyledDocument doc, String text) {
        apply(doc, text, 0, text.length(), JSON_KEY, "directive");
        apply(doc, text, 0, text.length(), STRING, "string");
        apply(doc, text, 0, text.length(), NUMBER, "number");
        int lineStart = 0;
        while (lineStart <= text.length()) {
            int lineEnd = text.indexOf('\n', lineStart);
            if (lineEnd < 0) {
                lineEnd = text.length();
            }
            String line = text.substring(lineStart, lineEnd);
            Matcher matcher = Pattern.compile("\\b(true|false|null)\\b").matcher(line);
            while (matcher.find()) {
                doc.setCharacterAttributes(lineStart + matcher.start(),
                        matcher.end() - matcher.start(), doc.getStyle("keyword"), true);
            }
            lineStart = lineEnd + 1;
        }
    }

    private static void highlightHex(StyledDocument doc, String text) {
        m(doc, text, HEX_COMMENT, "comment");
        Matcher matcher = Pattern.compile("\\b[0-9A-Fa-f]{2,8}\\b").matcher(text);
        while (matcher.find()) {
            doc.setCharacterAttributes(matcher.start(),
                    matcher.end() - matcher.start(), doc.getStyle("number"), true);
        }
    }

    private static void apply(StyledDocument doc, String text, int start, int end,
                             Pattern pattern, String style) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            if (matcher.start() >= end) {
                break;
            }
            if (matcher.end() > end) {
                continue;
            }
            doc.setCharacterAttributes(start + matcher.start(),
                    matcher.end() - matcher.start(), doc.getStyle(style), true);
        }
    }

    private static void m(StyledDocument doc, String text, Pattern pattern, String style) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            doc.setCharacterAttributes(matcher.start(),
                    matcher.end() - matcher.start(), doc.getStyle(style), true);
        }
    }

    private static void ensureStyles(StyledDocument doc) {
        if (doc.getStyle("base") == null) {
            Style base = doc.addStyle("base", null);
            StyleConstants.setForeground(base, Colors.TEXT);

            Style comment = doc.addStyle("comment", base);
            StyleConstants.setForeground(comment, Colors.TEXT_MUTED);
            StyleConstants.setItalic(comment, true);

            Style directive = doc.addStyle("directive", base);
            StyleConstants.setForeground(directive, Colors.CYAN);
            StyleConstants.setBold(directive, true);

            Style keyword = doc.addStyle("keyword", base);
            StyleConstants.setForeground(keyword, Colors.PRIMARY_BRIGHT);

            Style register = doc.addStyle("register", base);
            StyleConstants.setForeground(register, Colors.MAGENTA);

            Style label = doc.addStyle("label", base);
            StyleConstants.setForeground(label, Colors.PURPLE);
            StyleConstants.setBold(label, true);

            Style string = doc.addStyle("string", base);
            StyleConstants.setForeground(string, Colors.GREEN);

            Style number = doc.addStyle("number", base);
            StyleConstants.setForeground(number, Colors.WARNING);

            Style type = doc.addStyle("type", base);
            StyleConstants.setForeground(type, Colors.PRIMARY);
        } else {
            Style base = doc.getStyle("base");
            StyleConstants.setForeground(base, Colors.TEXT);
            StyleConstants.setForeground(doc.getStyle("comment"), Colors.TEXT_MUTED);
            StyleConstants.setForeground(doc.getStyle("directive"), Colors.CYAN);
            StyleConstants.setForeground(doc.getStyle("keyword"), Colors.PRIMARY_BRIGHT);
            StyleConstants.setForeground(doc.getStyle("register"), Colors.MAGENTA);
            StyleConstants.setForeground(doc.getStyle("label"), Colors.PURPLE);
            StyleConstants.setForeground(doc.getStyle("string"), Colors.GREEN);
            StyleConstants.setForeground(doc.getStyle("number"), Colors.WARNING);
            StyleConstants.setForeground(doc.getStyle("type"), Colors.PRIMARY);
        }
    }
}
