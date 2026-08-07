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

### Método 2: Compilación de la APK en Android mediante Termux (Sin PC)

Este proyecto está configurado con **`compileSdk = 36`**, **`minSdk = 24`** y **Gradle KTS**, optimizado para poder ser compilado directamente en tu dispositivo móvil Android utilizando la aplicación **Termux**.

#### 📱 Guía Paso a Paso para Compilar con Termux:

##### 1. Preparar Termux en Android
* Descarga e instala **Termux** desde F-Droid (recomendado) o GitHub para asegurarte de tener la versión más reciente.
* Abre Termux y actualiza los paquetes básicos ejecutando:
  ```bash
  pkg update && pkg upgrade -y
  ```

##### 2. Instalar el entorno Java, Git y herramientas de desarrollo
Instala OpenJDK (Java 17 o 21), Git y Gradle en el entorno de Termux:
```bash
pkg install openjdk-21 git gradle android-tools -y
```

##### 3. Otorgar permisos de almacenamiento
Permite que Termux pueda acceder a las carpetas de tu teléfono para copiar el archivo APK una vez compilado:
```bash
termux-setup-storage
```

##### 4. Clonar el Repositorio e Ingresar al Proyecto
```bash
git clone https://github.com/tu-usuario/cordero-f-app.git
cd cordero-f-app
```

##### 5. Configurar la variable ANDROID_HOME (si es necesario)
Si utilizas el SDK de Android en Termux (mediante `commandlinetools-linux` o el paquete `android-sdk`):
```bash
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
```

##### 6. Compilar el archivo APK
Ejecuta la tarea de compilación con Gradle:
```bash
gradle assembleDebug
```
*Si tienes el script ejecutable `gradlew`, también puedes usar:*
```bash
chmod +x gradlew
./gradlew assembleDebug
```

##### 7. Copiar e Instalar la APK en tu Dispositivo
Una vez finalizada la compilación con éxito, copia la APK directamente a la carpeta de **Descargas (Download)** de tu teléfono:
```bash
cp app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/CorderoF.apk
```
Abre el gestor de archivos de tu teléfono, entra en la carpeta **Descargas**, pulsa sobre `CorderoF.apk` e instálala directamente en tu dispositivo.

---

### Método 3: Compilación Manual en PC (Línea de Comandos / Gradle)

Si has clonado o descargado este repositorio en tu ordenador local:

#### Requisitos Previos
- **JDK 17** o **21** instalado y configurado en tu `JAVA_HOME`.
- **Android SDK** (API Level 36).

#### Pasos para Compilar el APK:

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/cordero-f-app.git
cd cordero-f-app

# 2. Dar permisos de ejecución a gradlew
chmod +x gradlew

# 3. Compilar el APK de depuración
./gradlew assembleDebug
```

El ejecutable APK generado se ubicará en:
`app/build/outputs/apk/debug/app-debug.apk`

---

### Método 4: Abrir y Ejecutar en Android Studio

1. Abre **Android Studio** (versión Ladybug / Iguana o superior).
2. Selecciona **Open** y navega hasta la carpeta raíz del proyecto `cordero-f-app`.
3. Deja que Gradle sincronice las dependencias del proyecto (`libs.versions.toml`).
4. Conecta tu teléfono Android mediante depuración USB o inicia un Emulador Android.
5. Haz clic en el botón verde **Run 'app'** (`Shift + F10`).

---

## 📋 Licencia y Privacidad

Esta aplicación es software libre de código abierto. Tus datos personales pertenecen única y exclusivamente a ti.
