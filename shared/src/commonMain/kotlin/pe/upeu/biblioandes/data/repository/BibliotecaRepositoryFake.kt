package pe.upeu.biblioandes.data.repository

import kotlinx.coroutines.delay
import pe.upeu.biblioandes.data.local.DatosSimulados
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository

/** Implementación en memoria con latencia simulada; en la Unidad 2 se reemplaza por una versión remota. */
class BibliotecaRepositoryFake : BibliotecaRepository {

    /** Bandera para demostrar el estado de error del catálogo. */
    var shouldSimulateError: Boolean = false

    private val libros = DatosSimulados.libros.toMutableList()
    private val prestamos = DatosSimulados.prestamos.toMutableList()

    override suspend fun obtenerEstudiante(): Estudiante = DatosSimulados.estudiante

    override suspend fun obtenerCategorias(): List<String> = DatosSimulados.categorias

    override suspend fun obtenerLibros(): List<Libro> {
        delay(LATENCIA_MS)
        if (shouldSimulateError) throw IllegalStateException("Error simulado al cargar el catálogo")
        return libros.toList()
    }

    override suspend fun obtenerLibro(id: Int): Libro? {
        delay(LATENCIA_MS)
        return libros.firstOrNull { it.id == id }
    }

    override suspend fun obtenerPrestamos(): List<Prestamo> {
        delay(LATENCIA_MS)
        return prestamos.toList()
    }

    override suspend fun registrarPrestamo(
        libroId: Int,
        fechaPrestamo: String,
        fechaLimite: String
    ): Prestamo {
        val indice = libros.indexOfFirst { it.id == libroId }
        val actualizado = libros[indice].let { it.copy(ejemplaresDisponibles = it.ejemplaresDisponibles - 1) }
        libros[indice] = actualizado
        val nuevo = Prestamo(
            id = (prestamos.maxOfOrNull { it.id } ?: 0) + 1,
            libro = actualizado,
            fechaPrestamo = fechaPrestamo,
            fechaLimite = fechaLimite,
            estado = EstadoPrestamo.Activo(diasRestantes = 7)
        )
        prestamos.add(nuevo)
        return nuevo
    }

    private companion object {
        const val LATENCIA_MS = 800L
    }
}
