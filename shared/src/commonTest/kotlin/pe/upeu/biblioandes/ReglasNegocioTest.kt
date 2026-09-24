package pe.upeu.biblioandes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import pe.upeu.biblioandes.data.local.DatosSimulados
import pe.upeu.biblioandes.data.repository.BibliotecaRepositoryFake
import pe.upeu.biblioandes.domain.model.EstadoPrestamo
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
        assertEquals(listOf(1, 2, 7), caso.filtrar(libros, "", "Programación").map { it.id })
        assertEquals(listOf(1, 7), caso.filtrar(libros, "", "Programación", soloDisponibles = true).map { it.id })
        // Un libro agotado buscado por nombre no aparece con el filtro activo.
        assertEquals(emptyList(), caso.filtrar(libros, "estructuras", "Programación", soloDisponibles = true))
    }
}
