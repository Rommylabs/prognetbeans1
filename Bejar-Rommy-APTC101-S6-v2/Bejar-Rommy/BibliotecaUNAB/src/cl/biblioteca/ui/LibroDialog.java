package cl.biblioteca.ui;

import cl.biblioteca.modelo.Libro;
import cl.biblioteca.servicio.BibliotecaServicio;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/** JDialog para registrar un libro nuevo en el catálogo. */
final class LibroDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient BibliotecaServicio servicio;
    private final JTextField isbn = new JTextField(24);
    private final JTextField titulo = new JTextField(24);
    private final JTextField autor = new JTextField(24);
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
    private final JSpinner disponible = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
    private final JTextField imagen = new JTextField(24);

    LibroDialog(Window propietario, BibliotecaServicio servicio) {
        super(propietario, "Registrar libro", ModalityType.APPLICATION_MODAL);
        this.servicio = servicio;
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        agregar(formulario, "ISBN", isbn);
        agregar(formulario, "Título", titulo);
        agregar(formulario, "Autor", autor);
        agregar(formulario, "Cantidad en biblioteca", cantidad);
        agregar(formulario, "Cantidad disponible para préstamo", disponible);
        agregar(formulario, "Imagen (opcional)", imagen);
        formulario.add(new JLabel(""));
        formulario.add(Ui.boton("Elegir imagen...", e -> elegirImagen()));
        setLayout(new BorderLayout());
        add(formulario, BorderLayout.CENTER);
        add(Ui.boton("Guardar libro", e -> guardar()), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(propietario);
    }

    private static void agregar(JPanel panel, String etiqueta, java.awt.Component campo) {
        panel.add(new JLabel(etiqueta));
        panel.add(campo);
    }

    private void elegirImagen() {
        JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imagen.setText(selector.getSelectedFile().getAbsolutePath());
        }
    }

    /** Construye el libro con su constructor (que valida) y se lo entrega al servicio. */
    private void guardar() {
        try {
            Libro libro = new Libro(isbn.getText(), titulo.getText(), autor.getText(),
                    (Integer) cantidad.getValue(), (Integer) disponible.getValue(), imagen.getText());
            servicio.ingresarLibro(libro);
            dispose();
        } catch (IllegalArgumentException | IOException e) {
            Ui.error(this, e);
        }
    }
}
