package cl.biblioteca;

import cl.biblioteca.modelo.Devolucion;
import cl.biblioteca.modelo.Docente;
import cl.biblioteca.modelo.Estudiante;
import cl.biblioteca.modelo.Libro;
import cl.biblioteca.modelo.Prestamo;
import cl.biblioteca.persistencia.RepositorioTexto;
import cl.biblioteca.servicio.BibliotecaServicio;
import cl.biblioteca.ui.MainFrame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del sistema de biblioteca UNAB (sede Antonio Varas).
 * <p>
 * El método {@link #main(String[])} hace dos cosas, en este orden:
 * </p>
 * <ol>
 * <li>Ejecuta una demostración por consola con datos fijos (sin ingreso por teclado) en una
 * carpeta temporal, para no tocar los datos reales. Crea los objetos con sus constructores,
 * en el orden de las relaciones del diagrama, y llama a los métodos del servicio con casos
 * válidos e inválidos.</li>
 * <li>Abre la interfaz gráfica (JFrame y JDialog) con los datos reales de la carpeta {@code datos}.</li>
 * </ol>
 * Relación en el diagrama: Main depende de BibliotecaServicio y de MainFrame, y crea los objetos
 * del modelo solo como variables locales (dependencia de uso).
 */
public final class Main {

    /** Cambie a false para abrir solo la interfaz gráfica, sin la demostración por consola. */
    private static final boolean EJECUTAR_DEMOSTRACION = true;

    private static int verificaciones;
    private static int correctas;

    private Main() {
        // Clase de arranque: no se instancia.
    }

    /** Operación del servicio cuyo resultado se verifica en la demostración. */
    private interface Operacion {
        void ejecutar() throws IOException;
    }

    public static void main(String[] args) throws IOException {
        if (EJECUTAR_DEMOSTRACION) {
            demostrar();
        }
        SwingUtilities.invokeLater(Main::abrirInterfaz);
    }

    // ------------------------------------------------------------- demostración

    private static void demostrar() throws IOException {
        Path carpeta = new File(System.getProperty("java.io.tmpdir"),
                "biblioteca_demo_" + System.currentTimeMillis()).toPath();
        RepositorioTexto repositorio = new RepositorioTexto(carpeta);       // 1. se crea afuera...
        BibliotecaServicio biblioteca = new BibliotecaServicio(repositorio); // 2. ...y se entrega por constructor

        titulo("1. USUARIOS: herencia (Estudiante y Docente extienden Usuario)");
        Estudiante camila = new Estudiante("Camila Rojas Vega", "18.765.432-7", 'f',
                "Ingeniería en Computación e Informática");
        Docente luis = new Docente("Luis Soto Marín", "10.000.000-8", 'M',
                "Ingeniería en Computación e Informática", "Ingeniero Civil Informático", "Magíster y Doctor");
        System.out.println(camila);
        System.out.println(luis);
        probar("Ingresa un estudiante válido", true, () -> biblioteca.ingresarUsuario(camila));
        probar("Ingresa un docente válido", true, () -> biblioteca.ingresarUsuario(luis));
        probar("Rechaza un RUN con dígito verificador incorrecto", false,
                () -> new Estudiante("Ana Pino", "18.765.432-1", 'F', "Derecho"));
        probar("Rechaza un género distinto de M o F", false,
                () -> new Estudiante("Ana Pino", "11.111.111-1", 'X', "Derecho"));
        probar("Rechaza un RUN repetido", false, () -> biblioteca.ingresarUsuario(
                new Estudiante("Otra Persona", "18765432-7", 'F', "Derecho")));
        Estudiante diego = new Estudiante("Diego Fuentes Lara", "12.345.678-5", 'M', "Derecho");
        probar("Ingresa un segundo estudiante válido", true, () -> biblioteca.ingresarUsuario(diego));
        probar("Rechaza un docente sin grados de magíster o doctor", false,
                () -> new Docente("Pedro Gil", "16.222.333-K", 'M', "Derecho", "Abogado", "Licenciado"));

        titulo("2. LIBROS: validaciones de ISBN y cantidades");
        Libro java = new Libro("9780306406157", "Programación en Java", "Luis Joyanes", 2, 2, "");
        System.out.println(java);
        probar("Ingresa un libro válido", true, () -> biblioteca.ingresarLibro(java));
        probar("Rechaza un ISBN repetido", false, () -> biblioteca.ingresarLibro(
                new Libro("9780306406157", "Otro título", "Otro autor", 1, 1, "")));
        Libro datos = new Libro("0306406152", "Estructuras de Datos", "Ana Autora", 1, 1, "");
        probar("Ingresa un segundo libro válido", true, () -> biblioteca.ingresarLibro(datos));
        probar("Rechaza cantidad disponible mayor a la cantidad en biblioteca", false,
                () -> new Libro("9780132350884", "Clean Code", "R. Martin", 2, 3, ""));
        probar("Rechaza cantidad en biblioteca igual a cero", false,
                () -> new Libro("9780132350884", "Clean Code", "R. Martin", 0, 0, ""));
        probar("Rechaza un ISBN con formato inválido", false,
                () -> new Libro("123", "Título", "Autor", 1, 1, ""));

        titulo("3. PRÉSTAMOS: asociación entre Prestamo, Usuario y Libro");
        LocalDate hoy = LocalDate.now();
        probar("Registra el préstamo de un estudiante por 8 días", true, () -> {
            Prestamo prestamo = biblioteca.registrarPrestamo("9780306406157", "18.765.432-7", 8, hoy);
            System.out.println(prestamo.generarTarjeta());
        });
        System.out.println("Estudiante tras el préstamo: " + camila);
        System.out.println("Libro tras el préstamo     : " + java);
        probar("Rechaza un segundo préstamo al mismo usuario (no habilitado)", false,
                () -> biblioteca.registrarPrestamo("9780306406157", "18.765.432-7", 5, hoy));
        probar("Rechaza 11 días a un estudiante (máximo 10)", false,
                () -> biblioteca.registrarPrestamo("9780306406157", "12.345.678-5", 11, hoy));
        probar("Rechaza 25 días a un docente (máximo 20)", false,
                () -> biblioteca.registrarPrestamo("9780306406157", "10.000.000-8", 25, hoy));
        probar("Registra 20 días a un docente (polimorfismo)", true,
                () -> biblioteca.registrarPrestamo("9780306406157", "10.000.000-8", 20, hoy));
        probar("Rechaza el préstamo de un libro sin ejemplares disponibles", false,
                () -> biblioteca.registrarPrestamo("9780306406157", "18.765.432-7", 5, hoy));
        probar("Rechaza el préstamo de un libro inexistente", false,
                () -> biblioteca.registrarPrestamo("9780132350884", "18.765.432-7", 5, hoy));
        probar("Rechaza eliminar un usuario con un libro en su poder", false,
                () -> biblioteca.eliminarUsuario("18.765.432-7"));
        probar("Rechaza eliminar un libro con ejemplares prestados", false,
                () -> biblioteca.eliminarLibro("9780306406157"));

        titulo("4. DEVOLUCIONES: multa de $1.000 por día de atraso");
        probar("Rechaza devolver con un ISBN que no corresponde al préstamo", false,
                () -> biblioteca.registrarDevolucion("0306406152", "18.765.432-7", hoy));
        LocalDate tarde = hoy.plusDays(8 + 3); // vencía a los 8 días: llega 3 días tarde
        probar("Acepta una devolución con 3 días de atraso y cobra $3.000", true, () -> {
            Devolucion devolucion = biblioteca.registrarDevolucion("9780306406157", "18.765.432-7", tarde);
            System.out.println(devolucion.generarComprobante());
            if (devolucion.getMulta() != 3000) {
                throw new IllegalStateException("La multa esperada era $3000.");
            }
        });
        probar("Acepta una devolución a tiempo sin multa", true, () -> {
            Devolucion devolucion = biblioteca.registrarDevolucion("9780306406157", "10.000.000-8", hoy);
            if (devolucion.getMulta() != 0) {
                throw new IllegalStateException("No debía haber multa.");
            }
        });
        probar("Rechaza devolver un libro que ya no está prestado", false,
                () -> biblioteca.registrarDevolucion("9780306406157", "18.765.432-7", hoy));
        probar("Elimina un libro sin ejemplares prestados", true,
                () -> biblioteca.eliminarLibro("9780306406157"));

        titulo("5. ARCHIVOS DE TEXTO: los cambios se guardan y se recuperan");
        BibliotecaServicio recargada = new BibliotecaServicio(new RepositorioTexto(carpeta));
        probar("Los usuarios se recuperan desde usuarios.txt", true, () -> exigir(recargada.getUsuarios().size() == 3));
        probar("La eliminación de un libro se recuperó desde libros.txt", true,
                () -> exigir(recargada.getLibros().size() == 1));
        System.out.println("Archivos de la demostración en: " + carpeta);
        System.out.println();
        System.out.println("RESULTADO: " + correctas + " de " + verificaciones
                + " verificaciones se comportaron como se esperaba.");
        System.out.println();
    }

    private static void exigir(boolean condicion) {
        if (!condicion) {
            throw new IllegalStateException("La verificación no se cumplió.");
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("==================== " + texto + " ====================");
    }

    /**
     * Ejecuta una operación y compara su resultado con lo esperado: éxito o rechazo por una regla
     * de negocio (IllegalArgumentException / IllegalStateException).
     */
    private static void probar(String descripcion, boolean esperaExito, Operacion operacion) {
        verificaciones++;
        boolean exito;
        String detalle = "";
        try {
            operacion.ejecutar();
            exito = true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            exito = false;
            detalle = " -> " + e.getMessage();
        } catch (IOException e) {
            exito = !esperaExito;
            detalle = " -> error de archivo: " + e.getMessage();
        }
        boolean cumple = (exito == esperaExito);
        if (cumple) {
            correctas++;
        }
        System.out.println((cumple ? "  [CUMPLE] " : "  [FALLA]  ") + verificaciones + ". " + descripcion + detalle);
    }

    // ---------------------------------------------------------------- interfaz

    private static void abrirInterfaz() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            RepositorioTexto repositorio = new RepositorioTexto(Paths.get("datos"));
            BibliotecaServicio servicio = new BibliotecaServicio(repositorio);
            new MainFrame(servicio).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No se pudo iniciar la aplicación: " + e.getMessage(),
                    "Biblioteca UNAB", JOptionPane.ERROR_MESSAGE);
        }
    }
}
