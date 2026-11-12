package cl.biblioteca.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.print.PrinterException;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** JDialog que muestra un texto formateado (tarjeta de préstamo o comprobante) y permite imprimirlo. */
final class TarjetaDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextArea area;

    TarjetaDialog(Window propietario, String titulo, String texto) {
        super(propietario, titulo, ModalityType.APPLICATION_MODAL);
        area = new JTextArea(texto, 16, 44);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(Ui.boton("Imprimir", e -> imprimir()));
        botones.add(Ui.boton("Cerrar", e -> dispose()));
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(propietario);
    }

    private void imprimir() {
        try {
            area.print();
        } catch (PrinterException e) {
            Ui.error(this, e);
        }
    }
}
