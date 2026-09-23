package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository

class ObtenerLibroUseCase(private val repositorio: BibliotecaRepository) {
    suspend operator fun invoke(id: Int): Libro? = repositorio.obtenerLibro(id)
}
