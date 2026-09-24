package pe.upeu.biblioandes.presentation.catalogo

import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.OrdenCatalogo

/** Controles de filtrado visibles mientras hay datos (con resultados o sin ellos). */
data class FiltrosCatalogo(
    val busqueda: String = "",
    val categoriaSeleccionada: String? = null,
    val soloDisponibles: Boolean = false,
    val orden: OrdenCatalogo = OrdenCatalogo.TITULO,
    val categorias: List<String> = emptyList()
)

sealed interface CatalogoUiState {
    data object Loading : CatalogoUiState
    data class Success(val libros: List<Libro>, val filtros: FiltrosCatalogo) : CatalogoUiState
    data class Empty(val filtros: FiltrosCatalogo) : CatalogoUiState
    data class Error(val mensaje: String) : CatalogoUiState
}
