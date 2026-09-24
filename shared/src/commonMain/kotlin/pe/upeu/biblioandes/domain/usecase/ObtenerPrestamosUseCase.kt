package pe.upeu.biblioandes.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.util.Reloj

/** RN-03: un préstamo no devuelto cuya fecha límite ya pasó se evalúa como Vencido. */
fun Prestamo.conEstadoEvaluado(hoy: LocalDate): Prestamo {
    if (estado is EstadoPrestamo.Devuelto) return this
    val dias = hoy.daysUntil(LocalDate.parse(fechaLimite))
    return copy(
        estado = if (dias < 0) EstadoPrestamo.Vencido(-dias) else EstadoPrestamo.Activo(dias)
    )
}

class ObtenerPrestamosUseCase(
    private val repositorio: BibliotecaRepository,
    private val reloj: Reloj
) {
    /** RF-04: préstamos con estado evaluado, ordenados por fecha límite más próxima. */
    suspend operator fun invoke(): List<Prestamo> {
        val hoy = reloj.hoy()
        return repositorio.obtenerPrestamos()
            .map { it.conEstadoEvaluado(hoy) }
            .sortedBy { it.fechaLimite }
    }

    /** Cantidad de préstamos en estado Activo, con RN-03 ya aplicada. */
    suspend fun contarActivos(): Int = invoke().count { it.estado is EstadoPrestamo.Activo }
}
