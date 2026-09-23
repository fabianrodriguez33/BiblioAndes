package pe.upeu.biblioandes.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.util.Reloj

sealed class ResultadoSolicitud {
    data class Exito(val prestamo: Prestamo) : ResultadoSolicitud()
    data class Rechazada(val motivo: MotivoRechazo) : ResultadoSolicitud()
}

enum class MotivoRechazo(val mensaje: String) {
    LIBRO_NO_ENCONTRADO("El libro solicitado no existe."),
    MOROSIDAD("Tienes un préstamo vencido. Devuélvelo para solicitar nuevos libros."),
    LIMITE_ALCANZADO("Ya tienes 3 préstamos activos."),
    SIN_EJEMPLARES("No hay ejemplares disponibles de este libro.")
}

class SolicitarPrestamoUseCase(
    private val repositorio: BibliotecaRepository,
    private val reloj: Reloj
) {
    suspend operator fun invoke(libroId: Int): ResultadoSolicitud {
        val hoy = reloj.hoy()
        val libro = repositorio.obtenerLibro(libroId)
            ?: return ResultadoSolicitud.Rechazada(MotivoRechazo.LIBRO_NO_ENCONTRADO)
        val prestamos = repositorio.obtenerPrestamos().map { it.conEstadoEvaluado(hoy) }

        // RN-04: morosidad bloquea cualquier solicitud nueva.
        if (prestamos.any { it.estado is EstadoPrestamo.Vencido }) {
            return ResultadoSolicitud.Rechazada(MotivoRechazo.MOROSIDAD)
        }
        // RN-01: máximo tres préstamos activos simultáneos.
        if (prestamos.count { it.estado is EstadoPrestamo.Activo } >= LIMITE_PRESTAMOS_ACTIVOS) {
            return ResultadoSolicitud.Rechazada(MotivoRechazo.LIMITE_ALCANZADO)
        }
        // RN-02: no se puede solicitar un libro sin ejemplares.
        if (libro.ejemplaresDisponibles == 0) {
            return ResultadoSolicitud.Rechazada(MotivoRechazo.SIN_EJEMPLARES)
        }
        // RN-03: duración fija de 7 días.
        val limite = hoy.plus(DatePeriod(days = DURACION_PRESTAMO_DIAS))
        return ResultadoSolicitud.Exito(
            repositorio.registrarPrestamo(libro.id, hoy.toString(), limite.toString())
        )
    }

    companion object {
        const val LIMITE_PRESTAMOS_ACTIVOS = 3
        const val DURACION_PRESTAMO_DIAS = 7
    }
}
