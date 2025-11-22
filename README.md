# Renova Móvil

Aplicación Android (Kotlin + Jetpack Compose) para fomentar el reciclaje mediante puntos, escaneo de códigos y flujo de comercio aliado.

## Tecnologías

- Kotlin, Jetpack Compose, Material 3
- Android Gradle Plugin (AGP), Gradle Wrapper
- API web (Laravel)
- PostgreSQL (backend)

## Características

- Registro y verificación de usuarios
- Escaneo de códigos y acumulación de puntos (ciudadano)
- Módulo de comercio: ventas, historial y estadísticas (negocio)
- Internacionalización: Español (México) y Inglés

## Requisitos

- Android Studio y SDK Android
- JDK compatible
- Dispositivo físico o emulador con ADB

## Configuración

1. Clona el repositorio:

```bash
git clone https://github.com/<usuario>/renova-movil.git
```

2. Abre el proyecto en Android Studio y sincroniza Gradle.
3. (Opcional) Configura FCM/mensajería push según tu entorno.

## Compilar y ejecutar (Windows)

- Compilar debug:

```powershell
.\gradlew.bat :app:assembleDebug
```

- Instalar en dispositivo:

```powershell
.\gradlew.bat :app:installDebug
```

- Limpiar build:

```powershell
.\gradlew.bat :app:clean
```

## Estructura

- `app/` módulo principal
- `app/src/main/java/` código Kotlin
- `app/src/main/res/` recursos (`values/`, `values-es-rMX/`, drawables)

## Internacionalización

- Textos en `values/strings.xml` (inglés) y `values-es-rMX/strings.xml` (es-MX)
- Gestión de idioma con `LocaleHelper` en `MainActivity`

## Notas de seguridad

- Capturas de pantalla bloqueadas únicamente en la pantalla de QR del ciudadano

## Comandos útiles (producción)

- `.\gradlew.bat :app:assembleRelease` compilar APK
- `.\gradlew.bat :app:bundleRelease` generar AAB

---

Para desarrollo, usa Android Studio (Run/Debug) y el Gradle Wrapper del proyecto.