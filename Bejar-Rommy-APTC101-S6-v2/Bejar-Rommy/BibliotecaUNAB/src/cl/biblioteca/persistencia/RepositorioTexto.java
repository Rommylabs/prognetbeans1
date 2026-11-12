package cl.biblioteca.persistencia;

import cl.biblioteca.modelo.Docente;
import cl.biblioteca.modelo.Estudiante;
import cl.biblioteca.modelo.Libro;
import cl.biblioteca.modelo.Prestamo;
import cl.biblioteca.modelo.Usuario;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Almacenamiento externo en archivos de texto plano (usuarios.txt, libros.txt y prestamos.txt).
 * Cada línea es un registro con campos separados por tabulación. Al leer, los objetos se
 * construyen con sus constructores, de modo que un archivo alterado a mano con datos inválidos
 * se rechaza con un {@link IOException} en lugar de cargar información corrupta.
 */
public final class RepositorioTexto {

    private static final String ARCHIVO_USUARIOS = "usuarios.txt";
    private static final String ARCHIVO_LIBROS = "libros.txt";
    private static final String ARCHIVO_PRESTAMOS = "prestamos.txt";

    private final Path carpeta;

    /** @param carpeta directorio donde se guardan los tres archivos; se crea si no existe */
    public RepositorioTexto(Path carpeta) {
        this.carpeta = carpeta;
    }

    public Path getCarpeta() {
        return carpeta;
    }

    // ------------------------------------------------------------------ lectura

    /** Lee los usuarios guardados. Cada línea: tipo, nombre, run, género, carrera, préstamo, profesión, grados. */
    public List<Usuario> leerUsuarios() throws IOException {
        List<Usuario> usuarios = new ArrayList<>();
        for (String[] c : leerRegistros(ARCHIVO_USUARIOS, 8)) {
            try {
                Usuario usuario;
                if ("D".equals(c[0])) {
                    usuario = new Docente(c[1], c[2], c[3].charAt(0), c[4], c[6], c[7]);
                } else if ("E".equals(c[0])) {
                    usuario = new Estudiante(c[1], c[2], c[3].charAt(0), c[4]);
                } else {
                    throw new IOException("Tipo de usuario desconocido: " + c[0]);
                }
                usuario.setPrestamo(c[5]);
                usuarios.add(usuario);
            } catch (RuntimeException e) {
                throw new IOException("Registro de usuario inválido en " + ARCHIVO_USUARIOS + ": " + e.getMessage(), e);
            }
        }
        return usuarios;
    }

    /** Lee los libros guardados. Cada línea: isbn, título, autor, cantidad, disponibles, imagen. */
    public List<Libro> leerLibros() throws IOException {
        List<Libro> libros = new ArrayList<>();
        for (String[] c : leerRegistros(ARCHIVO_LIBROS, 6)) {
            try {
                libros.add(new Libro(c[0], c[1], c[2], Integer.parseInt(c[3]), Integer.parseInt(c[4]), c[5]));
            } catch (RuntimeException e) {
                throw new IOException("Registro de libro inválido en " + ARCHIVO_LIBROS + ": " + e.getMessage(), e);
            }
        }
        return libros;
    }

    /**
     * Lee los préstamos activos y los enlaza con los usuarios y libros ya cargados.
     * Cada línea: run, isbn, fecha de inicio, días.
     */
    public List<Prestamo> leerPrestamos(List<Usuario> usuarios, List<Libro> libros) throws IOException {
        List<Prestamo> prestamos = new ArrayList<>();
        for (String[] c : leerRegistros(ARCHIVO_PRESTAMOS, 4)) {
            Usuario usuario = buscarUsuario(usuarios, c[0]);
            Libro libro = buscarLibro(libros, c[1]);
            if (usuario == null || libro == null) {
                throw new IOException("El préstamo de " + c[0] + " referencia un usuario o libro inexistente.");
            }
            try {
                prestamos.add(new Prestamo(usuario, libro, LocalDate.parse(c[2]), Integer.parseInt(c[3])));
            } catch (RuntimeException e) {
                throw new IOException("Registro de préstamo inválido en " + ARCHIVO_PRESTAMOS + ": " + e.getMessage(), e);
            }
        }
        return prestamos;
    }

    private static Usuario buscarUsuario(List<Usuario> usuarios, String run) {
        for (Usuario usuario : usuarios) {
            if (usuario.getRun().equals(run)) {
                return usuario;
            }
        }
        return null;
    }

    private static Libro buscarLibro(List<Libro> libros, String isbn) {
        for (Libro libro : libros) {
            if (libro.getIsbn().equals(isbn)) {
                return libro;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- escritura

    public void guardarUsuarios(List<Usuario> usuarios) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Usuario u : usuarios) {
            boolean docente = u instanceof Docente;
            lineas.add(armarLinea(docente ? "D" : "E", u.getNombreCompleto(), u.getRun(),
                    String.valueOf(u.getGenero()), u.getCarrera(), u.getPrestamo(),
                    docente ? ((Docente) u).getProfesion() : "", docente ? ((Docente) u).getGrados() : ""));
        }
        escribir(ARCHIVO_USUARIOS, lineas);
    }

    public void guardarLibros(List<Libro> libros) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Libro l : libros) {
            lineas.add(armarLinea(l.getIsbn(), l.getTitulo(), l.getAutor(),
                    String.valueOf(l.getCantidadBiblioteca()), String.valueOf(l.getCantidadDisponible()),
                    l.getImagen()));
        }
        escribir(ARCHIVO_LIBROS, lineas);
    }

    public void guardarPrestamos(List<Prestamo> prestamos) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Prestamo p : prestamos) {
            lineas.add(armarLinea(p.getUsuario().getRun(), p.getLibro().getIsbn(),
                    p.getFechaInicio().toString(), String.valueOf(p.getDias())));
        }
        escribir(ARCHIVO_PRESTAMOS, lineas);
    }

    // ---------------------------------------------------------------- utilidades

    /** Lee un archivo y devuelve sus registros ya separados en campos; si no existe, no hay registros. */
    private List<String[]> leerRegistros(String archivo, int camposEsperados) throws IOException {
        Path ruta = carpeta.resolve(archivo);
        if (!Files.exists(ruta)) {
            return Collections.emptyList();
        }
        List<String[]> registros = new ArrayList<>();
        for (String linea : Files.readAllLines(ruta, StandardCharsets.UTF_8)) {
            if (linea.trim().isEmpty()) {
                continue;
            }
            String[] campos = linea.split("\t", -1);
            if (campos.length != camposEsperados) {
                throw new IOException("Registro mal formado en " + archivo + ".");
            }
            for (int i = 0; i < campos.length; i++) {
                campos[i] = decodificar(campos[i]);
            }
            registros.add(campos);
        }
        return registros;
    }

    /** Escribe en un archivo temporal y luego lo reemplaza, para no dejar archivos a medio escribir. */
    private void escribir(String archivo, List<String> lineas) throws IOException {
        Files.createDirectories(carpeta);
        Path destino = carpeta.resolve(archivo);
        Path temporal = Files.createTempFile(carpeta, "biblioteca-", ".tmp");
        try {
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            try {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    private static String armarLinea(String... campos) {
        StringJoiner linea = new StringJoiner("\t");
        for (String campo : campos) {
            linea.add(codificar(campo));
        }
        return linea.toString();
    }

    /** Protege tabulaciones y saltos de línea dentro de un campo para no romper el formato. */
    private static String codificar(String texto) {
        return texto.replace("%", "%25").replace("\t", "%09").replace("\r", "%0D").replace("\n", "%0A");
    }

    private static String decodificar(String texto) {
        return texto.replace("%0A", "\n").replace("%0D", "\r").replace("%09", "\t").replace("%25", "%");
    }
}
