package pe.upeu.biblioandes.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.presentation.components.BarraSuperior
import pe.upeu.biblioandes.presentation.components.PantallaCargando
import pe.upeu.biblioandes.presentation.components.PantallaError

@Composable
fun DetalleLibroScreen(viewModel: DetalleLibroViewModel, onAtras: () -> Unit) {
    val estado by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Detalle del libro", onAtras = onAtras)
        val libro = estado.libro
        when {
            estado.cargando && libro == null -> PantallaCargando()
            libro == null -> PantallaError(estado.error ?: "Error", viewModel::cargar)
            else -> FichaLibro(libro, estado.procesando, estado.limiteAlcanzado, viewModel::onSolicitarClick)
        }
    }

    if (estado.confirmando) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelarConfirmacion,
            title = { Text("Confirmar préstamo") },
            text = { Text("¿Deseas solicitar \"${estado.libro?.titulo}\" por 7 días?") },
            confirmButton = { TextButton(onClick = viewModel::onConfirmarSolicitud) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = viewModel::onCancelarConfirmacion) { Text("Cancelar") } }
        )
    }
    estado.mensaje?.let { mensaje ->
        AlertDialog(
            onDismissRequest = viewModel::onMensajeVisto,
            title = { Text("Solicitud de préstamo") },
            text = { Text(mensaje) },
            confirmButton = { TextButton(onClick = viewModel::onMensajeVisto) { Text("Aceptar") } }
        )
    }
}

@Composable
private fun FichaLibro(libro: Libro, procesando: Boolean, limiteAlcanzado: Boolean, onSolicitar: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(libro.titulo, style = MaterialTheme.typography.headlineMedium)
        Text(libro.autor, style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Dato("Año", libro.anio.toString())
                Dato("Categoría", libro.categoria)
                Dato("Sede", libro.sede)
                Dato("Ejemplares disponibles", libro.ejemplaresDisponibles.toString())
            }
        }
        Button(onClick = onSolicitar, enabled = !procesando && !limiteAlcanzado, modifier = Modifier.fillMaxWidth()) {
            Text(if (procesando) "Procesando..." else "Solicitar préstamo")
        }
        if (limiteAlcanzado) {
            Text(
                "Alcanzaste el límite de préstamos activos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.titleMedium)
    }
}
