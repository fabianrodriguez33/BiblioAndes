package pe.upeu.biblioandes.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
}

fun initKoin() {
    startKoin { modules(appModule) }
}
