package pe.upeu.biblioandes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import pe.upeu.biblioandes.data.local.DatosSimulados
import pe.upeu.biblioandes.data.repository.BibliotecaRepositoryFake
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
import pe.upeu.biblioandes.domain.model.OrdenCatalogo
import pe.upeu.biblioandes.domain.model.Prestamo
import pe.upeu.biblioandes.domain.repository.BibliotecaRepository
import pe.upeu.biblioandes.domain.usecase.MotivoRechazo
import pe.upeu.biblioandes.domain.usecase.ObtenerCatalogoUseCase
import pe.upeu.biblioandes.domain.usecase.ObtenerPrestamosUseCase
import pe.upeu.biblioandes.domain.usecase.ResultadoSolicitud
import pe.upeu.biblioandes.domain.usecase.SolicitarPrestamoUseCase
import pe.upeu.biblioandes.domain.util.Reloj

/** Repositorio con préstamos configurables para aislar cada regla. */
private class RepoDePrueba(private val prestamosIniciales: List<Prestamo>) :
    BibliotecaRepository by BibliotecaRepositoryFake() {
    private val extra = mutableListOf<Prestamo>()
    override suspend fun obtenerPrestamos() = prestamosIniciales + extra
    override suspend fun registrarPrestamo(libroId: Int, fechaPrestamo: String, fechaLimite: String): Prestamo {
        val p = Prestamo(100 + extra.size, DatosSimulados.libros.first { it.id == libroId },
            fechaPrestamo, fechaLimite, EstadoPrestamo.Activo(7))
        extra += p
        return p
    }
}

class ReglasNegocioTest {
    private val hoy = LocalDate(2026, 9, 23)
    private val reloj = Reloj { hoy }
    private val repo = BibliotecaRepositoryFake()
    private val prestamos = DatosSimulados.prestamos
    private val activo = prestamos[0]
    private val devuelto = prestamos[2]

    @Test
    fun rn03EvaluaVencidoPorFechaLimite() = runTest {
        val lista = ObtenerPrestamosUseCase(repo, reloj)()
        assertEquals(EstadoPrestamo.Vencido(19), lista.first { it.id == 5 }.estado)
        assertEquals(EstadoPrestamo.Activo(4), lista.first { it.id == 1 }.estado)
    }

    @Test
    fun rn04MorosidadBloqueaSolicitudes() = runTest {
        val r = SolicitarPrestamoUseCase(repo, reloj)(1)
        assertEquals(ResultadoSolicitud.Rechazada(MotivoRechazo.MOROSIDAD), r)
    }

    @Test
    fun rn02SinEjemplaresSeRechaza() = runTest {
        val caso = SolicitarPrestamoUseCase(RepoDePrueba(listOf(devuelto)), reloj)
        assertEquals(ResultadoSolicitud.Rechazada(MotivoRechazo.SIN_EJEMPLARES), caso(2))
    }

    @Test
    fun rn01LimiteDeTresActivos() = runTest {
        val tres = listOf(activo, prestamos[1], activo.copy(id = 9))
        val caso = SolicitarPrestamoUseCase(RepoDePrueba(tres), reloj)
        assertEquals(ResultadoSolicitud.Rechazada(MotivoRechazo.LIMITE_ALCANZADO), caso(7))
    }

    @Test
    fun rn03DuracionDeSieteDias() = runTest {
        val caso = SolicitarPrestamoUseCase(RepoDePrueba(listOf(activo)), reloj)
        val r = caso(7)
        assertIs<ResultadoSolicitud.Exito>(r)
        assertEquals("2026-09-30", r.prestamo.fechaLimite)
    }

    private suspend fun activosYLimite(lista: List<Prestamo>): Pair<Int, Boolean> {
        val repoPrueba = RepoDePrueba(lista)
        return ObtenerPrestamosUseCase(repoPrueba, reloj).contarActivos() to
            SolicitarPrestamoUseCase(repoPrueba, reloj).limiteAlcanzado()
    }

    @Test
    fun rn01SinPrestamosActivosNoAlcanzaElLimite() = runTest {
        assertEquals(0 to false, activosYLimite(listOf(devuelto)))
    }

    @Test
    fun rn01DosActivosNoAlcanzanElLimite() = runTest {
        assertEquals(2 to false, activosYLimite(listOf(activo, prestamos[1])))
    }

    @Test
    fun rn01TresActivosAlcanzanElLimite() = runTest {
        assertEquals(3 to true, activosYLimite(listOf(activo, prestamos[1], activo.copy(id = 9))))
    }

    @Test
    fun prestamoVencidoNoCuentaComoActivo() = runTest {
        val conVencido = listOf(activo, prestamos[1], prestamos[4])
        assertEquals(2 to false, activosYLimite(conVencido))
    }

    @Test
    fun ordenPorTituloEsAscendenteYNoDistingueTildes() = runTest {
        val caso = ObtenerCatalogoUseCase(repo)
        val ids = caso.filtrar(DatosSimulados.libros, "", null, orden = OrdenCatalogo.TITULO).map { it.id }
        // "Álgebra" (8) va primero pese a la tilde; "Cien" (11) precede a "Clean" (7).
        assertEquals(listOf(8, 3, 11, 7, 12, 2, 6, 1, 10, 4, 5, 9), ids)
    }

    @Test
    fun ordenPorAnioEsDescendenteYDesempataPorTitulo() = runTest {
        val caso = ObtenerCatalogoUseCase(repo)
        val ids = caso.filtrar(DatosSimulados.libros, "", null, orden = OrdenCatalogo.ANIO).map { it.id }
        assertEquals(listOf(5, 1, 10, 7, 4, 2, 6, 9, 8, 3, 11, 12), ids)
    }

    @Test
    fun ordenConservaCategoriaSeleccionada() = runTest {
        val caso = ObtenerCatalogoUseCase(repo)
        val libros = DatosSimulados.libros
        assertEquals(listOf(7, 2, 1), caso.filtrar(libros, "", "Programación", orden = OrdenCatalogo.TITULO).map { it.id })
        assertEquals(listOf(1, 7, 2), caso.filtrar(libros, "", "Programación", orden = OrdenCatalogo.ANIO).map { it.id })
    }

    @Test
    fun ordenSeCombinaConBusquedaYSoloDisponibles() = runTest {
        val caso = ObtenerCatalogoUseCase(repo)
        val libros = DatosSimulados.libros
        assertEquals(listOf(4, 5), caso.filtrar(libros, "redes", null, orden = OrdenCatalogo.TITULO).map { it.id })
        assertEquals(listOf(5, 4), caso.filtrar(libros, "redes", null, orden = OrdenCatalogo.ANIO).map { it.id })
        // Con "Solo disponibles" desaparece el agotado (5) y el orden se mantiene.
        assertEquals(
            listOf(4, 9),
            caso.filtrar(libros, "", "Redes", soloDisponibles = true, orden = OrdenCatalogo.ANIO).map { it.id }
        )
    }

    @Test
    fun todosLosLibrosTienenEditorial() = runTest {
        val libros = repo.obtenerLibros()
        assertEquals(12, libros.size)
        assertEquals(emptyList(), libros.filter { it.editorial.isBlank() })
    }

    @Test
    fun elPrestamoConservaLaEditorialDelLibro() = runTest {
        val nuevo = repo.registrarPrestamo(7, "2026-09-23", "2026-09-30")
        assertEquals("Pearson", nuevo.libro.editorial)
        // El ejemplar descontado no altera el resto de los datos del libro.
        assertEquals("Pearson", repo.obtenerLibro(7)?.editorial)
    }

    @Test
    fun busquedaIgnoraTildesYMayusculas() = runTest {
        val caso = ObtenerCatalogoUseCase(repo)
        val libros = DatosSimulados.libros
        assertEquals(listOf(3), caso.filtrar(libros, "CALCULO", null).map { it.id })
        assertEquals(listOf(11), caso.filtrar(libros, "garcia marquez", "Literatura").map { it.id })
    }

    @Test
    fun soloDisponiblesOcultaLibrosSinEjemplares() = runTest {
        val caso = ObtenerCatalogoUseCase(repo)
        val resultado = caso.filtrar(DatosSimulados.libros, "", null, soloDisponibles = true)
        assertEquals(10, resultado.size)
        assertEquals(emptyList(), resultado.filter { it.ejemplaresDisponibles == 0 })
    }

    @Test
    fun soloDisponiblesSeCombinaConCategoriaYBusqueda() = runTest {
        val caso = ObtenerCatalogoUseCase(repo)
        val libros = DatosSimulados.libros
        // Programación tiene un libro agotado (id 2): con el filtro solo quedan 1 y 7.
        // El orden por defecto es por título: Clean Architecture (7), Estructuras (2), Kotlin (1).
        assertEquals(listOf(7, 2, 1), caso.filtrar(libros, "", "Programación").map { it.id })
        assertEquals(listOf(7, 1), caso.filtrar(libros, "", "Programación", soloDisponibles = true).map { it.id })
        // Un libro agotado buscado por nombre no aparece con el filtro activo.
        assertEquals(emptyList(), caso.filtrar(libros, "estructuras", "Programación", soloDisponibles = true))
    }
}
