package cl.biblioteca.modelo;

import java.time.LocalDate;

/**
 * Resultado de una devolución ya registrada: conserva el préstamo que se cerró, la fecha en que
 * se devolvió el libro y la multa calculada. Es de solo lectura (no tiene mutadores).
 */
public final class Devolucion {

    private final Prestamo prestamo;
    private final LocalDate fechaDevolucion;
    private final long diasAtraso;
    private final long multa;

    /**
     * @param prestamo        préstamo que se cierra
     * @param fechaDevolucion fecha en que se entrega el libro
     */
    public Devolucion(Prestamo prestamo, LocalDate fechaDevolucion) {
        if (prestamo == null || fechaDevolucion == null) {
            throw new IllegalArgumentException("La devolución requiere un préstamo y una fecha.");
        }
        this.prestamo = prestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.diasAtraso = prestamo.calcularDiasAtraso(fechaDevolucion);
        this.multa = prestamo.calcularMulta(fechaDevolucion);
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public long getDiasAtraso() {
        return diasAtraso;
    }

    public long getMulta() {
        return multa;
    }

    /** Texto del comprobante que se muestra al registrar la devolución. */
    public String generarComprobante() {
        return "DEVOLUCIÓN REGISTRADA\n"
                + "Libro   : " + prestamo.getLibro().getTitulo() + " (" + prestamo.getLibro().getIsbn() + ")\n"
                + "Usuario : " + prestamo.getUsuario().getNombreCompleto() + " (" + prestamo.getUsuario().getRun() + ")\n"
                + "Fecha límite    : " + prestamo.getFechaVencimiento() + "\n"
                + "Fecha devolución: " + fechaDevolucion + "\n"
                + "Días de atraso  : " + diasAtraso + "\n"
                + "Multa a pagar   : $" + multa + "\n";
    }

    @Override
    public String toString() {
        return "Devolucion{isbn=" + prestamo.getLibro().getIsbn() + ", run=" + prestamo.getUsuario().getRun()
                + ", fechaDevolucion=" + fechaDevolucion + ", diasAtraso=" + diasAtraso + ", multa=" + multa + "}";
    }
}
