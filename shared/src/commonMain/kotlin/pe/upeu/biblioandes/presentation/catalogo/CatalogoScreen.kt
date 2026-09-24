package pe.upeu.biblioandes.presentation.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.biblioandes.domain.model.OrdenCatalogo
import pe.upeu.biblioandes.presentation.catalogo.components.LibroCardItem
import pe.upeu.biblioandes.presentation.components.BarraSuperior
import pe.upeu.biblioandes.presentation.components.PantallaCargando
import pe.upeu.biblioandes.presentation.components.PantallaError
import pe.upeu.biblioandes.presentation.components.PantallaVacia

@Composable
fun CatalogoScreen(viewModel: CatalogoViewModel, onLibroClick: (Int) -> Unit) {
    val estado by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.cargar() }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Catálogo")
        when (val s = estado) {
            CatalogoUiState.Loading -> PantallaCargando()
            is CatalogoUiState.Error -> PantallaError(s.mensaje, viewModel::cargar)
            is CatalogoUiState.Empty -> {
                Filtros(
                    filtros = s.filtros,
                    onBusquedaChange = viewModel::onBusquedaChange,
                    onCategoriaClick = viewModel::onCategoriaClick,
                    onSoloDisponiblesClick = viewModel::onSoloDisponiblesClick,
                    onOrdenChange = viewModel::onOrdenChange
                )
                PantallaVacia("No se encontraron libros con esos filtros.")
            }
            is CatalogoUiState.Success -> {
                Filtros(
                    filtros = s.filtros,
                    onBusquedaChange = viewModel::onBusquedaChange,
                    onCategoriaClick = viewModel::onCategoriaClick,
                    onSoloDisponiblesClick = viewModel::onSoloDisponiblesClick,
                    onOrdenChange = viewModel::onOrdenChange
                )
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(s.libros, key = { it.id }) { libro ->
                        LibroCardItem(libro, onClick = { onLibroClick(libro.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun Filtros(
    filtros: FiltrosCatalogo,
    onBusquedaChange: (String) -> Unit,
    onCategoriaClick: (String) -> Unit,
    onSoloDisponiblesClick: () -> Unit,
    onOrdenChange: (OrdenCatalogo) -> Unit
) {
    OutlinedTextField(
        value = filtros.busqueda,
        onValueChange = onBusquedaChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Buscar por título o autor") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (filtros.busqueda.isNotEmpty()) {
                IconButton(onClick = { onBusquedaChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        singleLine = true
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = filtros.soloDisponibles,
                onClick = onSoloDisponiblesClick,
                label = { Text("Solo disponibles") }
            )
        }
        items(filtros.categorias) { categoria ->
            FilterChip(
                selected = categoria == filtros.categoriaSeleccionada,
                onClick = { onCategoriaClick(categoria) },
                label = { Text(categoria) }
            )
        }
    }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Ordenar por:", style = MaterialTheme.typography.labelLarge)
        OrdenCatalogo.entries.forEach { orden ->
            FilterChip(
                selected = orden == filtros.orden,
                onClick = { onOrdenChange(orden) },
                label = { Text(orden.etiqueta) }
            )
        }
    }
}
