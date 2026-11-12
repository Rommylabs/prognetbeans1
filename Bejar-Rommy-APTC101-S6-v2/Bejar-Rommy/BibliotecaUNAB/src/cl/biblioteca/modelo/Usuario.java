package cl.biblioteca.modelo;

import cl.biblioteca.util.Validaciones;

/**
 * Clase abstracta que reúne los datos comunes de docentes y estudiantes (herencia).
 * <p>
 * Cada atributo se asigna a través de su mutador, de modo que las reglas de negocio
 * (RUN válido, género M/F, textos obligatorios, ISBN del préstamo) se aplican siempre,
 * tanto en el constructor como en las ediciones posteriores.
 * </p>
 * El atributo {@code prestamo} vale {@code "0"} cuando el usuario no tiene un libro en su
 * poder y el ISBN del libro cuando sí lo tiene.
 */
public abstract class Usuario {

    private String nombreCompleto;
    private String run;
    private char genero;
    private String carrera;
    private String prestamo;

    /**
     * Crea un usuario sin préstamo activo.
     *
     * @param nombreCompleto nombre y apellidos
     * @param run            RUN con dígito verificador
     * @param genero         'M' o 'F'
     * @param carrera        carrera asociada al usuario
     */
    protected Usuario(String nombreCompleto, String run, char genero, String carrera) {
        setNombreCompleto(nombreCompleto);
        setRun(run);
        setGenero(genero);
        setCarrera(carrera);
        this.prestamo = Validaciones.SIN_PRESTAMO;
    }

    /** Plazo máximo de préstamo en días; cada subclase define el suyo (polimorfismo). */
    public abstract int maximoDiasPrestamo();

    /** Nombre del tipo de usuario para mostrar en pantallas y archivos. */
    public abstract String getTipo();

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public final void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = Validaciones.textoRequerido(nombreCompleto, "el nombre completo");
    }

    public String getRun() {
        return run;
    }

    public final void setRun(String run) {
        this.run = Validaciones.run(run);
    }

    public char getGenero() {
        return genero;
    }

    public final void setGenero(char genero) {
        this.genero = Validaciones.genero(genero);
    }

    public String getCarrera() {
        return carrera;
    }

    public final void setCarrera(String carrera) {
        this.carrera = Validaciones.textoRequerido(carrera, "la carrera");
    }

    public String getPrestamo() {
        return prestamo;
    }

    public final void setPrestamo(String prestamo) {
        this.prestamo = Validaciones.prestamo(prestamo);
    }

    /** Indica si el usuario puede solicitar un libro (no tiene ninguno en su poder). */
    public boolean estaHabilitado() {
        return Validaciones.SIN_PRESTAMO.equals(prestamo);
    }

    /** Deja al usuario no disponible para otro préstamo, registrando el ISBN recibido. */
    public void asignarPrestamo(String isbn) {
        if (!estaHabilitado()) {
            throw new IllegalStateException("El usuario ya tiene el libro " + prestamo + " en su poder.");
        }
        setPrestamo(Validaciones.isbn(isbn));
    }

    /** Vuelve a habilitar al usuario para solicitar un nuevo libro. */
    public void liberarPrestamo() {
        this.prestamo = Validaciones.SIN_PRESTAMO;
    }

    @Override
    public String toString() {
        return getTipo() + "{nombreCompleto=" + nombreCompleto + ", run=" + run + ", genero=" + genero
                + ", carrera=" + carrera + ", prestamo=" + prestamo + "}";
    }
}
