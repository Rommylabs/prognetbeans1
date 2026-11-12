package cl.biblioteca.modelo;

import cl.biblioteca.util.Validaciones;

/**
 * Docente de la universidad. Además de los datos de {@link Usuario} registra su profesión y
 * sus grados (magíster y/o doctor). Su plazo máximo de préstamo es de 20 días.
 */
public final class Docente extends Usuario {

    /** Plazo máximo de préstamo para docentes, en días. */
    public static final int DIAS_MAXIMOS = 20;

    private String profesion;
    private String grados;

    /**
     * @param nombreCompleto nombre y apellidos
     * @param run            RUN con dígito verificador
     * @param genero         'M' o 'F'
     * @param carrera        carrera o área en la que enseña
     * @param profesion      profesión del docente
     * @param grados         Magíster, Doctor o Magíster y Doctor
     */
    public Docente(String nombreCompleto, String run, char genero, String carrera,
            String profesion, String grados) {
        super(nombreCompleto, run, genero, carrera);
        setProfesion(profesion);
        setGrados(grados);
    }

    public String getProfesion() {
        return profesion;
    }

    public void setProfesion(String profesion) {
        this.profesion = Validaciones.textoRequerido(profesion, "la profesión");
    }

    public String getGrados() {
        return grados;
    }

    public void setGrados(String grados) {
        this.grados = Validaciones.grados(grados);
    }

    @Override
    public int maximoDiasPrestamo() {
        return DIAS_MAXIMOS;
    }

    @Override
    public String getTipo() {
        return "Docente";
    }

    @Override
    public String toString() {
        return super.toString() + "{profesion=" + profesion + ", grados=" + grados + "}";
    }
}
