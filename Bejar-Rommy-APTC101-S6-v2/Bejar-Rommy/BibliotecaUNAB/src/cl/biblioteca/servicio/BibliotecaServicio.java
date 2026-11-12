package cl.biblioteca.servicio;

import cl.biblioteca.modelo.Devolucion;
import cl.biblioteca.modelo.Libro;
import cl.biblioteca.modelo.Prestamo;
import cl.biblioteca.modelo.Usuario;
import cl.biblioteca.persistencia.RepositorioTexto;
import cl.biblioteca.util.Validaciones;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Casos de uso de la biblioteca: administra usuarios y libros y registra préstamos y devoluciones.
 * <p>
 * Es la clase que coordina a las demás: guarda los objetos del modelo en listas
 * ({@link ArrayList}), pide al repositorio que persista cada cambio y deja las reglas propias de
 * cada objeto (rangos, formatos, inventario) en las clases del modelo. No depende de Swing,
 * por lo que se puede ejecutar y probar desde consola.
 * </p>
 */
public final class BibliotecaServicio {

    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Libro> libros = new ArrayList<>();
    private final List<Prestamo> prestamos = new ArrayList<>();
    private final RepositorioTexto repositorio;

    /**
     * Crea el servicio y carga los datos guardados.
     *
     * @param repositorio almacenamiento en archivos de texto (asociación: se entrega ya creado)
     */
    public BibliotecaServicio(RepositorioTexto repositorio) throws IOException {
        this.repositorio = repositorio;
        recargar();
    }

    // ------------------------------------------------------------------ consultas

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    public List<Libro> getLibros() {
        return Collections.unmodifiableList(libros);
    }

    public List<Prestamo> getPrestamos() {
        return Collections.unmodifiableList(prestamos);
    }

    /** Busca un usuario por RUN; devuelve null si no existe. */
    public Usuario buscarUsuario(String run) {
        String normalizado = Validaciones.run(run);
        for (Usuario usuario : usuarios) {
            if (usuario.getRun().equals(normalizado)) {
                return usuario;
            }
        }
        return null;
    }

    /** Busca un libro por ISBN; devuelve null si no existe. */
    public Libro buscarLibro(String isbn) {
        String normalizado = Validaciones.isbn(isbn);
        for (Libro libro : libros) {
            if (libro.getIsbn().equals(normalizado)) {
                return libro;
            }
        }
        return null;
    }

    /** Busca el préstamo activo de un usuario; devuelve null si no tiene. */
    public Prestamo buscarPrestamoDe(Usuario usuario) {
        for (Prestamo prestamo : prestamos) {
            if (prestamo.getUsuario() == usuario) {
                return prestamo;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------- usuarios

    /** Ingresa un usuario nuevo. El RUN no puede repetirse y no puede entrar con un libro en su poder. */
    public void ingresarUsuario(Usuario usuario) throws IOException {
        exigir(usuario, "el usuario");
        if (buscarUsuario(usuario.getRun()) != null) {
            throw new IllegalArgumentException("El RUN " + usuario.getRun() + " ya está registrado.");
        }
        if (!usuario.estaHabilitado()) {
            throw new IllegalArgumentException("Un usuario nuevo no puede ingresar con un libro en su poder.");
        }
        usuarios.add(usuario);
        persistir();
    }

    /**
     * Reemplaza los datos de un usuario existente (identificado por su RUN). Permite cambiar
     * también el tipo (docente/estudiante) siempre que el préstamo activo respete el nuevo plazo.
     */
    public void editarUsuario(Usuario datosNuevos) throws IOException {
        exigir(datosNuevos, "el usuario");
        Usuario actual = buscarUsuario(datosNuevos.getRun());
        if (actual == null) {
            throw new IllegalArgumentException("El usuario " + datosNuevos.getRun() + " no existe.");
        }
        Prestamo activo = buscarPrestamoDe(actual);
        if (activo != null && datosNuevos.maximoDiasPrestamo() < activo.getDias()) {
            throw new IllegalArgumentException("No se puede cambiar el tipo: el préstamo activo excede el plazo permitido.");
        }
        datosNuevos.setPrestamo(actual.getPrestamo());
        usuarios.set(usuarios.indexOf(actual), datosNuevos);
        if (activo != null) {
            activo.setUsuario(datosNuevos);
        }
        persistir();
    }

    /** Elimina un usuario por RUN. Debe existir y no tener un libro en su poder. */
    public void eliminarUsuario(String run) throws IOException {
        Usuario usuario = buscarUsuario(run);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        if (!usuario.estaHabilitado()) {
            throw new IllegalArgumentException("No se puede eliminar: tiene el libro " + usuario.getPrestamo() + " en su poder.");
        }
        usuarios.remove(usuario);
        persistir();
    }

    // ---------------------------------------------------------------------- libros

    /** Ingresa un libro nuevo: ISBN único y cantidad disponible mayor a cero (requerimiento 2.2). */
    public void ingresarLibro(Libro libro) throws IOException {
        exigir(libro, "el libro");
        if (buscarLibro(libro.getIsbn()) != null) {
            throw new IllegalArgumentException("El ISBN " + libro.getIsbn() + " ya está registrado.");
        }
        if (libro.getCantidadDisponible() <= 0) {
            throw new IllegalArgumentException("La cantidad disponible debe ser mayor a cero.");
        }
        libros.add(libro);
        persistir();
    }

    /** Elimina un libro por ISBN. Debe existir y no tener ejemplares prestados. */
    public void eliminarLibro(String isbn) throws IOException {
        Libro libro = buscarLibro(isbn);
        if (libro == null) {
            throw new IllegalArgumentException("El libro no existe.");
        }
        for (Prestamo prestamo : prestamos) {
            if (prestamo.getLibro() == libro) {
                throw new IllegalArgumentException("No se puede eliminar: el libro tiene ejemplares prestados.");
            }
        }
        libros.remove(libro);
        persistir();
    }

    // ------------------------------------------------------------------- préstamos

    /** Registra un préstamo con la fecha actual. */
    public Prestamo registrarPrestamo(String isbn, String run, int dias) throws IOException {
        return registrarPrestamo(isbn, run, dias, LocalDate.now());
    }

    /**
     * Registra un préstamo siguiendo el orden del requerimiento 3.1: el libro debe existir y tener
     * ejemplares, el usuario debe existir y estar habilitado, y los días respetar el plazo de su tipo.
     * Luego deja al usuario no disponible, resta un ejemplar y guarda todo en los archivos.
     *
     * @param fecha fecha de inicio del préstamo (la interfaz envía la fecha actual)
     * @return el préstamo creado, con su fecha de vencimiento calculada
     */
    public Prestamo registrarPrestamo(String isbn, String run, int dias, LocalDate fecha) throws IOException {
        Libro libro = buscarLibro(isbn);
        if (libro == null) {
            throw new IllegalArgumentException("El libro no existe.");
        }
        if (!libro.hayEjemplarDisponible()) {
            throw new IllegalArgumentException("No hay ejemplares disponibles de \"" + libro.getTitulo() + "\".");
        }
        Usuario usuario = buscarUsuario(run);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        if (!usuario.estaHabilitado()) {
            throw new IllegalArgumentException(usuario.getNombreCompleto() + " no está habilitado: tiene el libro "
                    + usuario.getPrestamo() + " en su poder.");
        }
        Prestamo prestamo = new Prestamo(usuario, libro, fecha, dias);
        usuario.asignarPrestamo(libro.getIsbn());
        libro.prestarEjemplar();
        prestamos.add(prestamo);
        persistir();
        return prestamo;
    }

    /** Registra una devolución con la fecha actual. */
    public Devolucion registrarDevolucion(String isbn, String run) throws IOException {
        return registrarDevolucion(isbn, run, LocalDate.now());
    }

    /**
     * Registra una devolución siguiendo el requerimiento 3.2: el libro y el usuario deben existir y
     * el ISBN debe coincidir con el préstamo del usuario. Habilita al usuario, suma un ejemplar y
     * calcula la multa ($1.000 por día de atraso).
     */
    public Devolucion registrarDevolucion(String isbn, String run, LocalDate fecha) throws IOException {
        Libro libro = buscarLibro(isbn);
        if (libro == null) {
            throw new IllegalArgumentException("El libro no existe.");
        }
        Usuario usuario = buscarUsuario(run);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        Prestamo prestamo = buscarPrestamoDe(usuario);
        if (prestamo == null || !libro.getIsbn().equals(usuario.getPrestamo()) || prestamo.getLibro() != libro) {
            throw new IllegalArgumentException("El ISBN no coincide con el préstamo del usuario ("
                    + usuario.getPrestamo() + ").");
        }
        Devolucion devolucion = new Devolucion(prestamo, fecha);
        usuario.liberarPrestamo();
        libro.recibirEjemplar();
        prestamos.remove(prestamo);
        persistir();
        return devolucion;
    }

    // ------------------------------------------------------------------- internos

    private static void exigir(Object objeto, String nombre) {
        if (objeto == null) {
            throw new IllegalArgumentException("Falta " + nombre + ".");
        }
    }

    /** Guarda los tres archivos; si falla, vuelve a leer el disco para no dejar la memoria desfasada. */
    private void persistir() throws IOException {
        try {
            repositorio.guardarUsuarios(usuarios);
            repositorio.guardarLibros(libros);
            repositorio.guardarPrestamos(prestamos);
        } catch (IOException e) {
            try {
                recargar();
            } catch (IOException ignorado) {
                // Se informa el error original de escritura.
            }
            throw e;
        }
    }

    /** Reemplaza el contenido en memoria por el que hay en los archivos. */
    private void recargar() throws IOException {
        List<Usuario> leidosUsuarios = repositorio.leerUsuarios();
        List<Libro> leidosLibros = repositorio.leerLibros();
        List<Prestamo> leidosPrestamos = repositorio.leerPrestamos(leidosUsuarios, leidosLibros);
        usuarios.clear();
        usuarios.addAll(leidosUsuarios);
        libros.clear();
        libros.addAll(leidosLibros);
        prestamos.clear();
        prestamos.addAll(leidosPrestamos);
    }
}
