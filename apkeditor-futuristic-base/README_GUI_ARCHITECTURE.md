# APKEditor — base GUI futurista

Esta versión parte directamente del código reunido en `todo_el_codigo_limpio.txt`.

## Qué se conserva

Se restauran los 64 archivos fuente y recursos que estaban en el documento original, manteniendo sus paquetes:
- `com.reandroid.apkeditor`
- `compile`
- `decompile`
- `merge`
- `refactor`
- `protect`
- `info`
- `smali`
- `common`
- `utils`
- `com.reandroid.commons`

No se reemplazó el motor existente por una implementación ficticia.

## Nueva arquitectura

```text
                    ┌─────────────────────┐
                    │      GUI Swing      │
                    │ gui/view/components │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │     Operation       │
                    │   core/operations   │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ LegacyOptionsAdapter│
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ Motor APKEditor     │
                    │ Builder/Decompiler  │
                    │ Merger/Refactor...  │
                    └─────────────────────┘
```

La GUI no debe contener lógica de APK. El motor tampoco debe conocer Swing.

## Entrada GUI

```bash
java -cp "..." com.reandroid.apkeditor.gui.GUIApplication
```

El classpath/dependencias deben ser los mismos que ya utiliza el proyecto original.

## Estructura nueva

```text
src/main/java/com/reandroid/apkeditor/
├── core/
│   ├── ExecutionContext.java
│   ├── LegacyOptionsOperation.java
│   ├── Operation.java
│   ├── OperationListener.java
│   ├── OperationRegistry.java
│   └── OperationResult.java
├── operations/
│   └── OperationCatalog.java
├── gui/
│   ├── GUIApplication.java
│   ├── MainWindow.java
│   ├── components/
│   ├── model/
│   ├── navigation/
│   ├── theme/
│   └── view/
└── ... motor existente ...
```

## Regla de mantenimiento

- `core`: contratos y ejecución.
- `operations`: conecta casos de uso con el motor existente.
- `gui`: presentación.
- `CommandExecutor`: sigue siendo parte del motor; solamente publica eventos mediante `ExecutionContext`.
- `cli`: permanece independiente.
- No meter lógica de APK en una vista Swing.
- No meter componentes Swing en `Builder`, `Decompiler`, `Merger`, etc.

## Siguiente refactor recomendado

1. Extraer modelos de configuración GUI de `Options`.
2. Crear adaptadores específicos `DecodeOperation`, `BuildOperation`, `MergeOperation`, etc.
3. Separar validación de entrada/salida del parser CLI.
4. Crear un `ProjectSession` para archivos temporales y proyectos abiertos.
5. Añadir tareas cancelables y progreso real.
6. Añadir tests de `core` sin iniciar Swing.
7. Cuando la arquitectura esté estable, retirar duplicaciones del motor una por una.

## Nota

El ZIP es una base estructural. La integración completa depende de las dependencias externas que no estaban incluidas dentro de `todo_el_codigo_limpio.txt` (por ejemplo ARSCLib, dexlib, jcommander interno del proyecto y demás librerías).
