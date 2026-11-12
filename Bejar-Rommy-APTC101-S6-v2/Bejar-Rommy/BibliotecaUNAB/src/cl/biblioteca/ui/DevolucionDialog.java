package cl.biblioteca.ui;

import cl.biblioteca.modelo.Devolucion;
import cl.biblioteca.servicio.BibliotecaServicio;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** JDialog para registrar la devolución de un libro y mostrar la multa, si corresponde. */
final class DevolucionDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient BibliotecaServicio servicio;
    private final JTextField isbn = new JTextField(20);
    private final JTextField run = new JTextField(20);

    DevolucionDialog(Window propietario, BibliotecaServicio servicio) {
        super(propietario, "Registrar devolución", ModalityType.APPLICATION_MODAL);
        this.servicio = servicio;
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formulario.add(new JLabel("ISBN del libro"));
        formulario.add(isbn);
        formulario.add(new JLabel("RUN del usuario"));
        formulario.add(run);
        setLayout(new BorderLayout());
        add(formulario, BorderLayout.CENTER);
        add(Ui.boton("Registrar devolución", e -> registrar()), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(propietario);
    }

    private void registrar() {
        try {
            Devolucion devolucion = servicio.registrarDevolucion(isbn.getText(), run.getText());
            dispose();
            new TarjetaDialog(getOwner(), "Devolución registrada", devolucion.generarComprobante()).setVisible(true);
        } catch (IllegalArgumentException | IOException e) {
            Ui.error(this, e);
        }
    }
}
