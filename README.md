# Cordero F — Gestor de Vida Diaria, Citas, Tareas y Hábitos

**Cordero F** es una aplicación de gestión de vida diaria personal en español diseñada bajo el principio de **privacidad absoluta, cero APIs externas y funcionamiento 100% offline**. Cuenta con agenda de citas y eventos, calendario táctico mensual, seguimiento de hábitos y rutinas con rachas, matriz de priorización Eisenhower local, notificaciones push locales en el dispositivo móvil y un diseño visual de alta legibilidad.

---

## 🛡️ Características Principales

- **Gestión Integral de Agenda, Citas y Tareas**: Organización por categorías dedicadas (*Citas & Eventos, Trabajo, Personal, Salud, Finanzas, Hogar, Compras, Estudio*), niveles de prioridad y estados de cumplimiento.
- **Calendario Táctico Mensual e Interactivo**: Vista mensual y diaria detallada para explorar citas programadas, agregar eventos rápidamente seleccionando días específicos en el calendario y filtrar compromisos.
- **Seguimiento de Hábitos & Rutinas Diarias**: Módulo de seguimiento de hábitos personales (agua, ejercicio, lectura, medicamentos, descanso) con contadores de rachas en días y barra de progreso de cumplimiento diario.
- **Matriz de Priorización Eisenhower 100% Local**: Organización inteligente de tareas en 4 cuadrantes (*Hazlo ya, Planificar, Rápido/Trámites, Pendientes*) y motor de análisis en español local sin requerir llamadas a APIs o IA externa.
- **Notificaciones Push Locales en el Dispositivo**: Programación de recordatorios con `AlarmManager`, `BroadcastReceiver` y `NotificationCompat` que funcionan sin conexión a Internet y con botón de prueba de notificación directa.
- **Garantía de Privacidad y Funcionamiento Offline**: Todos los datos se guardan localmente en la base de datos **Room (SQLite)** de Android. No requiere servidores ni transmite datos fuera del teléfono.
- **Protección por PIN de Seguridad**: Sistema de bloqueo numérico con PIN de 4 dígitos para resguardar el acceso a tu información personal.
- **Copia de Seguridad en JSON**: Exportación e importación rápida de copias de respaldo en almacenamiento local.

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **Interfaz de Usuario**: Jetpack Compose (Material Design 3)
- **Base de Datos Local**: Room Database (SQLite) + Kotlin Coroutines & Flow
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
