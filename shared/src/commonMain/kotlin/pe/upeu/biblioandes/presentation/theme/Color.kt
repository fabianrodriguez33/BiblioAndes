package pe.upeu.biblioandes.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val AzulAndes = Color(0xFF1B4F8A)
val AzulAndesClaro = Color(0xFFD6E4FF)
val OroAndes = Color(0xFFB8860B)
val OroAndesClaro = Color(0xFFFFE9A8)
val VerdeActivo = Color(0xFF2E7D32)
val RojoVencido = Color(0xFFC62828)
val GrisDevuelto = Color(0xFF616161)

val EsquemaClaro = lightColorScheme(
    primary = AzulAndes,
    onPrimary = Color.White,
    primaryContainer = AzulAndesClaro,
    onPrimaryContainer = Color(0xFF00214A),
    secondary = OroAndes,
    onSecondary = Color.White,
    secondaryContainer = OroAndesClaro,
    onSecondaryContainer = Color(0xFF3B2A00),
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF8F9FC),
    onSurface = Color(0xFF1A1C1E),
    error = RojoVencido
)

val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF002F65),
    primaryContainer = Color(0xFF12386B),
    onPrimaryContainer = AzulAndesClaro,
    secondary = Color(0xFFE8C36A),
    onSecondary = Color(0xFF3B2A00),
    secondaryContainer = Color(0xFF574100),
    onSecondaryContainer = OroAndesClaro,
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    error = Color(0xFFFF8A80)
)
