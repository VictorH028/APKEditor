# APKEDITOR UI

Documentación de la interfaz gráfica de APKEditor.

> Estado actual: interfaz implementada en **Swing + Java2D**. La idea inicial de migrar a JavaFX quedó descartada porque el proyecto ya funciona con Swing en Termux/aarch64 y la interfaz actual cubre el flujo principal.

## 1. Resumen

APKEDITOR UI es una interfaz de escritorio para:

- Abrir y analizar archivos APK/ZIP.
- Explorar el árbol de archivos del paquete.
- Leer código Smali, XML y recursos.
- Ver imágenes binarias como volcado hexadecimal.
- Ejecutar las operaciones existentes del motor: decodificar, construir, fusionar, refactorizar y proteger.
- Consultar logs, progreso y estadísticas del APK.

La interfaz no reimplementa el motor. Las vistas sólo preparan los datos y `OperationService` ejecuta las operaciones registradas en `OperationCatalog`.

## 2. Requisitos

- **JDK 21**.
- Gradle Wrapper incluido en el repositorio (`gradlew`).
- Los archivos de `libs/`:
  - `ARSCLib.jar`
  - `smali.jar`
  - `JCommand.jar`
- Un servidor gráfico X11:
  - En escritorio Linux: Xorg/Wayland con XWayland.
  - En Termux: VNC (`Xvnc`) o `Termux:X11`.
- Para VNC, un visor VNC conectado al puerto correspondiente.

No hace falta instalar Android SDK para ejecutar la interfaz.

## 3. Compilación

Desde la raíz del proyecto:

```bash
sh gradlew compileJava --offline
```

Compilación completa sin pruebas:

```bash
sh gradlew build -x test --offline
```

Jar ejecutable de la GUI con dependencias:

```bash
sh gradlew guiFatJar --offline
```

Fat jar general (CLI + GUI):

```bash
sh gradlew fatJar --offline
```

Artefactos generados:

```text
build/classes/java/main/                  clases compiladas
build/resources/main/                     recursos
build/libs/APKEditor-1.4.9.jar            jar normal
build/libs/APKEditor-1.4.9-all.jar        fat jar general
build/libs/APKEditor-1.4.9-gui.jar        jar ejecutable de la GUI
```

> Usa `sh gradlew` si `./gradlew` falla con `permission denied`.

## 4. Ejecución

### 4.1 Termux con VNC (entorno probado)

Si ya existe un servidor VNC en `:0`:

```bash
cd /data/data/com.termux/files/home/proyecto/APKEditor

export DISPLAY=:0
export XAUTHORITY="$HOME/.Xauthority"

sh gradlew compileJava --offline

java -cp 'build/classes/java/main:build/resources/main:libs/*' \
  com.reandroid.apkeditor.gui.GUIApplication
```

Después abre el visor VNC en `localhost:5900` (o el puerto configurado).

Si el servidor no está iniciado, un ejemplo con TigerVNC es:

```bash
vncserver :0 -geometry 2400x1080 -depth 24
```

### 4.2 Termux con Termux:X11

```bash
export DISPLAY=:0
termux-x11 :0 &
```

Abre la aplicación Termux:X11 y ejecuta la GUI con el mismo comando Java.

### 4.3 Escritorio Linux

```bash
sh gradlew compileJava --offline

java -cp 'build/classes/java/main:build/resources/main:libs/*' \
  com.reandroid.apkeditor.gui.GUIApplication
```

### 4.4 Desde el jar de la GUI

```bash
sh gradlew guiFatJar --offline

java -jar build/libs/APKEditor-1.4.9-gui.jar
```

El fat jar general también contiene la GUI, pero su entrada por defecto es el CLI:

```bash
sh gradlew fatJar --offline

java -cp build/libs/APKEditor-1.4.9-all.jar \
  com.reandroid.apkeditor.gui.GUIApplication
```

> Importante: `sh gradlew run` ejecuta el **CLI** (`com.reandroid.apkeditor.Main`), no la GUI. Usa `guiFatJar` y `java -jar build/libs/APKEditor-1.4.9-gui.jar` para abrir la interfaz.

## 5. Estructura de la ventana

La ventana principal es `MainWindow`:

```text
+------------------------------------------------------------------+
| Topbar: marca | selector de APK | tema | idioma | minimizar/maximizar/cerrar |
+------------+-----------------------------------------------------+
| Sidebar    | Área de contenido                                   |
| 236 px     | Panel / Workspace / Operaciones / Información       |
|            | Configuración                                       |
+------------+-----------------------------------------------------+
| Barra de estado: sistema, memoria y estado de la operación        |
+------------------------------------------------------------------+
```

- Tamaño mínimo: `1200 x 800`.
- Tamaño inicial: hasta `1536 x 1024`, ajustado al tamaño de pantalla.
- Topbar: `74 px`.
- Sidebar: `236 px`.
- Barra de estado: `48 px`.
- La ventana no usa decoración del sistema; se arrastra desde la topbar y se maximiza con doble clic o con el botón correspondiente.

## 6. Navegación

| Elemento del sidebar | Ruta | Destino |
| --- | --- | --- |
| Panel | `DASHBOARD` | Panel de control, estado y accesos rápidos |
| Descompilar | `DECOMPILE` | Workspace en la pestaña Estructura |
| Analizar | `ANALYZE` | Workspace en la pestaña Análisis |
| Visor Smali | `SMALI_VIEWER` | Workspace en la pestaña Código |
| Construir | `BUILD` | Vista de operación Build |
| Fusionar | `MERGE` | Vista de operación Merge |
| Refactorizar | `REFACTOR` | Vista de operación Refactor |
| Proteger | `PROTECT` | Vista de operación Protect |
| Información | `INFORMATION` | Detalle técnico del APK |
| Configuración | `SETTINGS` | Temas, idioma, rutas, motor y atajos |

La ruta `DECODE` existe como alias y se redirige a `DECOMPILE`. La ruta `LOGS` existe en el enum, pero los logs se muestran integrados en el workspace y en las vistas de operación.

## 7. Workspace

El workspace (`DecompileView`) es el área principal de trabajo.

### 7.1 Encabezado

- Título y subtítulo con el APK activo.
- Botón de búsqueda de archivos.
- Botón para restablecer el espacio de trabajo.
- Botón **Descompilar**.
- Pestañas de trabajo: **Estructura**, **Código**, **Manifest**, **Recursos** y **Análisis**.

Las pestañas son vistas del mismo workspace, no rutas independientes:

- **Estructura**: muestra el árbol y enfoca el explorador.
- **Código**: abre el editor y enfoca el documento activo.
- **Manifest**: abre `AndroidManifest.xml`.
- **Recursos**: intenta abrir `strings.xml`; si no existe, abre `resources.arsc`.
- **Análisis**: genera y abre un reporte de texto con los datos de `ApkAnalysis`.

### 7.2 Paneles

El workspace usa `JSplitPane` anidados:

```text
+----------------+---------------------------+------------------+
| Explorador     | Editor de código          | Panel de log     |
| Árbol APK      | Pestañas de documentos    | Eventos          |
+----------------+---------------------------+------------------+
| Dock inferior: progreso | información | estadísticas         |
+---------------------------------------------------------------+
```

- El ancho de los divisores es de `14 px`.
- Los paneles conservan proporciones y mínimos al redimensionar:
  - Explorador: mínimo `240 px`.
  - Editor: mínimo `360 px`.
  - Log: mínimo `260 px`.
- El dock inferior mide `175 px`.

### 7.3 Explorador de archivos

- Árbol con iconos por tipo: Smali, XML, DEX, recursos, imágenes, librerías, manifest, assets, carpetas y archivos.
- Búsqueda incremental por nombre.
- Expansión con un solo clic.
- Al seleccionar un archivo se abre en el editor.
- Se conservan las rutas expandidas al filtrar.
- El árbol se limita a 6000 entradas para mantener la interfaz fluida.

### 7.4 Editor

El editor (`CodeEditor` + `EditorTabs`) incluye:

- Una pestaña por archivo abierto.
- Numeración de líneas.
- Resaltado de línea actual.
- Resaltado de sintaxis para Smali, XML, JSON, Java y texto.
- Estado con `Ln`, `Col`, espacios, codificación y lenguaje.
- Búsqueda dentro del archivo.
- Ir a línea.
- Copiar selección.
- Cierre de pestañas desde la pestaña o desde el menú del editor.
- Vista hexadecimal para archivos binarios.

Límites de lectura:

- Lectura de archivos desde ZIP: hasta 2 MB por entrada.
- Volcado hexadecimal visible: primeros 4096 bytes.

> En esta versión los cambios del editor viven en memoria. Todavía no se escriben de vuelta al APK ni al proyecto descompilado.

## 8. Carga y análisis de APK

Desde la topbar:

1. Pulsa el selector de APK o usa `Ctrl+O`.
2. Elige un archivo `.apk`, `.zip` o `.aab`. El selector permite los tres formatos; el soporte real de AAB depende de las capacidades del motor.
3. `ApkService.analyze()` se ejecuta en segundo plano.
4. Se actualizan:
   - `AppState.currentInput`
   - `AppState.analysis`
   - `AppState.currentTree`
5. Se muestran los mensajes del análisis en el panel de log.
6. La interfaz navega al workspace.

`ApkService` puede analizar APKs, ZIPs y directorios descompilados. La lectura del manifest y el conteo de clases usan ARSCLib y el lector DEX del motor.

## 9. Operaciones del motor

Las operaciones se registran en `OperationCatalog` y se ejecutan a través de `OperationService`.

| Operación | ID | Entrada | Salida |
| --- | --- | --- | --- |
| Descompilar | `decode` | APK | Directorio de proyecto |
| Build | `build` | Directorio descompilado | APK |
| Merge | `merge` | APK | APK fusionado |
| Refactor | `refactor` | APK | APK refactorizado |
| Protect | `protect` | APK | APK protegido |
| Info | `info` | APK | Información del paquete |

`Info` está registrada en el catálogo para el motor; la vista **Información** del sidebar muestra los datos obtenidos durante el análisis del APK.

En la interfaz, Build/Merge/Refactor/Protect comparten `OperationView`. Cada vista permite:

- Escribir o elegir la entrada.
- Escribir o elegir la salida.
- Ejecutar la operación.
- Restablecer el formulario.
- Ver progreso, log y estado al finalizar.

La descompilación se inicia desde el workspace:

- Si no hay APK seleccionado, se abre el selector.
- La salida se genera automáticamente como `<nombre>_decompile`, añadiendo un índice si ya existe.
- Al terminar correctamente, la ruta de salida se guarda en `AppState.currentOutput`.

`OperationService` ejecuta el motor en un hilo secundario y publica los cambios en el EDT. La UI nunca llama directamente a `Decompiler`, `Builder`, `Protector`, etc.

## 10. Atajos de teclado

Atajos implementados:

| Atajo | Acción |
| --- | --- |
| `F5` | Ir al workspace e iniciar descompilación |
| `Ctrl+O` | Seleccionar APK |
| `Ctrl+1` | Volver al workspace |
| `Ctrl+,` | Abrir Configuración |
| `Ctrl+F` | Buscar en el archivo activo |
| `Ctrl+G` | Ir a una línea del editor |
| `Esc` | Cerrar la búsqueda del editor |

La tarjeta de Configuración también muestra `Ctrl+S` y `Ctrl+W`; en esta versión aparecen como referencia, pero todavía no están enlazados a acciones.

## 11. Temas y estilos

Temas disponibles:

| Tema | Característica |
| --- | --- |
| `Futuristic` | Tema por defecto, azul neón sobre fondo oscuro |
| `Cyberpunk` | Morado/rosa neón |
| `Dark` | Gris azulado sobrio |
| `Midnight` | Azul profundo |

Se pueden cambiar desde:

- El selector de tema de la topbar.
- La tarjeta **Apariencia** de Configuración.

Al cambiar el tema, `MainWindow` reconstruye la interfaz y conserva los archivos abiertos mediante `AppState`. El tema no se guarda entre reinicios.

### 11.1 Idioma

La interfaz admite español e inglés:

- Idioma por defecto: español.
- Selector en la topbar, junto al tema.
- Selector en la tarjeta **Apariencia** de Configuración.
- Al cambiar el idioma, `MainWindow` reconstruye la interfaz y conserva la ruta actual y los archivos abiertos mediante `AppState`.
- Las traducciones viven en `src/main/resources/i18n/messages.properties` (inglés base) y `messages_es.properties` (español).
- `I18n` centraliza las traducciones y devuelve la clave si falta un texto.
- El idioma no se guarda entre reinicios.

Paleta principal de `Futuristic`:

| Uso | Color |
| --- | --- |
| Fondo | `#020B18` |
| Fondo secundario | `#041426` |
| Superficie | `#06182A` |
| Borde | `#123653` |
| Primario | `#00BFFF` |
| Primario brillante | `#00D5FF` |
| Éxito | `#00E6A0` |
| Advertencia | `#FFB547` |
| Error | `#FF4668` |
| Texto | `#E6F4FF` |
| Texto secundario | `#9CB4CC` |
| Texto atenuado | `#64829F` |

Tipografías:

- Inter, Segoe UI o SansSerif.
- JetBrains Mono, Cascadia Code, Fira Code, Source Code Pro, Consolas o Monospaced.

Los iconos se dibujan con Java2D mediante `IconFactory`; no se usan imágenes estáticas para los componentes.

## 12. Arquitectura

### 12.1 Paquetes

| Paquete | Responsabilidad |
| --- | --- |
| `gui` | `GUIApplication` y `MainWindow` |
| `gui.view` | Dashboard, workspace, operaciones, información y configuración |
| `gui.panel` | Paneles reutilizables de progreso, información, estadísticas y estado |
| `gui.components` | Botones, tarjetas, campos, barras y controles visuales |
| `gui.editor` | Editor, pestañas, numeración y resaltado Smali |
| `gui.tree` | Explorador de archivos |
| `gui.log` | Panel de logs |
| `gui.model` | Estado compartido y modelos de la UI |
| `gui.service` | Análisis APK, logs y ejecución de operaciones |
| `gui.i18n` | Traducciones español/inglés y cambio de idioma |
| `gui.theme` | Colores, fuentes, iconos y temas |
| `gui.navigation` | Rutas y navegación |
| `core` | Contratos y adaptadores del motor |
| `operations` | Registro central de operaciones existentes |

### 12.2 Flujo de datos

```text
Usuario
   |
   v
Vista / componente
   |
   +--> AppState (estado compartido)
   |
   +--> OperationService
             |
             v
       OperationRegistry
             |
             v
       LegacyOptionsOperation
             |
             v
          Motor APKEditor
             |
             +--> OperationListener
             +--> ExecutionContext
             +--> LogService
                        |
                        v
                     LogPanel
```

### 12.3 Reglas de diseño

- Las vistas no crean ni ejecutan operaciones del motor directamente.
- `AppState` no contiene lógica de descompilación.
- `OperationService` encapsula el trabajo en segundo plano.
- `LogService` es el bus de mensajes de la interfaz.
- Las actualizaciones de UI se publican en el EDT con `SwingUtilities.invokeLater`.
- Los listeners se eliminan en `removeNotify()` para evitar fugas.

## 13. Logs y diagnóstico

`LogService` es un singleton con:

- Niveles: `INFO`, `SUCCESS`, `WARNING`, `ERROR`, `COMMAND`, `DEBUG`.
- Timestamps.
- Autoscroll.
- Limpieza del panel.
- Exportación a archivo de texto.

El panel de log aparece:

- En el workspace.
- En cada vista de operación.

Para diagnosticar un fallo:

1. Revisa el panel de log.
2. Exporta el log con el botón **Exportar**.
3. Verifica la ruta de entrada y salida.
4. Comprueba que el APK/proyecto existe y es legible.
5. Si el error viene del motor, revisa el mensaje completo en el log.

## 14. Solución de problemas

### `No X11 DISPLAY variable was set`

No hay servidor gráfico o falta exportar el display:

```bash
export DISPLAY=:0
```

Verifica que el servidor esté activo:

```bash
pgrep -af Xvnc
```

### La GUI no aparece en Termux

- Confirma que el visor VNC está conectado.
- Comprueba `echo $DISPLAY`.
- Prueba `xrandr -display :0` para verificar que el servidor responde.
- Revisa la salida de Java en la terminal.

### `permission denied: ./gradlew`

Usa:

```bash
sh gradlew compileJava --offline
```

### `Could not find or load main class ...GUIApplication`

- Compila primero con `sh gradlew compileJava --offline`.
- Ejecuta desde la raíz del proyecto.
- Mantén las comillas en el classpath:

```bash
java -cp 'build/classes/java/main:build/resources/main:libs/*' \
  com.reandroid.apkeditor.gui.GUIApplication
```

### `sh gradlew run` abre la consola

Es el comportamiento esperado: el `mainClass` del proyecto es el CLI. Usa el comando de GUI indicado en este documento.

### El APK no se puede analizar

- Verifica que el archivo exista y sea legible.
- Revisa el log para ver si falló el manifest, el DEX o el ZIP.
- Algunos APKs protegidos o corruptos pueden fallar en el motor.
- El árbol se limita a 6000 entradas y la lectura de entradas ZIP a 2 MB.

### La ventana es demasiado pequeña

La ventana tiene un mínimo de `1200 x 800`. Si la pantalla es menor, aumenta la resolución del servidor X/VNC o usa una pantalla externa.

## 15. Estado y limitaciones actuales

Implementado:

- Ventana principal, topbar, sidebar, rutas y barra de estado.
- Dashboard, workspace, información y configuración.
- Temas en caliente.
- Cambio de idioma español/inglés en caliente.
- Carga y análisis de APK.
- Árbol de archivos y búsqueda.
- Editor con resaltado, numeración, búsqueda e ir a línea.
- Descompilación y operaciones Build/Merge/Refactor/Protect.
- Logs con exportación.
- Progreso simulado mientras el motor reporta avance.

Pendiente o limitado:

- Guardar los cambios del editor en el APK o proyecto.
- Atajos `Ctrl+S` y `Ctrl+W`.
- Persistencia del tema, del idioma y de la sesión entre reinicios.
- Formularios avanzados con todas las opciones específicas del motor.
- Ruta dedicada de logs en el sidebar.
- Pruebas automatizadas de la interfaz.

## 16. Referencias

- [README del proyecto](../README.md)
- [Especificación visual](../deseño.json)
- [Referencia visual](../.img/ui_last.png)
- [Repositorio APKEditor](https://github.com/VictorH028/APKEditor)
- `build.gradle`: dependencias, versión y tareas `fatJar` / `guiFatJar`
- `src/main/java/com/reandroid/apkeditor/gui/`: implementación de la interfaz
- `src/main/java/com/reandroid/apkeditor/core/`: contratos del motor
- `src/main/java/com/reandroid/apkeditor/operations/OperationCatalog.java`: registro de operaciones
