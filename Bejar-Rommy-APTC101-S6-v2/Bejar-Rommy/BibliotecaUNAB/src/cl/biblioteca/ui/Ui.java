package cl.biblioteca.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/** Utilidades compartidas por las ventanas: botones y mensajes de error uniformes. */
final class Ui {

    private Ui() {
        // Clase de utilidades: no se instancia.
    }

    /** Crea un botón con texto y acción. */
    static JButton boton(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        return boton;
    }

    /** Muestra un mensaje de error comprensible para el usuario. */
    static void error(Component padre, Exception e) {
        String mensaje = (e instanceof NumberFormatException)
                ? "Ingrese un número entero válido."
                : e.getMessage();
        JOptionPane.showMessageDialog(padre, mensaje, "Atención", JOptionPane.ERROR_MESSAGE);
    }
}
