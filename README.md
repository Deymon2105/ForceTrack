# ForceTrack - Aplicación de Seguimiento de Entrenamientos

Aplicación Android para gestionar y registrar entrenamientos de gimnasio de forma sencilla y efectiva.

## Características

- **Gestión de Bloques**: Organiza tus entrenamientos en bloques personalizados
- **Splits Semanales**: Crea splits de entrenamiento por semanas
- **Rutinas Diarias**: Programa ejercicios específicos para cada día
- **Registro de Series**: Guarda peso, repeticiones y RIR para cada serie
- **Calendario de Entrenamientos**: Visualiza tu progreso con un calendario interactivo
- **Seguimiento de Rachas**: Mantén la motivación con el contador de rachas
- **Modo Offline**: Todos tus datos se guardan localmente
- **Sincronización con Xano**: Backend en la nube para respaldo de datos

## Tecnologías

- **Kotlin** - Lenguaje de programación
- **Jetpack Compose** - UI moderna y declarativa
- **Room Database** - Persistencia local de datos
- **Retrofit** - Cliente HTTP para API REST
- **Xano** - Backend como servicio
- **Coroutines & Flow** - Programación asíncrona
- **Material Design 3** - Diseño moderno

## Arquitectura

```
app/
├── config/          # Configuración (API, Rutas)
├── database/        # Room (Entities, DAOs, Repository)
├── model/           # Modelos de dominio
├── network/         # Retrofit, DTOs, RequestQueue
├── ui/              # Pantallas Compose
├── utils/           # Utilidades
└── viewmodel/       # ViewModels (MVVM)
```

## Seguridad

- RequestQueue con rate limiting (2 req/seg, 800ms delay)
- Reintentos automáticos con exponential backoff
- Configuración de API en módulo separado
- Ofuscación de código con R8/ProGuard

## Requisitos

- Android 7.0 (API 24) o superior
- Android Studio Hedgehog o superior
- JDK 17
- Gradle 8.x

## Instalación

1. Clona el repositorio:
```bash
git clone https://github.com/tuusuario/forcetrack.git
```

2. Se debe abrir el proyecto en Android Studio

3. Debes sincronizar las dependencias de Gradle

4. Ejecutr la aplicación en un emulador o dispositivo físico

## Configuración

La configuración de la API de Xano está en:
```
app/src/main/java/com/example/forcetrack/config/ApiConfig.kt
```

Para cambiar las URLs de los endpoints, modifica los valores en `ApiConfig`.

## Compilar APK Firmado

1. En Android Studio: **Build** → **Generate Signed Bundle / APK**
2. Selecciona **Android App Bundle**
3. Crea o selecciona un keystore
4. El archivo se generará en `app/release/`

## Autores

Tu Nombre - [@juatapiaoing - @Deymon2105](https://github.com/juatapiaoing - https://github.com/Deymon2105)

## Agradecimientos

- Material Design por los iconos
- Jetpack Compose por facilitar el desarrollo UI
- Xano por el backend

---

**Versión actual:** 1.0 (versionCode: 1)
**Última actualización:** 25 - Noviembre - 2025

