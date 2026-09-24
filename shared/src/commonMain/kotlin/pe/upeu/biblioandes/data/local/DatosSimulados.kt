package pe.upeu.biblioandes.data.local

import pe.upeu.biblioandes.domain.model.*

object DatosSimulados {
    val estudiante = Estudiante(
        codigo = "E-2291",
        nombre = "Fabian Rodriguez",
        carrera = "Ingeniería de Sistemas",
        correo = "fabian.rodriguez@correo.pe"
    )

    val categorias = listOf("Programación", "Matemática", "Redes", "Gestión", "Literatura")

    val libros = listOf(
        Libro(1, "Kotlin en profundidad", "M. Salazar", "Ediciones Andes", 2023, "Programación", "Central", 3),
        Libro(2, "Estructuras de datos", "R. Peña", "Alfaomega", 2021, "Programación", "Central", 0),
        Libro(3, "Cálculo aplicado", "L. Ortega", "Pearson", 2019, "Matemática", "Sede Norte", 2),
        Libro(4, "Redes de computadoras", "A. Medina", "Alfaomega", 2022, "Redes", "Sede Sur", 4),
        Libro(5, "Seguridad en redes", "P. Ríos", "Ediciones Andes", 2024, "Redes", "Central", 0),
        Libro(6, "Gestión de proyectos", "S. Delgado", "Planeta", 2021, "Gestión", "Sede Norte", 2),
        Libro(7, "Clean Architecture en Móviles", "G. Martin", "Pearson", 2022, "Programación", "Central", 5),
        Libro(8, "Álgebra Lineal Moderna", "H. Gómez", "McGraw-Hill", 2020, "Matemática", "Sede Sur", 1),
        Libro(9, "Sistemas Operativos Distribuidos", "E. Tanenbaum", "Pearson", 2021, "Redes", "Central", 3),
        Libro(10, "Liderazgo y Gestión de Equipos", "M. Kovacs", "Planeta", 2023, "Gestión", "Sede Norte", 4),
        Libro(11, "Cien Años de Soledad", "G. García Márquez", "Alfaguara", 1967, "Literatura", "Central", 2),
        Libro(12, "Don Quijote de la Mancha", "M. de Cervantes", "Alfaguara", 1605, "Literatura", "Sede Sur", 1)
    )

    val prestamos = listOf(
        Prestamo(1, libros[0], "2026-09-20", "2026-09-27", EstadoPrestamo.Activo(diasRestantes = 4)),
        Prestamo(2, libros[3], "2026-09-21", "2026-09-28", EstadoPrestamo.Activo(diasRestantes = 5)),
        Prestamo(3, libros[2], "2026-08-20", "2026-08-27", EstadoPrestamo.Devuelto(fechaDevolucion = "2026-08-26")),
        Prestamo(4, libros[1], "2026-08-05", "2026-08-12", EstadoPrestamo.Devuelto(fechaDevolucion = "2026-08-11")),
        Prestamo(5, libros[5], "2026-08-28", "2026-09-04", EstadoPrestamo.Vencido(diasDeAtraso = 19))
    )
}
