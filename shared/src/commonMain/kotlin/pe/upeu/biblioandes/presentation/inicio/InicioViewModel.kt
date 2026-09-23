package pe.upeu.biblioandes.presentation.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Estudiante
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.usecase.ObtenerEstudianteUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerPrestamosUseCase

sealed interface InicioUiState {
    data object Loading : InicioUiState
    data class Success(val estudiante: Estudiante, val proximoAVencer: Prestamo?) : InicioUiState
    data class Error(val mensaje: String) : InicioUiState
}

class InicioViewModel(
    private val obtenerEstudiante: ObtenerEstudianteUseCase,
    private val obtenerPrestamos: ObtenerPrestamosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InicioUiState>(InicioUiState.Loading)
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    fun cargar() {
        _uiState.value = InicioUiState.Loading
        viewModelScope.launch {
            try {
                val estudiante = obtenerEstudiante()
                // Los préstamos llegan ordenados por fecha límite: el primer activo es el que vence antes.
                val proximo = obtenerPrestamos().firstOrNull { it.estado is EstadoPrestamo.Activo }
                _uiState.value = InicioUiState.Success(estudiante, proximo)
            } catch (e: Exception) {
                _uiState.value = InicioUiState.Error(e.message ?: "No se pudo cargar el inicio")
            }
        }
    }
}
