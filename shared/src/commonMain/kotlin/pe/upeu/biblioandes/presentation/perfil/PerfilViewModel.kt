package pe.upeu.biblioandes.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.usecase.ObtenerEstudianteUseCase

class PerfilViewModel(private val obtenerEstudiante: ObtenerEstudianteUseCase) : ViewModel() {

    private val _estudiante = MutableStateFlow<Estudiante?>(null)
    val estudiante: StateFlow<Estudiante?> = _estudiante.asStateFlow()

    private val _temaOscuro = MutableStateFlow(false)
    val temaOscuro: StateFlow<Boolean> = _temaOscuro.asStateFlow()

    init {
        viewModelScope.launch { _estudiante.value = obtenerEstudiante() }
    }

    fun onTemaOscuroChange(oscuro: Boolean) {
        _temaOscuro.value = oscuro
    }
}
