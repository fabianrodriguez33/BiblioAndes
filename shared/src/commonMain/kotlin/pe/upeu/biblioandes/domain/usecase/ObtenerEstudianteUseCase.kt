package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository

class ObtenerEstudianteUseCase(private val repositorio: BibliotecaRepository) {
    suspend operator fun invoke(): Estudiante = repositorio.obtenerEstudiante()
}
