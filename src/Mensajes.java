import java.awt.Component;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;

/**
 * Mensajes que repiten varias ventanas del cajero. Así cada ventana no repite los mismos mensajes.
 */
public class Mensajes {

    /**
     * Muestra el error que ocurrió en un SwingWorker. Si fue una ExcepcionInterna se muestra su mensaje tal cual (saldo insuficiente, monto no válido, etc.),
     * si fue otro error es que no se pudo hablar con el servidor.
     */
    public static void mostrarError(Component padre, ExecutionException e, String titulo) {
        if (e.getCause() instanceof ExcepcionInterna) {
            JOptionPane.showMessageDialog(padre, e.getCause().getMessage(), titulo, JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(padre, "No se pudo comunicar con el servidor: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Lee el monto escrito por el cliente. Si no es válido muestra el mensaje "Monto no válido" y devuelve null para que la ventana
     * se quede abierta y el cliente lo corrija.
     */
    public static Double leerMonto(Component padre, String texto, String titulo) {
        try {
            return Formato.leerMonto(texto);
        } catch (ExcepcionInterna e) {
            JOptionPane.showMessageDialog(padre, e.getMessage(), titulo, JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }
}
