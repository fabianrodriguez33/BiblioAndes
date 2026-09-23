package pe.upeu.biblioandes.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BiblioAndesTheme(temaOscuro: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (temaOscuro) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        content = content
    )
}
