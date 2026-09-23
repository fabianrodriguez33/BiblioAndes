package pe.upeu.biblioandes.data.local

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import pe.upeu.biblioandes.domain.util.Reloj

class RelojSistema : Reloj {
    override fun hoy(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
