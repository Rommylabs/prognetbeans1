package cl.biblioteca.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Préstamo activo de un libro a un usuario. Mantiene referencias a los objetos
 * {@link Usuario} y {@link Libro} (asociación), en lugar de copiar sus datos.
 * La fecha de vencimiento no se guarda: se calcula siempre desde la fecha de inicio y los días.
 */
public final class Prestamo {

    /** Multa en pesos por cada día de atraso en la devolución. */
    public static final int MULTA_POR_DIA = 1000;

    private Usuario usuario;
    private Libro libro;
    private LocalDate fechaInicio;
    private int dias;

    /**
     * @param usuario     quien solicita el libro
     * @param libro       libro prestado
     * @param fechaInicio fecha del préstamo
     * @param dias        días de préstamo; entre 1 y el máximo que permite el tipo de usuario
     */
    public Prestamo(Usuario usuario, Libro libro, LocalDate fechaInicio, int dias) {
        setUsuario(usuario);
        setLibro(libro);
        setFechaInicio(fechaInicio);
        setDias(dias);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El préstamo requiere un usuario.");
        }
        this.usuario = usuario;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        if (libro == null) {
            throw new IllegalArgumentException("El préstamo requiere un libro.");
        }
        this.libro = libro;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        if (fechaInicio == null) {
            throw new IllegalArgumentException("El préstamo requiere una fecha de inicio.");
        }
        this.fechaInicio = fechaInicio;
    }

    public int getDias() {
        return dias;
    }

    /** Los días deben estar entre 1 y el plazo máximo del tipo de usuario (20 docentes, 10 estudiantes). */
    public void setDias(int dias) {
        int maximo = usuario.maximoDiasPrestamo();
        if (dias < 1 || dias > maximo) {
            throw new IllegalArgumentException(
                    "Los días de préstamo deben estar entre 1 y " + maximo + " para un " + usuario.getTipo().toLowerCase() + ".");
        }
        this.dias = dias;
    }

    /** Fecha límite de devolución, calculada automáticamente. */
    public LocalDate getFechaVencimiento() {
        return fechaInicio.plusDays(dias);
    }

    /** Días de atraso respecto de la fecha límite; cero si se devuelve a tiempo. */
    public long calcularDiasAtraso(LocalDate fechaDevolucion) {
        return Math.max(0, ChronoUnit.DAYS.between(getFechaVencimiento(), fechaDevolucion));
    }

    /** Multa en pesos: {@value #MULTA_POR_DIA} por cada día de atraso. */
    public long calcularMulta(LocalDate fechaDevolucion) {
        return calcularDiasAtraso(fechaDevolucion) * MULTA_POR_DIA;
    }

    /** Genera el texto de la tarjeta de préstamo que se imprime y se pega en el libro. */
    public String generarTarjeta() {
        String linea = "----------------------------------------\n";
        return "BIBLIOTECA UNAB - SEDE ANTONIO VARAS\n"
                + "TARJETA DE PRÉSTAMO\n" + linea
                + "Usuario : " + usuario.getNombreCompleto() + "\n"
                + "RUN     : " + usuario.getRun() + "\n"
                + "Tipo    : " + usuario.getTipo() + "\n" + linea
                + "Libro   : " + libro.getTitulo() + "\n"
                + "Autor   : " + libro.getAutor() + "\n"
                + "ISBN    : " + libro.getIsbn() + "\n" + linea
                + "Préstamo: " + fechaInicio + "\n"
                + "Plazo   : " + dias + " días\n"
                + "Devolver: " + getFechaVencimiento() + "\n" + linea
                + "Multa por atraso: $" + MULTA_POR_DIA + " por día\n";
    }

    @Override
    public String toString() {
        return "Prestamo{run=" + usuario.getRun() + ", isbn=" + libro.getIsbn() + ", fechaInicio=" + fechaInicio
                + ", dias=" + dias + ", fechaVencimiento=" + getFechaVencimiento() + "}";
    }
}
