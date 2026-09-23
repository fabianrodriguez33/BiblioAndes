package pe.upeu.biblioandes.presentation.prestamos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.presentation.components.BarraSuperior
import pe.upeu.biblioandes.presentation.components.PantallaCargando
import pe.upeu.biblioandes.presentation.components.PantallaError
import pe.upeu.biblioandes.presentation.components.PantallaVacia
import pe.upeu.biblioandes.presentation.theme.GrisDevuelto
import pe.upeu.biblioandes.presentation.theme.RojoVencido
import pe.upeu.biblioandes.presentation.theme.VerdeActivo

@Composable
fun PrestamosScreen(viewModel: PrestamosViewModel) {
    val estado by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.cargar() }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Mis préstamos")
        when (val s = estado) {
            PrestamosUiState.Loading -> PantallaCargando()
            is PrestamosUiState.Error -> PantallaError(s.mensaje, viewModel::cargar)
            is PrestamosUiState.Empty -> {
                Filtros(s.filtro, viewModel::onFiltroClick)
                PantallaVacia("No tienes préstamos con ese estado.")
            }
            is PrestamosUiState.Success -> {
                Filtros(s.filtro, viewModel::onFiltroClick)
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(s.prestamos, key = { it.id }) { PrestamoItem(it) }
                }
            }
        }
    }
}

@Composable
private fun Filtros(seleccionado: FiltroEstado?, onClick: (FiltroEstado) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FiltroEstado.entries) { filtro ->
            FilterChip(
                selected = filtro == seleccionado,
                onClick = { onClick(filtro) },
                label = { Text(filtro.etiqueta) }
            )
        }
    }
}

@Composable
private fun PrestamoItem(prestamo: Prestamo) {
    val (texto, color) = when (val e = prestamo.estado) {
        is EstadoPrestamo.Activo -> "Activo · vence en ${e.diasRestantes} días" to VerdeActivo
        is EstadoPrestamo.Devuelto -> "Devuelto el ${e.fechaDevolucion}" to GrisDevuelto
        is EstadoPrestamo.Vencido -> "Vencido · ${e.diasDeAtraso} días de atraso" to RojoVencido
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(prestamo.libro.titulo, style = MaterialTheme.typography.titleMedium)
            Text(prestamo.libro.autor, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Prestado: ${prestamo.fechaPrestamo}  ·  Límite: ${prestamo.fechaLimite}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(texto, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}
