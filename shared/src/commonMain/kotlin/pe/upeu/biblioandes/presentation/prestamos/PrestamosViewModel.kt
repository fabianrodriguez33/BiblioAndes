package pe.upeu.biblioandes.presentation.prestamos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.usecase.ObtenerPrestamosUseCase

enum class FiltroEstado(val etiqueta: String) {
    ACTIVO("Activo"),
    DEVUELTO("Devuelto"),
    VENCIDO("Vencido");

    fun acepta(estado: EstadoPrestamo): Boolean = when (this) {
        ACTIVO -> estado is EstadoPrestamo.Activo
        DEVUELTO -> estado is EstadoPrestamo.Devuelto
        VENCIDO -> estado is EstadoPrestamo.Vencido
    }
}

sealed interface PrestamosUiState {
    data object Loading : PrestamosUiState
    data class Success(val prestamos: List<Prestamo>, val filtro: FiltroEstado?) : PrestamosUiState
    data class Empty(val filtro: FiltroEstado?) : PrestamosUiState
    data class Error(val mensaje: String) : PrestamosUiState
}

class PrestamosViewModel(private val obtenerPrestamos: ObtenerPrestamosUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<PrestamosUiState>(PrestamosUiState.Loading)
    val uiState: StateFlow<PrestamosUiState> = _uiState.asStateFlow()

    private val _prestamosActivos = MutableStateFlow(0)
    val prestamosActivos: StateFlow<Int> = _prestamosActivos.asStateFlow()

    private var prestamos: List<Prestamo> = emptyList()
    private var filtro: FiltroEstado? = null

    fun cargar() {
        _uiState.value = PrestamosUiState.Loading
        viewModelScope.launch {
            try {
                prestamos = obtenerPrestamos()
                publicar()
            } catch (e: Exception) {
                _uiState.value = PrestamosUiState.Error(e.message ?: "No se pudieron cargar los préstamos")
            }
        }
    }

    /** Actualiza solo el contador del badge, sin pasar por el estado de carga. */
    fun actualizarActivos() {
        viewModelScope.launch {
            try {
                _prestamosActivos.value = obtenerPrestamos.contarActivos()
            } catch (e: Exception) {
                // Si falla, se conserva el último valor conocido.
            }
        }
    }

    fun onFiltroClick(nuevo: FiltroEstado) {
        filtro = if (filtro == nuevo) null else nuevo
        publicar()
    }

    private fun publicar() {
        val visibles = filtro?.let { f -> prestamos.filter { f.acepta(it.estado) } } ?: prestamos
        _uiState.value =
            if (visibles.isEmpty()) PrestamosUiState.Empty(filtro)
            else PrestamosUiState.Success(visibles, filtro)
    }
}
