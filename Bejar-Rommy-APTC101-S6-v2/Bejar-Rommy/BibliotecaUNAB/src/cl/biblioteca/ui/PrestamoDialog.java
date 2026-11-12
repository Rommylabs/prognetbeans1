package cl.biblioteca.ui;

import cl.biblioteca.modelo.Prestamo;
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

/** JDialog para registrar un préstamo. La fecha de inicio y la de devolución se calculan solas. */
final class PrestamoDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient BibliotecaServicio servicio;
    private final JTextField isbn = new JTextField(20);
    private final JTextField run = new JTextField(20);
    private final JTextField dias = new JTextField("10", 20);

    PrestamoDialog(Window propietario, BibliotecaServicio servicio) {
        super(propietario, "Nuevo préstamo", ModalityType.APPLICATION_MODAL);
        this.servicio = servicio;
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formulario.add(new JLabel("ISBN del libro"));
        formulario.add(isbn);
        formulario.add(new JLabel("RUN del usuario"));
        formulario.add(run);
        formulario.add(new JLabel("Días de préstamo (docente 20, estudiante 10)"));
        formulario.add(dias);
        setLayout(new BorderLayout());
        add(formulario, BorderLayout.CENTER);
        add(Ui.boton("Registrar préstamo", e -> registrar()), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(propietario);
    }

    private void registrar() {
        try {
            Prestamo prestamo = servicio.registrarPrestamo(isbn.getText(), run.getText(),
                    Integer.parseInt(dias.getText().trim()));
            dispose();
            new TarjetaDialog(getOwner(), "Tarjeta de préstamo", prestamo.generarTarjeta()).setVisible(true);
        } catch (IllegalArgumentException | IOException e) {
            Ui.error(this, e);
        }
    }
}
