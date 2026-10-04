# Arquitectura

## Capas

### Core
Contratos pequeños y agnósticos de interfaz.

### Operations
Un único punto para registrar las operaciones disponibles.

### GUI
Swing puro. Ninguna clase GUI importa `Builder`, `Decompiler`, etc. directamente.

### Legacy engine
El código original permanece en sus paquetes para minimizar el riesgo de romper imports.

## Flujo de una operación

```text
GUI
 ↓
OperationView
 ↓
LegacyOptionsOperation
 ↓
Options.validate()
 ↓
Options.runCommand()
 ↓
Builder / Decompiler / Merger / ...
 ↓
CommandExecutor
 ↓
ExecutionContext
 ↓
OperationListener
 ↓
LogConsole / ProgressPanel
```

Esto permite que la misma operación pueda ser llamada desde CLI, GUI o una futura API.
