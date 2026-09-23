package pe.upeu.biblioandes.presentation.catalogo.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.presentation.theme.RojoVencido
import pe.upeu.biblioandes.presentation.theme.VerdeActivo

/** Tarjeta reutilizable: solo conoce un [Libro] y la acción de toque. */
@Composable
fun LibroCardItem(libro: Libro, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(libro.titulo, style = MaterialTheme.typography.titleMedium)
            Text(libro.autor, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.padding(top = 8.dp)) {
                val disponible = libro.ejemplaresDisponibles > 0
                Text(
                    if (disponible) "Disponible (${libro.ejemplaresDisponibles})" else "Agotado",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (disponible) VerdeActivo else RojoVencido
                )
                Text("  ·  ${libro.categoria}", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
