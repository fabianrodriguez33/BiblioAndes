package pe.upeu.biblioandes.presentation.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.biblioandes.presentation.components.BarraSuperior
import pe.upeu.biblioandes.presentation.components.PantallaCargando

@Composable
fun PerfilScreen(viewModel: PerfilViewModel, onAtras: () -> Unit) {
    val estudiante by viewModel.estudiante.collectAsState()
    val oscuro by viewModel.temaOscuro.collectAsState()

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Perfil y ajustes", onAtras = onAtras)
        val e = estudiante
        if (e == null) {
            PantallaCargando()
        } else {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(e.nombre, style = MaterialTheme.typography.titleLarge)
                        Text("Código: ${e.codigo}", style = MaterialTheme.typography.bodyMedium)
                        Text(e.carrera, style = MaterialTheme.typography.bodyMedium)
                        Text(e.correo, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tema oscuro", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = oscuro, onCheckedChange = viewModel::onTemaOscuroChange)
                }
            }
        }
    }
}
