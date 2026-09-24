# BiblioAndes

Aplicación móvil multiplataforma para la biblioteca de un instituto tecnológico con tres sedes (Central, Sede Norte y
Sede Sur). Permite consultar el catálogo, solicitar préstamos y controlar las fechas de devolución.

Está construida con **Kotlin Multiplatform** y **Compose Multiplatform**: una sola base de código en
`shared/commonMain` que se ejecuta de forma nativa en **Android** e **iOS**.

- **Autor:** Fabian Rodriguez
- **Contexto:** examen parcial de la Unidad 1 (modalidad individual)
- **Estado de los datos:** simulados **en memoria**. No hay red ni persistencia en disco; el servicio web se
  desarrollará en la Unidad 2.

---

## Tabla de contenidos

1. [Funcionalidades](#funcionalidades)
2. [Arquitectura](#arquitectura)
3. [Estructura de paquetes](#estructura-de-paquetes)
4. [Reglas de negocio](#reglas-de-negocio)
5. [Datos simulados](#datos-simulados)
6. [Solicitudes de cambio (SC-A a SC-D)](#solicitudes-de-cambio-sc-a-a-sc-d)
7. [Pruebas](#pruebas)
8. [Ejecución](#ejecución)
9. [Migración a la API real (Unidad 2)](#migración-a-la-api-real-unidad-2)
10. [Flujo de Git](#flujo-de-git)

---

## Funcionalidades

| Requerimiento | Pantalla | Descripción |
|---|---|---|
| RF-01 | Inicio | Saludo con el nombre del estudiante, tarjeta con el préstamo activo que vence primero y accesos rápidos al catálogo y a "Mis préstamos". |
| RF-02 | Catálogo | Lista de libros con título, autor, editorial y disponibilidad, filtrable por categoría (Programación, Matemática, Redes, Gestión, Literatura). |
| RF-03 | Detalle | Ficha completa del libro y botón "Solicitar préstamo" con diálogo de confirmación. |
| RF-04 | Mis préstamos | Préstamos ordenados por fecha de devolución más próxima, filtrables por estado (Activo, Devuelto, Vencido). |
| RF-05 | Catálogo | Búsqueda en tiempo real por título o autor, sin distinguir mayúsculas, minúsculas ni tildes. |
| RF-06 | Perfil | Ficha del estudiante e interruptor de tema claro/oscuro que se aplica de inmediato a toda la app. |
| RF-07 | Navegación | Barra inferior con Inicio, Catálogo y Préstamos; navegación jerárquica al detalle y soporte del botón atrás. El perfil se abre desde el ícono de la pantalla de inicio. |

Todas las pantallas que cargan datos manejan los estados **Cargando** (con el retardo simulado de 800 ms),
**Contenido**, **Vacío** y **Error** (con botón "Reintentar").

---

## Arquitectura

**Clean Architecture + MVVM**, en tres capas con dependencias en un solo sentido:

```
presentation  ──▶  domain  ◀──  data
   (UI, ViewModels)   (reglas, contratos)   (fuentes de datos)
```

- **`domain`** no depende de ninguna otra capa. Contiene los modelos, la interfaz del repositorio y los casos de uso,
  donde viven todas las reglas de negocio.
- **`data`** implementa la interfaz del repositorio. Hoy usa datos en memoria.
- **`presentation`** contiene composables y ViewModels. Los ViewModels exponen únicamente `StateFlow<UiState>` de solo
  lectura (`asStateFlow()`); la interfaz nunca contiene reglas de negocio.
- **`di`** ensambla todo con **Koin** y es el único lugar que conoce la implementación concreta del repositorio.

Recorrido de un libro hasta la pantalla:

`DatosSimulados` → `BibliotecaRepositoryFake` (aplica `delay(800)`) → `ObtenerCatalogoUseCase` →
`CatalogoViewModel` (emite `CatalogoUiState`) → `CatalogoScreen` (`LazyColumn` con `LibroCardItem`).

### Decisiones de diseño

- **`EstadoPrestamo` es una `sealed class`** (`Activo`, `Devuelto`, `Vencido`) porque cada estado lleva datos
  distintos (`diasRestantes`, `fechaDevolucion`, `diasDeAtraso`) sin necesidad de campos nulables.
- **El reloj está abstraído** (`Reloj`) para que las reglas que dependen de la fecha sean comprobables.
- **El tema oscuro** lo administra `PerfilViewModel` y se aplica en la raíz del árbol, en `AppNavHost`.
- **El ViewModel de préstamos** se crea en la raíz de la navegación para que el badge de la barra inferior sea visible
  desde cualquier pestaña.

### Tecnologías

Kotlin 2.4 · Compose Multiplatform 1.12 · Material 3 · Navigation Compose · Koin · kotlinx-coroutines ·
kotlinx-datetime. No se usa ninguna librería de red ni de persistencia (Ktor, Retrofit, Room, SQLDelight).

---

## Estructura de paquetes

```
shared/src/commonMain/kotlin/pe/upeu/biblioandes/
├── App.kt
├── domain/
│   ├── model/
│   │   ├── Libro.kt
│   │   ├── Prestamo.kt
│   │   ├── EstadoPrestamo.kt
│   │   ├── Estudiante.kt
│   │   └── OrdenCatalogo.kt
│   ├── repository/
│   │   └── BibliotecaRepository.kt
│   ├── usecase/
│   │   ├── ObtenerCatalogoUseCase.kt
│   │   ├── ObtenerLibroUseCase.kt
│   │   ├── ObtenerEstudianteUseCase.kt
│   │   ├── ObtenerPrestamosUseCase.kt
│   │   └── SolicitarPrestamoUseCase.kt
│   └── util/
│       └── Reloj.kt
├── data/
│   ├── local/
│   │   ├── DatosSimulados.kt
│   │   └── RelojSistema.kt
│   └── repository/
│       └── BibliotecaRepositoryFake.kt
├── presentation/
│   ├── inicio/        InicioViewModel, InicioScreen
│   ├── catalogo/      CatalogoViewModel, CatalogoUiState, CatalogoScreen, components/LibroCardItem
│   ├── detalle/       DetalleLibroViewModel, DetalleLibroScreen
│   ├── prestamos/     PrestamosViewModel, PrestamosScreen
│   ├── perfil/        PerfilViewModel, PerfilScreen
│   ├── navigation/    AppNavHost, Destinos
│   ├── components/    Estados (carga, vacío, error) y barra superior
│   └── theme/         Color, Type, BiblioAndesTheme
└── di/
    └── AppModule.kt
```

Otros módulos: `androidApp/` (actividad y aplicación Android, que inicia Koin) e `iosApp/` (proyecto de Xcode, que inicia
Koin al arrancar).

---

## Reglas de negocio

Viven **exclusivamente** en `domain/usecase/`; ni los composables ni los ViewModels las duplican.

| Regla | Descripción | Dónde |
|---|---|---|
| RN-01 | Un estudiante no puede tener más de 3 préstamos activos a la vez. | `SolicitarPrestamoUseCase` |
| RN-02 | No se puede solicitar un libro sin ejemplares disponibles. | `SolicitarPrestamoUseCase` |
| RN-03 | Todo préstamo dura 7 días; si la fecha límite ya pasó y no fue devuelto, se evalúa como `Vencido`. | `ObtenerPrestamosUseCase` y `SolicitarPrestamoUseCase` |
| RN-04 | Con al menos un préstamo vencido no se puede solicitar ningún libro nuevo. | `SolicitarPrestamoUseCase` |

El orden de evaluación al solicitar es RN-04, RN-01 y RN-02. El resultado es un `ResultadoSolicitud`
(`Exito` o `Rechazada` con su `MotivoRechazo`).

> Los datos de ejemplo incluyen un préstamo vencido, por lo que RN-04 rechaza toda solicitud nueva hasta que ese dato
> cambie. Los casos de éxito se verifican en las pruebas con repositorios de prueba.

---

## Datos simulados

`data/local/DatosSimulados.kt` contiene el estudiante (Fabian Rodriguez, código E-2291), las cinco categorías,
**12 libros** (dos agotados) y **5 préstamos** (2 activos, 2 devueltos y 1 vencido).
`BibliotecaRepositoryFake` mantiene copias mutables en memoria: al registrar un préstamo descuenta un ejemplar.

**Simular un error de carga:** poner `shouldSimulateError = true` en `BibliotecaRepositoryFake` para ver el estado de
error del catálogo con su botón "Reintentar".

---

## Solicitudes de cambio (SC-A a SC-D)

Cada una se desarrolló en su rama `sc-<letra>-rodriguez`, en tres commits (estructura, conexión con la interfaz y
limpieza con verificación de casos límite).

| SC | Cambio | Dónde reside la lógica |
|---|---|---|
| **SC-A** | Chip **"Solo disponibles"** en el catálogo que oculta libros con 0 ejemplares y se combina con la categoría y la búsqueda. | `ObtenerCatalogoUseCase.filtrar`; estado en `FiltrosCatalogo.soloDisponibles` |
| **SC-B** | **Badge** con el número de préstamos activos en la barra inferior y botón "Solicitar préstamo" deshabilitado al llegar al límite (RN-01). | `ObtenerPrestamosUseCase.contarActivos()` y `SolicitarPrestamoUseCase.limiteAlcanzado()`; publicado por `PrestamosViewModel` y `DetalleUiState` |
| **SC-C** | **Selector de orden** por Título (A→Z, sin tildes) o Año (más reciente primero), conservando el filtro de categoría. Por defecto, Título. | `ObtenerCatalogoUseCase.filtrar`; estado en `FiltrosCatalogo.orden` |
| **SC-D** | Campo **`editorial`** en el modelo `Libro`, con datos semilla, mostrado bajo el autor en catálogo y detalle. | Atraviesa `domain/model`, `data/local` y `presentation` |

---

## Pruebas

Las reglas de negocio y los filtros están cubiertos en `shared/src/commonTest/.../ReglasNegocioTest.kt`
(18 pruebas): RN-01 a RN-04, búsqueda sin tildes, "Solo disponibles", orden por título y por año, conteo de
activos y límite, y la editorial en libros y préstamos.

```bash
./gradlew :shared:testAndroidHostTest
```

---

## Ejecución

**Requisitos:** JDK 17 o superior, Android Studio con SDK de Android y, para iOS, macOS con Xcode.

- **Android** (compilar el APK de depuración):

  ```bash
  ./gradlew :androidApp:assembleDebug
  ```

  También se puede ejecutar el módulo `androidApp` desde Android Studio en un emulador o dispositivo.

- **iOS:** abrir la carpeta [iosApp](./iosApp) en Xcode (macOS) y ejecutar en un simulador.

---

## Migración a la API real (Unidad 2)

La arquitectura está pensada para que reemplazar la fuente simulada sea un cambio localizado en `data`:

1. Crear `data/remote/BibliotecaApiService.kt` y los DTOs en `data/remote/dto/`.
2. Implementar `data/repository/BibliotecaRepositoryImpl.kt` contra la interfaz `BibliotecaRepository`.
3. Cambiar el registro `single<BibliotecaRepository>` en `di/AppModule.kt`.

`domain` y `presentation` no se modifican.

---

## Flujo de Git

- **`main`:** código estable y presentable; sin commits directos, solo merges.
- **`develop`:** rama de integración.
- **`feature/<funcionalidad>-rodriguez`:** ramas de la Parte I (`setup`, `domain`, `data`, `ui`).
- **`sc-<letra>-rodriguez`:** ramas de la Parte II (SC-A a SC-D).
- **Commits convencionales:** `feat`, `fix`, `refactor`, `style` y `docs`.
- **Entrega:** tag `v1.0-unidad1` sobre `main`.
