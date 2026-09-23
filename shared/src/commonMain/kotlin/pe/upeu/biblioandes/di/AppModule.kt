package pe.upeu.biblioandes.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.upeu.biblioandes.data.local.RelojSistema
import pe.upeu.biblioandes.data.repository.BibliotecaRepositoryFake
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.usecase.ObtenerCatalogoUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerEstudianteUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerLibroUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerPrestamosUseCase
import pe.upeu.biblioandes.domain.usecase.SolicitarPrestamoUseCase
import pe.upeu.biblioandes.domain.util.Reloj
import pe.upeu.biblioandes.presentation.catalogo.CatalogoViewModel
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroViewModel
import pe.upeu.biblioandes.presentation.inicio.InicioViewModel
import pe.upeu.biblioandes.presentation.perfil.PerfilViewModel
import pe.upeu.biblioandes.presentation.prestamos.PrestamosViewModel

val appModule: Module = module {
    // Único punto que conoce la fuente de datos: en la Unidad 2 se cambia solo esta línea.
    single<BibliotecaRepository> { BibliotecaRepositoryFake() }
    single<Reloj> { RelojSistema() }

    factory { ObtenerCatalogoUseCase(get()) }
    factory { ObtenerLibroUseCase(get()) }
    factory { ObtenerEstudianteUseCase(get()) }
    factory { ObtenerPrestamosUseCase(get(), get()) }
    factory { SolicitarPrestamoUseCase(get(), get()) }

    viewModel { CatalogoViewModel(get()) }
    viewModel { (libroId: Int) -> DetalleLibroViewModel(libroId, get(), get()) }
    viewModel { PrestamosViewModel(get()) }
    viewModel { InicioViewModel(get(), get()) }
    viewModel { PerfilViewModel(get()) }
}

fun initKoin() {
    startKoin { modules(appModule) }
}
