package pe.upeu.biblioandes.presentation.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.usecase.ObtenerCatalogoUseCase

class CatalogoViewModel(private val obtenerCatalogo: ObtenerCatalogoUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogoUiState>(CatalogoUiState.Loading)
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    private var libros: List<Libro> = emptyList()
    private var filtros = FiltrosCatalogo()

    fun cargar() {
        _uiState.value = CatalogoUiState.Loading
        viewModelScope.launch {
            try {
                val catalogo = obtenerCatalogo()
                libros = catalogo.libros
                filtros = filtros.copy(categorias = catalogo.categorias)
                publicar()
            } catch (e: Exception) {
                _uiState.value = CatalogoUiState.Error(e.message ?: "No se pudo cargar el catálogo")
            }
        }
    }

    fun onBusquedaChange(texto: String) {
        filtros = filtros.copy(busqueda = texto)
        publicar()
    }

    fun onCategoriaClick(categoria: String) {
        filtros = filtros.copy(
            categoriaSeleccionada = if (filtros.categoriaSeleccionada == categoria) null else categoria
        )
        publicar()
    }

    private fun publicar() {
        val resultado = obtenerCatalogo.filtrar(libros, filtros.busqueda, filtros.categoriaSeleccionada)
        _uiState.value =
            if (resultado.isEmpty()) CatalogoUiState.Empty(filtros)
            else CatalogoUiState.Success(resultado, filtros)
    }
}
