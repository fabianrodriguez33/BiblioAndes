package pe.upeu.biblioandes.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.upeu.biblioandes.presentation.catalogo.CatalogoScreen
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroScreen
import pe.upeu.biblioandes.presentation.detalle.DetalleLibroViewModel
import pe.upeu.biblioandes.presentation.inicio.InicioScreen
import pe.upeu.biblioandes.presentation.perfil.PerfilScreen
import pe.upeu.biblioandes.presentation.perfil.PerfilViewModel
import pe.upeu.biblioandes.presentation.prestamos.PrestamosScreen
import pe.upeu.biblioandes.presentation.prestamos.PrestamosViewModel
import pe.upeu.biblioandes.presentation.theme.BiblioAndesTheme

private data class ItemNavegacion(val destino: Destino, val etiqueta: String, val icono: ImageVector)

private val itemsNavegacion = listOf(
    ItemNavegacion(Destino.Inicio, "Inicio", Icons.Filled.Home),
    ItemNavegacion(Destino.Catalogo, "Catálogo", Icons.Filled.Search),
    ItemNavegacion(Destino.Prestamos, "Préstamos", Icons.AutoMirrored.Filled.List)
)

@Composable
fun AppNavHost() {
    val perfilViewModel = koinViewModel<PerfilViewModel>()
    val temaOscuro by perfilViewModel.temaOscuro.collectAsState()
    // Se crea en la raíz para que el badge de la barra inferior sea visible desde cualquier pestaña.
    val prestamosViewModel = koinViewModel<PrestamosViewModel>()
    val prestamosActivos by prestamosViewModel.prestamosActivos.collectAsState()

    // El tema se aplica en la raíz del árbol para que el Switch afecte a toda la app.
    BiblioAndesTheme(temaOscuro) {
        val navController = rememberNavController()
        val entradaActual by navController.currentBackStackEntryAsState()
        val destinoActual = entradaActual?.destination

        // Refresca el contador al cambiar de pantalla (p. ej. tras solicitar un préstamo).
        LaunchedEffect(destinoActual?.route) { prestamosViewModel.actualizarActivos() }

        Scaffold(
            bottomBar = {
                if (itemsNavegacion.any { item -> destinoActual?.hierarchy?.any { it.route == item.destino.ruta } == true }) {
                    NavigationBar {
                        itemsNavegacion.forEach { item ->
                            NavigationBarItem(
                                selected = destinoActual?.hierarchy?.any { it.route == item.destino.ruta } == true,
                                onClick = { navController.navegarAPrincipal(item.destino) },
                                icon = {
                                    if (item.destino == Destino.Prestamos && prestamosActivos > 0) {
                                        BadgedBox(badge = { Badge { Text(prestamosActivos.toString()) } }) {
                                            Icon(item.icono, contentDescription = item.etiqueta)
                                        }
                                    } else {
                                        Icon(item.icono, contentDescription = item.etiqueta)
                                    }
                                },
                                label = { Text(item.etiqueta) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(navController, startDestination = Destino.Inicio.ruta, modifier = Modifier.padding(padding)) {
                composable(Destino.Inicio.ruta) {
                    InicioScreen(
                        viewModel = koinViewModel(),
                        onIrCatalogo = { navController.navegarAPrincipal(Destino.Catalogo) },
                        onIrPrestamos = { navController.navegarAPrincipal(Destino.Prestamos) },
                        onIrPerfil = { navController.navigate(Destino.Perfil.ruta) }
                    )
                }
                composable(Destino.Catalogo.ruta) {
                    CatalogoScreen(
                        viewModel = koinViewModel(),
                        onLibroClick = { navController.navigate(Destino.Detalle.crearRuta(it)) }
                    )
                }
                composable(Destino.Prestamos.ruta) { PrestamosScreen(prestamosViewModel) }
                composable(Destino.Perfil.ruta) {
                    PerfilScreen(perfilViewModel, onAtras = { navController.popBackStack() })
                }
                composable(
                    Destino.Detalle.ruta,
                    arguments = listOf(navArgument(Destino.ARG_LIBRO_ID) { type = NavType.IntType })
                ) { entrada ->
                    val libroId = entrada.arguments?.read { getInt(Destino.ARG_LIBRO_ID) } ?: 0
                    DetalleLibroScreen(
                        viewModel = koinViewModel<DetalleLibroViewModel>(parameters = { parametersOf(libroId) }),
                        onAtras = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

private fun NavHostController.navegarAPrincipal(destino: Destino) {
    navigate(destino.ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
