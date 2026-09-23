package pe.upeu.biblioandes.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    onIrCatalogo: () -> Unit,
    onIrPrestamos: () -> Unit,
    onIrPerfil: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.cargar() }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("BiblioAndes", acciones = {
            IconButton(onClick = onIrPerfil) {
                Icon(Icons.Filled.Person, contentDescription = "Perfil")
            }
        })
        when (val s = estado) {
            InicioUiState.Loading -> PantallaCargando()
            is InicioUiState.Error -> PantallaError(s.mensaje, viewModel::cargar)
            is InicioUiState.Success -> Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Hola, ${s.estudiante.nombre}", style = MaterialTheme.typography.headlineMedium)
                TarjetaProximoVencimiento(s.proximoAVencer)
                Button(onClick = onIrCatalogo, modifier = Modifier.fillMaxWidth()) { Text("Ver catálogo") }
                OutlinedButton(onClick = onIrPrestamos, modifier = Modifier.fillMaxWidth()) { Text("Mis préstamos") }
            }
        }
    }
}

@Composable
private fun TarjetaProximoVencimiento(prestamo: Prestamo?) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Próxima devolución", style = MaterialTheme.typography.labelLarge)
            if (prestamo == null) {
                Text("No tienes préstamos activos.", style = MaterialTheme.typography.titleMedium)
            } else {
                Text(prestamo.libro.titulo, style = MaterialTheme.typography.titleLarge)
                Text("Vence el ${prestamo.fechaLimite}", style = MaterialTheme.typography.bodyMedium)
                val dias = (prestamo.estado as? EstadoPrestamo.Activo)?.diasRestantes
                if (dias != null) Text("Faltan $dias días", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
