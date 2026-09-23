package pe.upeu.biblioandes.domain.util

import kotlinx.datetime.LocalDate

/** Fecha actual de evaluación; abstraída para poder probar las reglas de negocio. */
fun interface Reloj {
    fun hoy(): LocalDate
}
