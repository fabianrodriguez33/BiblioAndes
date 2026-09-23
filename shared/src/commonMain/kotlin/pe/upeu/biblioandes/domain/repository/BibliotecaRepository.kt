package pe.upeu.biblioandes.domain.repository

import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.Prestamo

interface BibliotecaRepository {
    suspend fun obtenerEstudiante(): Estudiante
    suspend fun obtenerCategorias(): List<String>
    suspend fun obtenerLibros(): List<Libro>
    suspend fun obtenerLibro(id: Int): Libro?
    suspend fun obtenerPrestamos(): List<Prestamo>
    suspend fun registrarPrestamo(libroId: Int, fechaPrestamo: String, fechaLimite: String): Prestamo
}
