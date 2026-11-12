package cl.biblioteca.prueba;

import cl.biblioteca.modelo.Devolucion;
import cl.biblioteca.modelo.Docente;
import cl.biblioteca.modelo.Estudiante;
import cl.biblioteca.modelo.Libro;
import cl.biblioteca.modelo.Prestamo;
import cl.biblioteca.persistencia.RepositorioTexto;
import cl.biblioteca.servicio.BibliotecaServicio;
import cl.biblioteca.util.Validaciones;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

/**
 * Pruebas de la lógica de negocio sin interfaz gráfica ni librerías externas.
 * Ejecutar la clase imprime una línea OK/FALLA por prueba y termina con código 1 si alguna falla.
 */
public final class PruebaLogica {

    private static int total;
    private static int fallas;

    private PruebaLogica() {
        // Clase de pruebas: no se instancia.
    }

    private interface Codigo {
        void correr() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        pruebasDeValidaciones();
        pruebasDelModelo();
        pruebasDelServicio();
        System.out.println();
        System.out.println("RESULTADO: " + (total - fallas) + " de " + total + " pruebas correctas.");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    private static void pruebasDeValidaciones() {
        verificar("RUN válido se normaliza", () -> igual("12345678-5", Validaciones.run("12.345.678-5")));
        verificar("RUN con K como dígito verificador", () -> igual("16222333-K", Validaciones.run("16.222.333-k")));
        rechaza("RUN con dígito verificador incorrecto", () -> Validaciones.run("12.345.678-9"));
        rechaza("RUN con letras", () -> Validaciones.run("abc"));
        verificar("Género en minúscula pasa a mayúscula", () -> igual('F', Validaciones.genero('f')));
        rechaza("Género X", () -> Validaciones.genero('X'));
        verificar("ISBN-13 válido", () -> igual("9780306406157", Validaciones.isbn("978-0-306-40615-7")));
        verificar("ISBN-10 con X final", () -> igual("080442957X", Validaciones.isbn("080442957X")));
        rechaza("ISBN de 11 dígitos", () -> Validaciones.isbn("12345678901"));
        verificar("Grados: magister y doctor", () -> igual("Magíster y Doctor", Validaciones.grados("magister y doctor")));
        rechaza("Grados: licenciado", () -> Validaciones.grados("Licenciado"));
    }

    private static void pruebasDelModelo() {
        Estudiante estudiante = new Estudiante("Camila Rojas", "18.765.432-7", 'F', "Informática");
        Docente docente = new Docente("Luis Soto", "10.000.000-8", 'M', "Informática", "Ingeniero", "Doctor");
        verificar("Plazo del estudiante es 10 días", () -> igual(10, estudiante.maximoDiasPrestamo()));
        verificar("Plazo del docente es 20 días", () -> igual(20, docente.maximoDiasPrestamo()));
        verificar("Usuario nuevo está habilitado y su préstamo es 0", () -> {
            exigir(estudiante.estaHabilitado());
            igual("0", estudiante.getPrestamo());
        });
        rechaza("Setter de nombre rechaza texto vacío", () -> estudiante.setNombreCompleto("  "));
        rechaza("Setter de préstamo rechaza un ISBN inválido", () -> estudiante.setPrestamo("xyz"));
        verificar("asignarPrestamo deja al usuario no habilitado", () -> {
            Estudiante otro = new Estudiante("Ana Pino", "12.345.678-5", 'F', "Derecho");
            otro.asignarPrestamo("9780306406157");
            exigir(!otro.estaHabilitado());
            rechazaYa(() -> otro.asignarPrestamo("0306406152"));
        });

        Libro libro = new Libro("9780306406157", "Programación en Java", "Joyanes", 2, 2, null);
        verificar("Libro sin imagen guarda texto vacío", () -> igual("", libro.getImagen()));
        rechaza("Libro: disponible mayor que total", () -> libro.setCantidadDisponible(3));
        rechaza("Libro: total igual a cero", () -> libro.setCantidadBiblioteca(0));
        verificar("prestarEjemplar y recibirEjemplar mueven el inventario", () -> {
            libro.prestarEjemplar();
            igual(1, libro.getCantidadDisponible());
            libro.recibirEjemplar();
            igual(2, libro.getCantidadDisponible());
        });
        rechaza("recibirEjemplar con el inventario completo", libro::recibirEjemplar);

        LocalDate inicio = LocalDate.of(2026, 11, 2);
        Prestamo prestamo = new Prestamo(estudiante, libro, inicio, 8);
        verificar("Vencimiento = inicio + días", () -> igual(LocalDate.of(2026, 11, 10), prestamo.getFechaVencimiento()));
        verificar("Sin atraso no hay multa", () -> igual(0L, prestamo.calcularMulta(LocalDate.of(2026, 11, 10))));
        verificar("3 días de atraso = $3.000", () -> igual(3000L, prestamo.calcularMulta(LocalDate.of(2026, 11, 13))));
        rechaza("Préstamo de 11 días a un estudiante", () -> new Prestamo(estudiante, libro, inicio, 11));
        rechaza("Préstamo sin libro", () -> new Prestamo(estudiante, null, inicio, 5));
        verificar("Devolucion calcula atraso y multa", () -> {
            Devolucion devolucion = new Devolucion(prestamo, LocalDate.of(2026, 11, 12));
            igual(2L, devolucion.getDiasAtraso());
            igual(2000L, devolucion.getMulta());
        });
    }

    private static void pruebasDelServicio() throws IOException {
        RepositorioTexto repositorio = new RepositorioTexto(Files.createTempDirectory("biblioteca_prueba_"));
        BibliotecaServicio servicio = new BibliotecaServicio(repositorio);
        Estudiante estudiante = new Estudiante("Camila Rojas", "18.765.432-7", 'F', "Informática");
        Docente docente = new Docente("Luis Soto", "10.000.000-8", 'M', "Informática", "Ingeniero", "Magíster");
        Libro libro = new Libro("9780306406157", "Programación en Java", "Joyanes", 1, 1, "");
        LocalDate inicio = LocalDate.of(2026, 11, 2);

        verificarIO("Ingreso de usuarios y libro", () -> {
            servicio.ingresarUsuario(estudiante);
            servicio.ingresarUsuario(docente);
            servicio.ingresarLibro(libro);
        });
        rechazaIO("RUN duplicado", () -> servicio.ingresarUsuario(
                new Estudiante("Copia", "18765432-7", 'M', "Derecho")));
        rechazaIO("ISBN duplicado", () -> servicio.ingresarLibro(
                new Libro("9780306406157", "Copia", "Autor", 1, 1, "")));
        rechazaIO("Libro nuevo con cero disponibles", () -> servicio.ingresarLibro(
                new Libro("0306406152", "Sin stock", "Autor", 1, 0, "")));
        verificarIO("Préstamo válido actualiza usuario y libro", () -> {
            Prestamo prestamo = servicio.registrarPrestamo("9780306406157", "18.765.432-7", 10, inicio);
            igual(LocalDate.of(2026, 11, 12), prestamo.getFechaVencimiento());
            igual("9780306406157", estudiante.getPrestamo());
            igual(0, libro.getCantidadDisponible());
        });
        rechazaIO("Sin ejemplares disponibles", () -> servicio.registrarPrestamo("9780306406157", "10.000.000-8", 5, inicio));
        rechazaIO("Usuario no habilitado", () -> servicio.registrarPrestamo("9780306406157", "18.765.432-7", 5, inicio));
        rechazaIO("Eliminar libro prestado", () -> servicio.eliminarLibro("9780306406157"));
        rechazaIO("Eliminar usuario con préstamo", () -> servicio.eliminarUsuario("18.765.432-7"));
        verificarIO("Los datos se recuperan desde los archivos", () -> {
            BibliotecaServicio otra = new BibliotecaServicio(repositorio);
            igual(2, otra.getUsuarios().size());
            igual(1, otra.getPrestamos().size());
            igual("9780306406157", otra.buscarUsuario("18.765.432-7").getPrestamo());
            igual(0, otra.buscarLibro("9780306406157").getCantidadDisponible());
        });
        verificarIO("Devolución con 3 días de atraso cobra $3.000", () -> {
            Devolucion devolucion = servicio.registrarDevolucion("9780306406157", "18.765.432-7", LocalDate.of(2026, 11, 15));
            igual(3000L, devolucion.getMulta());
            exigir(estudiante.estaHabilitado());
            igual(1, libro.getCantidadDisponible());
        });
        rechazaIO("Devolver dos veces el mismo libro", () -> servicio.registrarDevolucion(
                "9780306406157", "18.765.432-7", inicio));
        verificarIO("Editar usuario cambia el tipo y conserva el RUN", () -> {
            servicio.editarUsuario(new Docente("Camila Rojas", "18.765.432-7", 'F', "Informática", "Ingeniera", "Doctor"));
            igual("Docente", servicio.buscarUsuario("18765432-7").getTipo());
        });
        verificarIO("Eliminar usuario y libro sin préstamos", () -> {
            servicio.eliminarUsuario("10.000.000-8");
            servicio.eliminarLibro("9780306406157");
            igual(1, new BibliotecaServicio(repositorio).getUsuarios().size());
        });
        rechazaIO("Eliminar un usuario inexistente", () -> servicio.eliminarUsuario("10.000.000-8"));
    }

    // ----------------------------------------------------------------- ayudas

    private static void verificar(String nombre, Runnable prueba) {
        verificarIO(nombre, prueba::run);
    }

    private static void verificarIO(String nombre, Codigo prueba) {
        total++;
        try {
            prueba.correr();
            System.out.println("OK:    " + nombre);
        } catch (Exception | AssertionError e) {
            fallas++;
            System.out.println("FALLA: " + nombre + " -> " + e);
        }
    }

    private static void rechaza(String nombre, Runnable prueba) {
        rechazaIO(nombre, prueba::run);
    }

    /** La prueba pasa solo si el código lanza una excepción de regla de negocio. */
    private static void rechazaIO(String nombre, Codigo prueba) {
        total++;
        try {
            prueba.correr();
            fallas++;
            System.out.println("FALLA: " + nombre + " -> no fue rechazado");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("OK:    " + nombre + " (rechazado: " + e.getMessage() + ")");
        } catch (Exception e) {
            fallas++;
            System.out.println("FALLA: " + nombre + " -> " + e);
        }
    }

    private static void rechazaYa(Runnable codigo) {
        try {
            codigo.run();
        } catch (IllegalArgumentException | IllegalStateException esperado) {
            return;
        }
        throw new AssertionError("Debía lanzar una excepción.");
    }

    private static void exigir(boolean condicion) {
        if (!condicion) {
            throw new AssertionError("La condición no se cumplió.");
        }
    }

    private static void igual(Object esperado, Object real) {
        if (!esperado.equals(real)) {
            throw new AssertionError("esperado " + esperado + " pero fue " + real);
        }
    }
}
