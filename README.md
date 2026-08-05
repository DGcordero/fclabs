# Cordero F — Aplicación de Gestión de Tareas Personales Privada

**Cordero F** es una aplicación de gestión de tareas personales en español diseñada bajo el principio de **privacidad absoluta y funcionamiento 100% offline**. Cuenta con recordatorios inteligentes, análisis en lenguaje natural, bloqueo por PIN de seguridad y un diseño visual de estilo táctico militar.

---

## 🛡️ Características Principales

- **Garantía de Privacidad y Funcionamiento Offline**: Todos los datos (tareas, notas, fechas, sub-tareas) se guardan localmente en el dispositivo utilizando la base de datos **Room (SQLite)**. No requiere servidor externo ni envía datos a la nube.
- **Protección por PIN de Seguridad**: Sistema de bloqueo numérico de 4 dígitos para impedir el acceso no autorizado a tu agenda personal.
- **Recordatorios Inteligentes**: Sistema de alarmas locales con `AlarmManager` y `NotificationChannel` para no perder ninguna entrega ni evento importante.
- **Entrada Rápida con Lenguaje Natural**: Procesa texto en español (ej. *"Reunión mañana 16:00 urgente trabajo"*) y extrae automáticamente la fecha, hora, categoría y nivel de prioridad.
- **Sugerencia de Sub-tareas**: Generador inteligente de listas de verificación tácticas para descomponer tareas complejas en pasos sencillos.
- **Resumen Diario Inteligente**: Evaluador de urgencia y recomendaciones de enfoque Pomodoro / priorización.
- **Copia de Seguridad Offline (JSON)**: Exportación e importación rápida de datos en formato JSON para respaldar o transferir tu información de manera segura.
- **Diseño Táctico Militar**: Interfaz en tono verde oliva táctico, caqui y cañón de escopeta para máxima legibilidad y menor consumo de batería.

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **Interfaz de Usuario**: Jetpack Compose (Material Design 3)
- **Base de Datos Local**: Room Database + Coroutines & Flow
- **Serialización**: Moshi
- **Alarmas y Notificaciones**: Android AlarmManager + BroadcastReceiver + NotificationCompat
- **Arquitectura**: MVVM (Model-View-ViewModel) + Clean Architecture

---

## 📥 Instrucciones de Instalación y Exportación

### Método 1: Exportar desde Google AI Studio (Recomendado)

Si estás usando esta aplicación dentro de **Google AI Studio**:

1. En la barra superior o panel de configuración de AI Studio, haz clic en **Export Project** o **Push to GitHub**.
2. **Push to GitHub**: Vincula tu cuenta de GitHub para crear automáticamente un nuevo repositorio público o privado con todo el código fuente.
3. **Descargar ZIP**: Descarga el paquete completo comprimido listo para extraer en tu equipo.

---

### Método 2: Compilación Manual del APK (Línea de Comandos / Gradle)

Si has clonado o descargado este repositorio en tu ordenador local:

#### Requisitos Previos
- **JDK 17** o superior instalado y configurado en tu `JAVA_HOME`.
- **Android SDK** (API Level 36 instalado).

#### Pasos para Compilar el APK:

```bash
# 1. Clonar el repositorio (si utilizas Git)
git clone https://github.com/tu-usuario/cordero-f-app.git
cd cordero-f-app

# 2. Dar permisos de ejecución al ejecutable de Gradle (Linux/macOS)
chmod +x gradlew

# 3. Compilar el APK de depuración (Debug APK)
./gradlew assembleDebug
```

El ejecutable APK generado se ubicará en:
`app/build/outputs/apk/debug/app-debug.apk`

Puedes transferir e instalar este archivo `.apk` directamente en tu dispositivo Android activando la opción **"Instalar aplicaciones de fuentes desconocidas"**.

---

### Método 3: Abrir y Ejecutar en Android Studio

1. Abre **Android Studio** (versión Ladybug / Iguana o superior).
2. Selecciona **Open** y navega hasta la carpeta raíz del proyecto `cordero-f-app`.
3. Deja que Gradle sincronice las dependencias del proyecto (`libs.versions.toml`).
4. Conecta tu teléfono Android mediante depuración USB o inicia un Emulador Android.
5. Haz clic en el botón verde **Run 'app'** (`Shift + F10`).

---

## 📋 Licencia y Privacidad

Esta aplicación es software libre de código abierto. Tus datos personales pertenecen única y exclusivamente a ti.
