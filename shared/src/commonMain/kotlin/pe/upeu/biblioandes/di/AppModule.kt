package pe.upeu.biblioandes.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
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

val appModule: Module = module {
    // Único punto que conoce la fuente de datos: en la Unidad 2 se cambia solo esta línea.
    single<BibliotecaRepository> { BibliotecaRepositoryFake() }
    single<Reloj> { RelojSistema() }

    factory { ObtenerCatalogoUseCase(get()) }
    factory { ObtenerLibroUseCase(get()) }
    factory { ObtenerEstudianteUseCase(get()) }
    factory { ObtenerPrestamosUseCase(get(), get()) }
    factory { SolicitarPrestamoUseCase(get(), get()) }
}

fun initKoin() {
    startKoin { modules(appModule) }
}
