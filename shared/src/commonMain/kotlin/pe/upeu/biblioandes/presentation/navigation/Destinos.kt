package pe.upeu.biblioandes.presentation.navigation

sealed class Destino(val ruta: String) {
    data object Inicio : Destino("inicio")
    data object Catalogo : Destino("catalogo")
    data object Prestamos : Destino("prestamos")
    data object Perfil : Destino("perfil")
    data object Detalle : Destino("detalle/{$ARG_LIBRO_ID}") {
        fun crearRuta(libroId: Int) = "detalle/$libroId"
    }

    companion object {
        const val ARG_LIBRO_ID = "libroId"
    }
}
