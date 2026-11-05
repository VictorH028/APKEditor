package com.reandroid.apkeditor.gui.service;

import com.reandroid.apkeditor.gui.model.LogEntry;
import com.reandroid.apkeditor.gui.model.LogLevel;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bus de logs de la interfaz. Cualquier capa puede publicar mensajes sin
 * conocer los componentes visuales que los muestran.
 */
public final class LogService {
    public interface Listener {
        void onEntry(LogEntry entry);
        void onCleared();
    }

    private static final LogService INSTANCE = new LogService();

    private final List<LogEntry> entries = new ArrayList<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private LogService() {
    }

    public static LogService get() {
        return INSTANCE;
    }

    public synchronized void add(LogLevel level, String message) {
        if (message == null) {
            return;
        }
        LogEntry entry = new LogEntry(level, message);
        entries.add(entry);
        for (Listener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.onEntry(entry));
        }
    }

    public void info(String message) {
        add(LogLevel.INFO, message);
    }

    public void success(String message) {
        add(LogLevel.SUCCESS, message);
    }

    public void warning(String message) {
        add(LogLevel.WARNING, message);
    }

    public void error(String message) {
        add(LogLevel.ERROR, message);
    }

    public void command(String message) {
        add(LogLevel.COMMAND, message);
    }

    public void debug(String message) {
        add(LogLevel.DEBUG, message);
    }

    public synchronized List<LogEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized void clear() {
        entries.clear();
        for (Listener listener : listeners) {
            SwingUtilities.invokeLater(listener::onCleared);
        }
    }

    public synchronized void addListener(Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public synchronized void seedDemoIfEmpty() {
        if (!entries.isEmpty()) {
            return;
        }
        info("Estableciendo entorno de trabajo...");
        info("Cargando APK: MyApplication.apk");
        info("Verificando estructura del archivo...");
        info("Descompilación iniciada...");
        info("Extrayendo recursos...");
        info("Generando código smali...");
        info("Analizando manifest...");
        info("Procesando librerías...");
        success("Descompilación completada.");
        info("Iniciando análisis de la aplicación...");
        info("Analizando permisos...");
        info("Detectando componentes...");
        info("Revisando vulnerabilidades...");
        success("Análisis completado.");
    }

    public synchronized void export(File file) throws IOException {
        if (file == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (LogEntry entry : entries) {
            builder.append(entry.toPlainText()).append(System.lineSeparator());
        }
        Files.writeString(file.toPath(), builder.toString(), StandardCharsets.UTF_8);
    }
}
