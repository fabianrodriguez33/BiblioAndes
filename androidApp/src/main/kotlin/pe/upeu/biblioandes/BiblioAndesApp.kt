package pe.upeu.biblioandes

import android.app.Application
import pe.upeu.biblioandes.di.initKoin

class BiblioAndesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
