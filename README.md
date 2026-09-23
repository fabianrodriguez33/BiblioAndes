# BiblioAndes

Aplicación móvil multiplataforma (Kotlin Multiplatform + Compose Multiplatform) para consultar el catálogo de la
biblioteca, solicitar préstamos y controlar fechas de devolución. Corre en **Android** e **iOS** desde el módulo
compartido `shared/commonMain`. Autor: Fabian Rodriguez (examen parcial Unidad 1, modalidad individual).

Los datos son simulados **en memoria** (sin red ni persistencia en disco). El backend llegará en la Unidad 2.

## Arquitectura: Clean Architecture + MVVM

```
shared/src/commonMain/kotlin/pe/upeu/biblioandes/
├── domain/          Reglas y contratos; no depende de data ni de presentation
│   ├── model/       Libro, Prestamo, EstadoPrestamo (sealed class), Estudiante
│   ├── repository/  BibliotecaRepository (interfaz)
│   └── usecase/     Casos de uso; aquí viven las reglas RN-01 a RN-04
├── data/
│   ├── local/       DatosSimulados (12 libros, 5 préstamos), RelojSistema
│   └── repository/  BibliotecaRepositoryFake (delay de 800 ms, bandera shouldSimulateError)
├── presentation/    Compose + ViewModels con StateFlow<UiState> de solo lectura
│   ├── inicio/ catalogo/ detalle/ prestamos/ perfil/
│   ├── navigation/  AppNavHost (aplica el tema en la raíz) y Destinos
│   ├── components/  Estados de carga, vacío y error; barra superior
│   └── theme/       Color, Type, BiblioAndesTheme (claro/oscuro)
└── di/              AppModule (Koin)
```

Flujo de datos: `DatosSimulados` → `BibliotecaRepositoryFake` → casos de uso → `ViewModel` (`UiState`) → `Screen`.

### Migración a la API real (Unidad 2)

Solo cambia la capa `data`: crear `data/remote/` (servicio + DTOs), `BibliotecaRepositoryImpl` y sustituir la
línea `single<BibliotecaRepository>` en `di/AppModule.kt`. `domain` y `presentation` no se modifican.

### Reglas de negocio (en `domain/usecase/`)

- **RN-01** máximo 3 préstamos activos · **RN-02** no se presta un libro sin ejemplares ·
  **RN-03** préstamo de 7 días; vencido si la fecha límite ya pasó · **RN-04** con un préstamo vencido no se solicita nada.

Nota: los datos de ejemplo incluyen un préstamo vencido, por lo que RN-04 rechaza toda solicitud nueva hasta que
ese dato cambie.

### Error simulado (solo Catálogo)

Poner `shouldSimulateError = true` en `BibliotecaRepositoryFake` para ver el estado de error con botón *Reintentar*.

## Ejecución

- Android: `./gradlew :androidApp:assembleDebug` (o ejecutar el módulo `androidApp` desde Android Studio).
- iOS: abrir [iosApp](./iosApp) en Xcode (macOS) y ejecutar en un simulador.
- Pruebas: `./gradlew :shared:testAndroidHostTest`

## Git

Ramas: `main` (estable), `develop` (integración), `feature/*-rodriguez`, `sc-<letra>-rodriguez`.
Commits convencionales (`feat`, `fix`, `refactor`, `style`, `docs`).
