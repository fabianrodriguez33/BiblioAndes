package pe.upeu.biblioandes.domain.usecase

import pe.upeu.biblioandes.domain.model.Libro
import pe.upeu.biblioandes.domain.model.OrdenCatalogo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository

data class Catalogo(val libros: List<Libro>, val categorias: List<String>)

class ObtenerCatalogoUseCase(private val repositorio: BibliotecaRepository) {

    suspend operator fun invoke(): Catalogo =
        Catalogo(repositorio.obtenerLibros(), repositorio.obtenerCategorias())

    /** RF-02 y RF-05: filtra por categoría y por título/autor sin distinguir mayúsculas ni tildes. */
    fun filtrar(
        libros: List<Libro>,
        busqueda: String,
        categoria: String?,
        soloDisponibles: Boolean = false,
        orden: OrdenCatalogo = OrdenCatalogo.TITULO
    ): List<Libro> {
        val termino = normalizar(busqueda.trim())
        val filtrados = libros.filter { libro ->
            (categoria == null || libro.categoria == categoria) &&
                (!soloDisponibles || libro.ejemplaresDisponibles > 0) &&
                (termino.isEmpty() ||
                    normalizar(libro.titulo).contains(termino) ||
                    normalizar(libro.autor).contains(termino))
        }
        return ordenar(filtrados, orden)
    }

    /** SC-C: título ascendente (sin tildes) o año descendente; el título desempata. */
    private fun ordenar(libros: List<Libro>, orden: OrdenCatalogo): List<Libro> = when (orden) {
        OrdenCatalogo.TITULO -> libros.sortedBy { normalizar(it.titulo) }
        OrdenCatalogo.ANIO -> libros.sortedWith(
            compareByDescending<Libro> { it.anio }.thenBy { normalizar(it.titulo) }
        )
    }

    private fun normalizar(texto: String): String = buildString {
        for (c in texto.lowercase()) {
            append(
                when (c) {
                    'á', 'à', 'ä', 'â' -> 'a'
                    'é', 'è', 'ë', 'ê' -> 'e'
                    'í', 'ì', 'ï', 'î' -> 'i'
                    'ó', 'ò', 'ö', 'ô' -> 'o'
                    'ú', 'ù', 'ü', 'û' -> 'u'
                    else -> c
                }
            )
        }
    }
}
