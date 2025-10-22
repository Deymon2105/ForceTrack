# ForceTrack - Aplicación de Seguimiento de Entrenamiento

## Estructura del Proyecto (SQLite con Room)

Este proyecto usa SQLite con Room para almacenar todos los datos de forma local en el dispositivo.

### 📁 Base de Datos

**Ubicación:** `database/`

#### Entidades (Tablas)
- `UsuarioEntity` - Tabla de usuarios
- `BloqueEntity` - Bloques de entrenamiento
- `SemanaEntity` - Semanas dentro de cada bloque
- `DiaEntity` - Días de entrenamiento
- `EjercicioEntity` - Ejercicios de cada día
- `SerieEntity` - Series de cada ejercicio

#### DAOs (Acceso a Datos)
Cada entidad tiene su propio DAO que maneja las operaciones de base de datos:
- `UsuarioDao` - Operaciones con usuarios
- `BloqueDao` - Operaciones con bloques
- `SemanaDao` - Operaciones con semanas
- `DiaDao` - Operaciones con días
- `EjercicioDao` - Operaciones con ejercicios
- `SerieDao` - Operaciones con series

#### Repositorio
`ForceTrackRepository` - Maneja todas las operaciones de la base de datos de forma centralizada

### 📦 Modelos

**Ubicación:** `model/`

Modelos simples para usar en la interfaz de usuario:
- `Usuario` - Información del usuario
- `BloqueEntrenamiento` - Bloque con semanas
- `SemanaEntrenamiento` - Semana con días
- `DiaRutina` - Día con ejercicios
- `EjercicioRutina` - Ejercicio con series
- `Serie` - Serie con peso, repeticiones y RIR

### 🎨 Interfaz de Usuario

**Ubicación:** `ui/`

- `LoginScreen` - Pantalla de inicio de sesión
- `RegistroScreen` - Pantalla de registro
- `BloquesScreen` - Lista de bloques de entrenamiento
- `SplitSemanalScreen` - Vista semanal del bloque
- `RutinaDiariaScreen` - Rutina diaria con ejercicios y series
- `EjerciciosScreen` - Selector de ejercicios disponibles
- `LoadingScreen` - Pantalla de carga

### 🧠 ViewModels

**Ubicación:** `viewmodel/`

- `AuthViewModel` - Maneja autenticación (login/registro)
- `BloquesViewModel` - Maneja bloques de entrenamiento
- `RutinaViewModel` - Maneja ejercicios y series del día

### 🎨 Tema

**Ubicación:** `ui/theme/`

Colores personalizados:
- Fondo: #0d1016
- Botones: #28C76F
- Texto y títulos: #c0c2c4
- Texto de inputs: #3a97c0
- Inputs: #161b24

## 🚀 Características

1. **Autenticación Simple** - Login y registro con SQLite
2. **Bloques de Entrenamiento** - Crea bloques personalizados
3. **Gestión de Semanas** - Organiza tu entrenamiento por semanas
4. **Rutinas Diarias** - Agrega ejercicios a cada día
5. **Series Dinámicas** - Agrega/elimina series en tiempo real
6. **Notas** - Guarda notas de cada sesión
7. **Persistencia** - Todos los datos se guardan localmente

## 📝 Código Simple y Autoexplicativo

El código está diseñado para ser fácil de entender:
- Nombres descriptivos en español
- Comentarios claros
- Funciones simples y directas
- Sin complejidad innecesaria

## 🔧 Dependencias

- Room (SQLite) - Base de datos local
- Jetpack Compose - Interfaz de usuario moderna
- Coroutines - Operaciones asíncronas
- Material 3 - Diseño moderno

## 💡 Flujo de la Aplicación

1. Usuario se registra o inicia sesión
2. Ve su lista de bloques (vacía al inicio)
3. Crea un nuevo bloque con semanas
4. Selecciona un día para entrenar
5. Agrega ejercicios con series
6. Registra peso, repeticiones y RIR
7. Guarda notas de la sesión
8. Todos los datos se guardan automáticamente en SQLite

