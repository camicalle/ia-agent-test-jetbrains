# ia-agent-test — Plugin de JetBrains

Plugin para distribuir y mantener actualizado el agente AI de pruebas en cualquier proyecto. Estandariza las buenas prácticas del equipo a través de archivos Markdown que la IA de tu editor lee para entender cómo trabajar correctamente en el proyecto.

Disponible para todos los IDEs de JetBrains: IntelliJ IDEA, PyCharm, WebStorm, GoLand, y más.

[![JetBrains Marketplace](https://img.shields.io/badge/JetBrains-Marketplace-blue)](https://plugins.jetbrains.com)

---

## ¿Qué hace este plugin?

Cuando lo instalas, el plugin es capaz de:

1. **Copiar** la carpeta del agente con todos sus archivos `.md` a tu proyecto la primera vez que lo inicializas
2. **Actualizar** automáticamente esos archivos cada vez que abres un proyecto que ya fue inicializado, si hay una nueva versión del plugin disponible
3. **Remover** la carpeta del agente del proyecto si ya no la necesitas
4. **No tocar** ningún proyecto que no hayas inicializado previamente

Los archivos `.md` contienen instrucciones, buenas prácticas y contexto para que la IA de tu editor (Copilot, Cursor, etc.) entienda los estándares del equipo y trabaje bajo esas reglas.

---

## Estructura del proyecto

```
.
├── .run/                          Configuraciones Run/Debug predefinidas
├── gradle/
│   ├── wrapper/                   Gradle Wrapper
│   └── libs.versions.toml         Catálogo de versiones
├── src/
│   └── main/
│       ├── kotlin/                Código fuente del plugin
│       │   ├── InitializeAgentAction.kt   Comando para inicializar el agente
│       │   ├── RemoveAgentAction.kt       Comando para remover el agente
│       │   └── AgentStartupActivity.kt   Actualización automática al abrir proyecto
│       └── resources/             Recursos del plugin
│           ├── META-INF/
│           │   ├── plugin.xml     Manifiesto del plugin
│           │   └── pluginIcon.svg Logo del plugin
│           └── templates/
│               └── .ai-agent-test/  Archivos del agente AI
│                   ├── agents/
│                   └── instructions/
├── build.gradle.kts               Configuración de Gradle
├── gradle.properties              Propiedades de Gradle
└── README.md                      Este archivo
```

---

## Estructura de carpetas que genera en tu proyecto

Cuando inicializas el plugin en tu proyecto, aparece la siguiente estructura:

```
tu-proyecto/
├── src/
├── pom.xml
└── .ai-agent-test/              ← generado por el plugin — no modificar
    ├── agents/
    │   └── one-agent.md         ← instrucciones del agente
    └── instructions/
        └── one-instructions.md  ← instrucciones generales para la IA
```

> ⚠️ **Importante:** No modifiques manualmente los archivos dentro de `.ai-agent-test`. Son gestionados por el plugin y cualquier cambio manual será sobreescrito en la próxima actualización.

---

## ¿Se sube al repositorio git?

Sí. Se recomienda subir la carpeta `.ai-agent-test` al repositorio para que cualquier persona que clone el proyecto ya tenga el contexto del agente disponible sin necesidad de ejecutar el comando de inicialización. El plugin se encarga de mantener los archivos actualizados cuando haya una nueva versión.

---

## Flujo completo de uso

### Primera vez — Inicializar el agente en un proyecto

```
1. Instalar el plugin desde el Marketplace de JetBrains
         ↓
2. Abrir el proyecto en IntelliJ IDEA u otro IDE de JetBrains
         ↓
3. Ir al menú Tools
         ↓
4. Click en "Initialize Agent Test"
         ↓
5. La carpeta .ai-agent-test aparece en el proyecto con todos los archivos
```

### Actualizaciones — Cuando hay una nueva versión del plugin

```
1. El autor actualiza los .md y publica una nueva versión del plugin
         ↓
2. JetBrains notifica al usuario que hay una actualización disponible
         ↓
3. El usuario actualiza el plugin con un click
         ↓
4. La próxima vez que abra cada proyecto:
         ↓
   ¿El proyecto tiene .ai-agent-test?
   /                            \
  SÍ                            NO
   ↓                             ↓
Los archivos se          No pasa nada.
actualizan               Cuando el usuario quiera
automáticamente          inicializar, ejecuta
                         Tools → Initialize Agent Test
```

### Remover el agente de un proyecto

```
1. Ir al menú Tools
         ↓
2. Click en "Remove Agent Test"
         ↓
3. Aparece confirmación: "¿Estás seguro de que quieres eliminar .ai-agent-test?"
         ↓
   /              \
  Sí, eliminar    Cancelar
   ↓                ↓
Se elimina       No pasa nada
.ai-agent-test
del proyecto
```

### Ejemplo con múltiples proyectos

```
proyecto-1 ✅ tiene .ai-agent-test → se actualiza automáticamente al abrir
proyecto-2 ✅ tiene .ai-agent-test → se actualiza automáticamente al abrir
proyecto-3 ✅ tiene .ai-agent-test → se actualiza automáticamente al abrir
proyecto-4 ❌ no tiene .ai-agent-test → no pasa nada
```

---

## Comandos disponibles

| Comando | Cómo ejecutarlo | Qué hace |
|---|---|---|
| `Initialize Agent Test` | `Tools → Initialize Agent Test` | Copia la carpeta `.ai-agent-test` con todos los archivos al proyecto por primera vez |
| `Remove Agent Test` | `Tools → Remove Agent Test` | Pide confirmación y elimina la carpeta `.ai-agent-test` del proyecto |

---

## Explicación de cada clase del código

### `InitializeAgentAction.kt`

**¿Cuándo corre?**
Solo cuando el usuario hace click en `Tools → Initialize Agent Test`. Nunca corre de forma automática.

**¿Qué hace paso a paso?**

1. Verifica que haya un proyecto abierto. Si no hay ninguno, muestra un error y para
2. Obtiene la ruta del proyecto abierto actualmente en el IDE
3. Busca la carpeta `templates/.ai-agent-test` dentro del plugin empaquetado
4. Detecta si está corriendo en modo desarrollo o empaquetado en jar y usa la estrategia correcta
5. Copia toda la estructura de archivos al proyecto del usuario en `.ai-agent-test`
6. Muestra el mensaje de confirmación **"✅ Agente AI inicializado en tu proyecto!"**

**¿Cuándo se usa?**
Únicamente la primera vez que un desarrollador quiere incorporar el agente a su proyecto. A partir de ese momento las actualizaciones son manejadas automáticamente por `AgentStartupActivity`.

---

### `RemoveAgentAction.kt`

**¿Cuándo corre?**
Solo cuando el usuario hace click en `Tools → Remove Agent Test`. Nunca corre de forma automática.

**¿Qué hace paso a paso?**

1. Verifica que haya un proyecto abierto. Si no hay ninguno, muestra un error y para
2. Busca la carpeta `.ai-agent-test` en el proyecto actual
3. Si no la encuentra, muestra un aviso y para
4. Si la encuentra, muestra un diálogo de confirmación antes de eliminar
5. Si el usuario confirma, elimina la carpeta completa con todo su contenido
6. Si el usuario cancela, no hace nada

**¿Por qué pide confirmación?**
Para evitar que alguien elimine el agente por accidente. Es una acción destructiva que no se puede deshacer fácilmente.

---

### `AgentStartupActivity.kt`

**¿Cuándo corre?**
Automáticamente cada vez que JetBrains abre un proyecto, sin que el usuario tenga que hacer nada. Es equivalente al `onStartupFinished` de VS Code.

**¿Qué hace paso a paso?**

1. Verifica que haya un proyecto abierto. Si no hay ninguno, para silenciosamente
2. Busca la carpeta `.ai-agent-test` en el proyecto actual
3. Si la encuentra — el proyecto ya fue inicializado — copia los archivos nuevos encima de los existentes
4. Si no la encuentra — el proyecto nunca fue inicializado — no hace absolutamente nada

**La clave de esta clase:**
Es la que garantiza que si tienes varios proyectos, solo se actualicen los que el usuario decidió inicializar. Los demás permanecen intactos.

---

## Diagrama de llamadas entre clases

```
JetBrains abre proyecto
        ↓
AgentStartupActivity.execute()
        ↓
¿Existe .ai-agent-test?
   /              \
  SÍ              NO
   ↓               ↓
copyAgentToProject()   No hace nada
        ↓
Archivos actualizados

Usuario ejecuta Tools →
Initialize Agent Test
        ↓
InitializeAgentAction.actionPerformed()
        ↓
copyAgentToProject()
        ↓
Archivos copiados por primera vez

Usuario ejecuta Tools →
Remove Agent Test
        ↓
RemoveAgentAction.actionPerformed()
        ↓
¿Confirma eliminación?
   /              \
  SÍ              NO
   ↓               ↓
deleteRecursively()  No hace nada
```

---

## Configuraciones Run/Debug predefinidas

| Configuración | Descripción |
|---|---|
| `Run IDE with Plugin` | Lanza una instancia de IntelliJ con el plugin cargado para pruebas |
| `Run Tests` | Ejecuta los tests del plugin |
| `Run Verifications` | Verifica la compatibilidad del plugin con los IDEs especificados |

Para probar el plugin localmente usa **Run IDE with Plugin** (▶️) y ve a `Tools` en la ventana que se abre.

---

## Build script

El archivo `build.gradle.kts` aplica tres plugins de Gradle:

| Plugin | Descripción |
|---|---|
| `org.jetbrains.kotlin.jvm` | Soporte para Kotlin |
| `org.jetbrains.changelog` | Simplifica la gestión del CHANGELOG |
| `org.jetbrains.intellij.platform` | Plugin de Gradle para la plataforma IntelliJ |

La versión del IDE contra la que se compila se configura en:

```kotlin
intellijIdea("2025.3.5")
```

---

## Publicar el plugin

### Manualmente

1. Ejecuta el task de Gradle:
```bash
./gradlew buildPlugin
```
2. El archivo `.zip` se genera en `build/distributions/`
3. Ve a [plugins.jetbrains.com/plugin/upload](https://plugins.jetbrains.com/plugin/upload)
4. Sube el archivo `.zip`

### Automáticamente con Gradle

```bash
./gradlew publishPlugin
```

Requiere configurar el token en `gradle.properties`:
```
pluginVerifierIdeVersions = 2025.3.5
org.gradle.configuration-cache = true
```

---

## Preguntas frecuentes

**¿Puedo modificar los archivos `.md` dentro de `.ai-agent-test`?**
No. Son gestionados por el plugin y se sobreescriben con cada actualización. Si quieres agregar instrucciones propias del proyecto, crea una carpeta separada fuera de `.ai-agent-test`.

**¿Qué pasa si desinstalo el plugin?**
La carpeta `.ai-agent-test` permanece en tus proyectos. El plugin no la elimina al desinstalarse. Si la quieres quitar, usa el comando `Remove Agent Test` antes de desinstalar.

**¿Funciona en todos los IDEs de JetBrains?**
Sí. Al depender solo de `com.intellij.modules.platform` funciona en IntelliJ IDEA, PyCharm, WebStorm, GoLand, Rider y cualquier otro IDE de JetBrains.

**¿El plugin funciona si no tengo ningún proyecto abierto?**
Sí, se instala sin problema. Simplemente no hace nada hasta que abras un proyecto.

**¿El comando Remove elimina sin preguntar?**
No. Siempre muestra un diálogo de confirmación antes de eliminar para evitar borrados accidentales.

---

## Versiones

| Versión | Cambios |
|---|---|
| 1.0.0 | Lanzamiento inicial — inicialización, actualización automática y remoción del agente de pruebas |

---

## Links útiles

- [IntelliJ Platform SDK][docs]
- [IntelliJ Platform Gradle Plugin][docs:intellij-platform-gradle-plugin-docs]
- [JetBrains Marketplace Quality Guidelines][jb:quality-guidelines]
- [Plugin Configuration File][docs:plugin.xml]
- [Publishing a Plugin][docs:publishing]

[docs]: https://plugins.jetbrains.com/docs/intellij
[docs:plugin.xml]: https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html
[docs:publishing]: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html
[docs:intellij-platform-gradle-plugin-docs]: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
[jb:quality-guidelines]: https://plugins.jetbrains.com/docs/marketplace/quality-guidelines.html