package com.reandroid.apkeditor.gui.theme;

import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.*;

/**
 * Iconos vectoriales dibujados con Java2D. Evita depender de imágenes
 * estáticas y mantiene el mismo lenguaje visual en todos los temas.
 */
public final class IconFactory {
    private IconFactory() {}

    public static Icon icon(String name, int size) {
        return icon(name, size, Colors.TEXT_SECONDARY);
    }

    public static Icon icon(String name, int size, Color color) {
        return new VectorIcon(name, size, color);
    }

    private static final class VectorIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;

        private VectorIcon(String name, int size, Color color) {
            this.name = name;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                        RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                g2.scale(size / 24.0, size / 24.0);
                paint(g2, name, color);
            } finally {
                g2.dispose();
            }
        }
    }

    private static void paint(Graphics2D g, String name, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (name == null ? "" : name) {
            case "house" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3, 11);
                p.lineTo(12, 3.5);
                p.lineTo(21, 11);
                g.draw(p);
                g.draw(new RoundRectangle2D.Double(6, 10.5, 12, 10, 2, 2));
                g.draw(new RoundRectangle2D.Double(10.2, 14.5, 3.6, 6, 1, 1));
            }
            case "file-code" -> {
                fileOutline(g);
                Path2D p = new Path2D.Double();
                p.moveTo(9.2, 13.2);
                p.lineTo(7.6, 15.3);
                p.lineTo(9.2, 17.4);
                p.moveTo(14.8, 13.2);
                p.lineTo(16.4, 15.3);
                p.lineTo(14.8, 17.4);
                g.draw(p);
            }
            case "binary" -> {
                g.draw(new RoundRectangle2D.Double(4, 4, 16, 16, 3, 3));
                g.setFont(new Font("Monospaced", Font.BOLD, 8));
                g.drawString("10", 7, 11);
                g.drawString("01", 11, 18);
            }
            case "scan-search", "search" -> {
                g.draw(new Ellipse2D.Double(4.5, 4.5, 12, 12));
                g.draw(new Line2D.Double(15.5, 15.5, 20, 20));
                if ("scan-search".equals(name)) {
                    g.draw(new Line2D.Double(7.5, 10.5, 13.5, 10.5));
                }
            }
            case "package" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 3);
                p.lineTo(20.5, 7.2);
                p.lineTo(20.5, 16.8);
                p.lineTo(12, 21);
                p.lineTo(3.5, 16.8);
                p.lineTo(3.5, 7.2);
                p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(3.8, 7.4, 12, 11.6));
                g.draw(new Line2D.Double(20.2, 7.4, 12, 11.6));
                g.draw(new Line2D.Double(12, 11.6, 12, 20.6));
            }
            case "git-merge" -> {
                g.draw(new Ellipse2D.Double(5, 3.5, 5, 5));
                g.draw(new Ellipse2D.Double(14, 15.5, 5, 5));
                g.draw(new Ellipse2D.Double(5, 15.5, 5, 5));
                Path2D p = new Path2D.Double();
                p.moveTo(7.5, 8.5);
                p.lineTo(7.5, 12);
                p.quadTo(7.5, 18, 14, 18);
                g.draw(p);
            }
            case "workflow" -> {
                g.draw(new RoundRectangle2D.Double(3, 4, 7, 5, 1.5, 1.5));
                g.draw(new RoundRectangle2D.Double(14, 4, 7, 5, 1.5, 1.5));
                g.draw(new RoundRectangle2D.Double(8.5, 15, 7, 5, 1.5, 1.5));
                g.draw(new Line2D.Double(10, 6.5, 14, 6.5));
                g.draw(new Line2D.Double(6.5, 9, 6.5, 12));
                g.draw(new Line2D.Double(17.5, 9, 17.5, 12));
                Path2D p = new Path2D.Double();
                p.moveTo(6.5, 12);
                p.lineTo(12, 12);
                p.lineTo(12, 15);
                g.draw(p);
            }
            case "shield" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 3);
                p.lineTo(20, 6);
                p.lineTo(20, 12.5);
                p.quadTo(20, 18, 12, 21);
                p.quadTo(4, 18, 4, 12.5);
                p.lineTo(4, 6);
                p.closePath();
                g.draw(p);
                Path2D check = new Path2D.Double();
                check.moveTo(8.2, 12.2);
                check.lineTo(11, 15);
                check.lineTo(16, 9.5);
                g.draw(check);
            }
            case "info" -> {
                g.draw(new Ellipse2D.Double(4, 4, 16, 16));
                g.draw(new Line2D.Double(12, 10.5, 12, 17));
                g.fill(new Ellipse2D.Double(11.1, 7, 1.8, 1.8));
            }
            case "settings" -> {
                g.draw(new Ellipse2D.Double(7, 7, 10, 10));
                g.draw(new Ellipse2D.Double(10.2, 10.2, 3.6, 3.6));
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * 2 * i / 8.0;
                    double x1 = 12 + Math.cos(a) * 7.1;
                    double y1 = 12 + Math.sin(a) * 7.1;
                    double x2 = 12 + Math.cos(a) * 9.2;
                    double y2 = 12 + Math.sin(a) * 9.2;
                    g.draw(new Line2D.Double(x1, y1, x2, y2));
                }
            }
            case "close" -> {
                g.draw(new Line2D.Double(6, 6, 18, 18));
                g.draw(new Line2D.Double(18, 6, 6, 18));
            }
            case "minimize" -> g.draw(new Line2D.Double(6, 12, 18, 12));
            case "maximize" -> g.draw(new RoundRectangle2D.Double(6, 6, 12, 12, 1.5, 1.5));
            case "restore" -> {
                g.draw(new RoundRectangle2D.Double(5, 8, 11, 11, 1.5, 1.5));
                g.draw(new RoundRectangle2D.Double(8, 5, 11, 11, 1.5, 1.5));
            }
            case "trash" -> {
                g.draw(new Line2D.Double(4.5, 7, 19.5, 7));
                g.draw(new Line2D.Double(9, 4, 15, 4));
                Path2D p = new Path2D.Double();
                p.moveTo(6, 7);
                p.lineTo(7.2, 20);
                p.lineTo(16.8, 20);
                p.lineTo(18, 7);
                g.draw(p);
                g.draw(new Line2D.Double(10, 10.5, 10.4, 17));
                g.draw(new Line2D.Double(14, 10.5, 13.6, 17));
            }
            case "download" -> {
                g.draw(new Line2D.Double(12, 4, 12, 15));
                Path2D p = new Path2D.Double();
                p.moveTo(7.5, 10.5);
                p.lineTo(12, 15);
                p.lineTo(16.5, 10.5);
                g.draw(p);
                Path2D tray = new Path2D.Double();
                tray.moveTo(4, 15);
                tray.lineTo(4, 20);
                tray.lineTo(20, 20);
                tray.lineTo(20, 15);
                g.draw(tray);
            }
            case "copy" -> {
                g.draw(new RoundRectangle2D.Double(8, 8, 11, 11, 2, 2));
                Path2D p = new Path2D.Double();
                p.moveTo(16, 8);
                p.lineTo(16, 5);
                p.lineTo(5, 5);
                p.lineTo(5, 16);
                p.lineTo(8, 16);
                g.draw(p);
            }
            case "folder" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3.5, 6.5);
                p.lineTo(9.5, 6.5);
                p.lineTo(11.2, 8.5);
                p.lineTo(20.5, 8.5);
                p.lineTo(20.5, 18.5);
                p.lineTo(3.5, 18.5);
                p.closePath();
                g.draw(p);
            }
            case "folder-open" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3.5, 7);
                p.lineTo(3.5, 18.5);
                p.lineTo(6, 11);
                p.lineTo(21, 11);
                p.lineTo(18.5, 18.5);
                p.lineTo(6, 18.5);
                g.draw(p);
                g.draw(new Line2D.Double(3.5, 7, 9, 7));
            }
            case "file" -> fileOutline(g);
            case "archive" -> {
                g.draw(new RoundRectangle2D.Double(4.5, 5, 15, 15, 2, 2));
                g.draw(new Line2D.Double(9, 5, 9, 9));
                g.draw(new Line2D.Double(12, 5, 12, 9));
                g.draw(new Line2D.Double(15, 5, 15, 9));
                g.draw(new RoundRectangle2D.Double(10.5, 11, 3, 4, 1, 1));
            }
            case "android" -> {
                Path2D head = new Path2D.Double();
                head.moveTo(5, 10);
                head.quadTo(12, 6.5, 19, 10);
                head.lineTo(19, 15);
                head.quadTo(19, 18, 16, 18);
                head.lineTo(8, 18);
                head.quadTo(5, 18, 5, 15);
                head.closePath();
                g.draw(head);
                g.draw(new Line2D.Double(8, 6.5, 6.5, 4));
                g.draw(new Line2D.Double(16, 6.5, 17.5, 4));
                g.fill(new Ellipse2D.Double(8.8, 11.2, 1.5, 1.5));
                g.fill(new Ellipse2D.Double(13.7, 11.2, 1.5, 1.5));
            }
            case "layers" -> {
                Path2D a = new Path2D.Double();
                a.moveTo(12, 3.5);
                a.lineTo(21, 8);
                a.lineTo(12, 12.5);
                a.lineTo(3, 8);
                a.closePath();
                g.draw(a);
                g.draw(new Line2D.Double(3, 12, 12, 16.5));
                g.draw(new Line2D.Double(21, 12, 12, 16.5));
                g.draw(new Line2D.Double(3, 15.7, 12, 20.2));
                g.draw(new Line2D.Double(21, 15.7, 12, 20.2));
            }
            case "blocks" -> {
                g.draw(new RoundRectangle2D.Double(4, 4, 6.5, 6.5, 1.2, 1.2));
                g.draw(new RoundRectangle2D.Double(13.5, 4, 6.5, 6.5, 1.2, 1.2));
                g.draw(new RoundRectangle2D.Double(4, 13.5, 6.5, 6.5, 1.2, 1.2));
                g.draw(new RoundRectangle2D.Double(13.5, 13.5, 6.5, 6.5, 1.2, 1.2));
            }
            case "chevron-right" -> {
                g.draw(new Line2D.Double(9.5, 6, 15.5, 12));
                g.draw(new Line2D.Double(15.5, 12, 9.5, 18));
            }
            case "chevron-down" -> {
                g.draw(new Line2D.Double(6, 9.5, 12, 15.5));
                g.draw(new Line2D.Double(12, 15.5, 18, 9.5));
            }
            case "check" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(5, 12.5);
                p.lineTo(10, 17.5);
                p.lineTo(19, 7);
                g.draw(p);
            }
            case "clock" -> {
                g.draw(new Ellipse2D.Double(4, 4, 16, 16));
                g.draw(new Line2D.Double(12, 7.5, 12, 12.3));
                g.draw(new Line2D.Double(12, 12.3, 15.5, 14));
            }
            case "play" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(7, 4.5);
                p.lineTo(19.5, 12);
                p.lineTo(7, 19.5);
                p.closePath();
                g.fill(p);
            }
            case "terminal" -> {
                g.draw(new RoundRectangle2D.Double(3.5, 5, 17, 14, 2, 2));
                Path2D p = new Path2D.Double();
                p.moveTo(7, 9.5);
                p.lineTo(10, 12);
                p.lineTo(7, 14.5);
                g.draw(p);
                g.draw(new Line2D.Double(12.5, 15, 16.5, 15));
            }
            case "code" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(9, 7);
                p.lineTo(4.5, 12);
                p.lineTo(9, 17);
                p.moveTo(15, 7);
                p.lineTo(19.5, 12);
                p.lineTo(15, 17);
                g.draw(p);
            }
            case "menu" -> {
                g.draw(new Line2D.Double(4, 7, 20, 7));
                g.draw(new Line2D.Double(4, 12, 20, 12));
                g.draw(new Line2D.Double(4, 17, 20, 17));
            }
            case "refresh" -> {
                g.draw(new Arc2D.Double(4, 4, 16, 16, 35, 280, Arc2D.OPEN));
                Path2D p = new Path2D.Double();
                p.moveTo(18.5, 4.5);
                p.lineTo(19.5, 9);
                p.lineTo(15, 8);
                g.draw(p);
            }
            default -> {
                g.draw(new Ellipse2D.Double(4, 4, 16, 16));
                g.fill(new Ellipse2D.Double(10.5, 10.5, 3, 3));
            }
        }
    }

    private static void fileOutline(Graphics2D g) {
        Path2D p = new Path2D.Double();
        p.moveTo(6, 3.5);
        p.lineTo(14, 3.5);
        p.lineTo(19, 8.5);
        p.lineTo(19, 20.5);
        p.lineTo(6, 20.5);
        p.closePath();
        g.draw(p);
        Path2D fold = new Path2D.Double();
        fold.moveTo(14, 3.5);
        fold.lineTo(14, 8.5);
        fold.lineTo(19, 8.5);
        g.draw(fold);
    }
}
