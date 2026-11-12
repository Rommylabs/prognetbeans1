package cl.biblioteca.util;

/**
 * Funciones estáticas de validación y normalización reutilizadas por las clases del modelo.
 * Cada función devuelve el valor ya normalizado o lanza {@link IllegalArgumentException}
 * con un mensaje comprensible para el usuario final.
 */
public final class Validaciones {

    /** Valor del atributo préstamo de un usuario que no tiene ningún libro en su poder. */
    public static final String SIN_PRESTAMO = "0";

    private Validaciones() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Valida formato y dígito verificador (módulo 11) de un RUN chileno.
     *
     * @param run texto ingresado, con o sin puntos y guion
     * @return RUN normalizado con la forma {@code 12345678-5}
     */
    public static String run(String run) {
        if (run == null) {
            throw new IllegalArgumentException("Ingrese el RUN.");
        }
        String limpio = run.replace(".", "").replace("-", "").replaceAll("\\s+", "").toUpperCase();
        if (!limpio.matches("[0-9]{1,8}[0-9K]")) {
            throw new IllegalArgumentException("El RUN tiene un formato inválido.");
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char digito = limpio.charAt(limpio.length() - 1);
        if (digito != calcularDigitoVerificador(cuerpo)) {
            throw new IllegalArgumentException("El dígito verificador del RUN es incorrecto.");
        }
        return Long.parseLong(cuerpo) + "-" + digito;
    }

    /** Calcula el dígito verificador esperado para el cuerpo numérico de un RUN. */
    private static char calcularDigitoVerificador(String cuerpo) {
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = (factor == 7) ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        }
        return (resto == 10) ? 'K' : (char) ('0' + resto);
    }

    /** Exige un texto con contenido y lo devuelve sin espacios sobrantes. */
    public static String textoRequerido(String texto, String etiqueta) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese " + etiqueta + ".");
        }
        return texto.trim();
    }

    /** Acepta solo M o F (en mayúscula o minúscula) y lo devuelve en mayúscula. */
    public static char genero(char genero) {
        char normalizado = Character.toUpperCase(genero);
        if (normalizado != 'M' && normalizado != 'F') {
            throw new IllegalArgumentException("El género debe ser M o F.");
        }
        return normalizado;
    }

    /**
     * Valida un ISBN-10 (el último carácter puede ser X) o un ISBN-13 numérico.
     *
     * @return ISBN sin guiones ni espacios
     */
    public static String isbn(String isbn) {
        String limpio = textoRequerido(isbn, "el ISBN").replace("-", "").replace(" ", "").toUpperCase();
        if (!limpio.matches("[0-9]{13}|[0-9]{9}[0-9X]")) {
            throw new IllegalArgumentException("ISBN inválido: use 10 caracteres (el último puede ser X) o 13 dígitos.");
        }
        return limpio;
    }

    /** Valida el atributo préstamo de un usuario: "0" (sin libro) o el ISBN del libro en su poder. */
    public static String prestamo(String prestamo) {
        if (SIN_PRESTAMO.equals(prestamo)) {
            return SIN_PRESTAMO;
        }
        return isbn(prestamo);
    }

    /**
     * Normaliza los grados de un docente. Se aceptan Magíster, Doctor o ambos.
     *
     * @return {@code Magíster}, {@code Doctor} o {@code Magíster y Doctor}
     */
    public static String grados(String grados) {
        String texto = textoRequerido(grados, "los grados del docente (magíster y/o doctor)")
                .toLowerCase().replace('í', 'i');
        boolean magister = false;
        boolean doctor = false;
        boolean valido = true;
        for (String palabra : texto.split("[^a-z]+")) {
            if (palabra.equals("magister")) {
                magister = true;
            } else if (palabra.equals("doctor")) {
                doctor = true;
            } else if (!palabra.isEmpty() && !palabra.equals("y") && !palabra.equals("e")) {
                valido = false;
            }
        }
        if (!valido || (!magister && !doctor)) {
            throw new IllegalArgumentException("Los grados deben ser Magíster, Doctor o Magíster y Doctor.");
        }
        if (magister && doctor) {
            return "Magíster y Doctor";
        }
        return magister ? "Magíster" : "Doctor";
    }

    /** Verifica que un entero esté dentro de un rango cerrado. */
    public static int enRango(int valor, int minimo, int maximo, String mensaje) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }
}
