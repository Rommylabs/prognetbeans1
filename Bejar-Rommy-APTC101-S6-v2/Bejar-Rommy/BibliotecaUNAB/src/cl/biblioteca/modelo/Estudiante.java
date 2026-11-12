package cl.biblioteca.modelo;

/**
 * Estudiante de la universidad. Hereda todos los datos de {@link Usuario}; la carrera que
 * estudia es la del atributo heredado. Su plazo máximo de préstamo es de 10 días.
 */
public final class Estudiante extends Usuario {

    /** Plazo máximo de préstamo para estudiantes, en días. */
    public static final int DIAS_MAXIMOS = 10;

    /**
     * @param nombreCompleto nombre y apellidos
     * @param run            RUN con dígito verificador
     * @param genero         'M' o 'F'
     * @param carrera        carrera que estudia
     */
    public Estudiante(String nombreCompleto, String run, char genero, String carrera) {
        super(nombreCompleto, run, genero, carrera);
    }

    @Override
    public int maximoDiasPrestamo() {
        return DIAS_MAXIMOS;
    }

    @Override
    public String getTipo() {
        return "Estudiante";
    }
}
