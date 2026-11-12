package cl.biblioteca.ui;

import cl.biblioteca.modelo.Docente;
import cl.biblioteca.modelo.Estudiante;
import cl.biblioteca.modelo.Usuario;
import cl.biblioteca.servicio.BibliotecaServicio;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** JDialog para crear o editar un usuario (docente o estudiante). */
final class UsuarioDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient BibliotecaServicio servicio;
    private final boolean editando;

    private final JComboBox<String> tipo = new JComboBox<>(new String[] {"Estudiante", "Docente"});
    private final JTextField nombre = new JTextField(24);
    private final JTextField run = new JTextField(24);
    private final JComboBox<String> genero = new JComboBox<>(new String[] {"M", "F"});
    private final JTextField carrera = new JTextField(24);
    private final JTextField profesion = new JTextField(24);
    private final JComboBox<String> grados =
            new JComboBox<>(new String[] {"Magíster", "Doctor", "Magíster y Doctor"});

    /**
     * @param propietario ventana desde la que se abre
     * @param servicio    casos de uso de la biblioteca
     * @param usuario     usuario a editar, o null para crear uno nuevo
     */
    UsuarioDialog(Window propietario, BibliotecaServicio servicio, Usuario usuario) {
        super(propietario, usuario == null ? "Nuevo usuario" : "Editar usuario", ModalityType.APPLICATION_MODAL);
        this.servicio = servicio;
        this.editando = (usuario != null);
        construir();
        if (editando) {
            cargar(usuario);
        }
        alternarCamposDocente();
        pack();
        setLocationRelativeTo(propietario);
    }

    private void construir() {
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        agregar(formulario, "Tipo de usuario", tipo);
        agregar(formulario, "Nombre completo", nombre);
        agregar(formulario, "RUN (con dígito verificador)", run);
        agregar(formulario, "Género", genero);
        agregar(formulario, "Carrera", carrera);
        agregar(formulario, "Profesión (solo docentes)", profesion);
        agregar(formulario, "Grados (solo docentes)", grados);
        tipo.addActionListener(e -> alternarCamposDocente());
        setLayout(new BorderLayout());
        add(formulario, BorderLayout.CENTER);
        add(Ui.boton("Guardar", e -> guardar()), BorderLayout.SOUTH);
    }

    private static void agregar(JPanel panel, String etiqueta, java.awt.Component campo) {
        panel.add(new JLabel(etiqueta));
        panel.add(campo);
    }

    private void cargar(Usuario usuario) {
        tipo.setSelectedItem(usuario.getTipo());
        nombre.setText(usuario.getNombreCompleto());
        run.setText(usuario.getRun());
        run.setEditable(false);
        genero.setSelectedItem(String.valueOf(usuario.getGenero()));
        carrera.setText(usuario.getCarrera());
        if (usuario instanceof Docente) {
            profesion.setText(((Docente) usuario).getProfesion());
            grados.setSelectedItem(((Docente) usuario).getGrados());
        }
    }

    private boolean esDocente() {
        return "Docente".equals(tipo.getSelectedItem());
    }

    private void alternarCamposDocente() {
        profesion.setEnabled(esDocente());
        grados.setEnabled(esDocente());
    }

    /** Construye el objeto con su constructor (que valida) y se lo entrega al servicio. */
    private void guardar() {
        try {
            char sexo = String.valueOf(genero.getSelectedItem()).charAt(0);
            Usuario usuario;
            if (esDocente()) {
                usuario = new Docente(nombre.getText(), run.getText(), sexo, carrera.getText(),
                        profesion.getText(), String.valueOf(grados.getSelectedItem()));
            } else {
                usuario = new Estudiante(nombre.getText(), run.getText(), sexo, carrera.getText());
            }
            if (editando) {
                servicio.editarUsuario(usuario);
            } else {
                servicio.ingresarUsuario(usuario);
            }
            dispose();
        } catch (IllegalArgumentException | IOException e) {
            Ui.error(this, e);
        }
    }
}
