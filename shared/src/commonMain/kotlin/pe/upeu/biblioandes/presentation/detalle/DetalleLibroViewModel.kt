package pe.upeu.biblioandes.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.usecase.ObtenerLibroUseCase
import pe.upeu.biblioandes.domain.usecase.ResultadoSolicitud
import pe.upeu.biblioandes.domain.usecase.SolicitarPrestamoUseCase

data class DetalleUiState(
    val cargando: Boolean = true,
    val libro: Libro? = null,
    val error: String? = null,
    val confirmando: Boolean = false,
    val procesando: Boolean = false,
    val limiteAlcanzado: Boolean = false,
    val mensaje: String? = null
)

class DetalleLibroViewModel(
    private val libroId: Int,
    private val obtenerLibro: ObtenerLibroUseCase,
    private val solicitarPrestamo: SolicitarPrestamoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val libro = obtenerLibro(libroId)
                val limite = solicitarPrestamo.limiteAlcanzado()
                _uiState.update {
                    it.copy(
                        cargando = false,
                        libro = libro,
                        limiteAlcanzado = limite,
                        error = if (libro == null) "No se encontró el libro." else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo cargar el libro.") }
            }
        }
    }

    fun onSolicitarClick() = _uiState.update { it.copy(confirmando = true) }

    fun onCancelarConfirmacion() = _uiState.update { it.copy(confirmando = false) }

    fun onConfirmarSolicitud() {
        _uiState.update { it.copy(confirmando = false, procesando = true) }
        viewModelScope.launch {
            val mensaje = when (val r = solicitarPrestamo(libroId)) {
                is ResultadoSolicitud.Exito -> "Préstamo registrado. Devuélvelo antes del ${r.prestamo.fechaLimite}."
                is ResultadoSolicitud.Rechazada -> r.motivo.mensaje
            }
            val libro = try { obtenerLibro(libroId) } catch (e: Exception) { _uiState.value.libro }
            val limite = try { solicitarPrestamo.limiteAlcanzado() } catch (e: Exception) { _uiState.value.limiteAlcanzado }
            _uiState.update {
                it.copy(procesando = false, libro = libro, limiteAlcanzado = limite, mensaje = mensaje)
            }
        }
    }

    fun onMensajeVisto() = _uiState.update { it.copy(mensaje = null) }
}
