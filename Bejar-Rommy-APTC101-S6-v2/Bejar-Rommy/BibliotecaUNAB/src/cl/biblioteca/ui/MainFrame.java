package cl.biblioteca.ui;

import cl.biblioteca.modelo.Docente;
import cl.biblioteca.modelo.Libro;
import cl.biblioteca.modelo.Prestamo;
import cl.biblioteca.modelo.Usuario;
import cl.biblioteca.servicio.BibliotecaServicio;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana principal (JFrame). Muestra el menú de inicio (home) y los módulos de usuarios, libros
 * y préstamos/devoluciones. Los formularios se abren como JDialog. El botón "Volver al inicio"
 * está siempre visible, de modo que el home es accesible en todo momento.
 */
public final class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int PESTANA_INICIO = 0;

    private final transient BibliotecaServicio servicio;
    private final JTabbedPane pestanas = new JTabbedPane();
    private final DefaultTableModel modeloUsuarios = crearModelo(
            "Tipo", "Nombre", "RUN", "Género", "Carrera", "Préstamo", "Profesión", "Grados");
    private final DefaultTableModel modeloLibros = crearModelo(
            "ISBN", "Título", "Autor", "En biblioteca", "Disponibles", "Imagen");
    private final DefaultTableModel modeloPrestamos = crearModelo(
            "RUN", "Usuario", "ISBN", "Libro", "Inicio", "Vencimiento", "Días");

    /** @param servicio casos de uso de la biblioteca (asociación: la ventana usa al servicio) */
    public MainFrame(BibliotecaServicio servicio) {
        super("Biblioteca UNAB - Sede Antonio Varas");
        this.servicio = servicio;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("  BIBLIOTECA UNAB - SISTEMA DE GESTIÓN", SwingConstants.LEFT);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 19f));
        titulo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(titulo, BorderLayout.NORTH);

        pestanas.addTab("Inicio", crearInicio());
        pestanas.addTab("Usuarios", crearPanelUsuarios());
        pestanas.addTab("Libros", crearPanelLibros());
        pestanas.addTab("Préstamos y devoluciones", crearPanelPrestamos());
        add(pestanas, BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pie.add(Ui.boton("Volver al inicio", e -> pestanas.setSelectedIndex(PESTANA_INICIO)));
        add(pie, BorderLayout.SOUTH);
        refrescar();
    }

    private static DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    // ------------------------------------------------------------------ paneles

    private JPanel crearInicio() {
        JPanel centro = new JPanel(new GridLayout(0, 1, 10, 10));
        JLabel descripcion = new JLabel("Seleccione un módulo para administrar la biblioteca", SwingConstants.CENTER);
        descripcion.setFont(descripcion.getFont().deriveFont(17f));
        centro.add(descripcion);
        centro.add(botonDeModulo("Administrar usuarios", 1));
        centro.add(botonDeModulo("Administrar libros", 2));
        centro.add(botonDeModulo("Registrar préstamos y devoluciones", 3));
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(centro);
        return panel;
    }

    private JButton botonDeModulo(String texto, int pestana) {
        return Ui.boton(texto, e -> pestanas.setSelectedIndex(pestana));
    }

    private JPanel crearPanelUsuarios() {
        JTable tabla = new JTable(modeloUsuarios);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(Ui.boton("Crear usuario", e -> abrir(new UsuarioDialog(this, servicio, null))));
        barra.add(Ui.boton("Editar seleccionado", e -> {
            String run = valorSeleccionado(tabla, 2, "un usuario");
            if (run != null) {
                abrir(new UsuarioDialog(this, servicio, servicio.buscarUsuario(run)));
            }
        }));
        barra.add(Ui.boton("Eliminar seleccionado", e -> {
            String run = valorSeleccionado(tabla, 2, "un usuario");
            if (run != null && confirmar("¿Eliminar al usuario " + run + "?")) {
                ejecutar(() -> servicio.eliminarUsuario(run));
            }
        }));
        return crearPanel(tabla, barra);
    }

    private JPanel crearPanelLibros() {
        JTable tabla = new JTable(modeloLibros);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(Ui.boton("Crear libro", e -> abrir(new LibroDialog(this, servicio))));
        barra.add(Ui.boton("Eliminar seleccionado", e -> {
            String isbn = valorSeleccionado(tabla, 0, "un libro");
            if (isbn != null && confirmar("¿Eliminar el libro " + isbn + "?")) {
                ejecutar(() -> servicio.eliminarLibro(isbn));
            }
        }));
        return crearPanel(tabla, barra);
    }

    private JPanel crearPanelPrestamos() {
        JTable tabla = new JTable(modeloPrestamos);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(Ui.boton("Nuevo préstamo", e -> abrir(new PrestamoDialog(this, servicio))));
        barra.add(Ui.boton("Registrar devolución", e -> abrir(new DevolucionDialog(this, servicio))));
        barra.add(Ui.boton("Ver tarjeta del préstamo", e -> {
            String run = valorSeleccionado(tabla, 0, "un préstamo");
            if (run != null) {
                Prestamo prestamo = servicio.buscarPrestamoDe(servicio.buscarUsuario(run));
                abrir(new TarjetaDialog(this, "Tarjeta de préstamo", prestamo.generarTarjeta()));
            }
        }));
        return crearPanel(tabla, barra);
    }

    private static JPanel crearPanel(JTable tabla, JPanel barra) {
        tabla.setAutoCreateRowSorter(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(barra, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------------------------------------------ acciones

    /** Operación del servicio que puede fallar por una regla de negocio o por el almacenamiento. */
    private interface Operacion {
        void ejecutar() throws Exception;
    }

    /** Abre un diálogo modal y, al cerrarse, actualiza las tablas con los cambios guardados. */
    private void abrir(java.awt.Window dialogo) {
        dialogo.setVisible(true);
        refrescar();
    }

    private void ejecutar(Operacion operacion) {
        try {
            operacion.ejecutar();
            refrescar();
        } catch (Exception e) {
            Ui.error(this, e);
        }
    }

    private boolean confirmar(String pregunta) {
        return JOptionPane.showConfirmDialog(this, pregunta, "Confirmación", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /** Devuelve el valor de una columna en la fila seleccionada, o avisa si no hay selección. */
    private String valorSeleccionado(JTable tabla, int columna, String que) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione " + que + " de la tabla.", "Atención",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return String.valueOf(tabla.getValueAt(fila, columna));
    }

    /** Vuelve a llenar las tres tablas con los datos actuales del servicio. */
    private void refrescar() {
        modeloUsuarios.setRowCount(0);
        for (Usuario u : servicio.getUsuarios()) {
            boolean docente = u instanceof Docente;
            modeloUsuarios.addRow(new Object[] {u.getTipo(), u.getNombreCompleto(), u.getRun(), u.getGenero(),
                u.getCarrera(), u.getPrestamo(), docente ? ((Docente) u).getProfesion() : "",
                docente ? ((Docente) u).getGrados() : ""});
        }
        modeloLibros.setRowCount(0);
        for (Libro l : servicio.getLibros()) {
            modeloLibros.addRow(new Object[] {l.getIsbn(), l.getTitulo(), l.getAutor(),
                l.getCantidadBiblioteca(), l.getCantidadDisponible(), l.getImagen()});
        }
        modeloPrestamos.setRowCount(0);
        for (Prestamo p : servicio.getPrestamos()) {
            modeloPrestamos.addRow(new Object[] {p.getUsuario().getRun(), p.getUsuario().getNombreCompleto(),
                p.getLibro().getIsbn(), p.getLibro().getTitulo(), p.getFechaInicio(),
                p.getFechaVencimiento(), p.getDias()});
        }
    }
}
