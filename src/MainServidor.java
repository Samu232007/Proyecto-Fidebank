import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Desarrollado por Samuel Mora Chaves, para el Avance 2 del Proyecto Final de la materia Programación Cliente/Servidor Concurrente, en la Universidad Fidélitas
 *
 * Clase principal de la aplicación SERVIDOR de FideBank (la aplicación del cajero se inicia con Main). Hay que ejecutarla antes de abrir los cajeros.
 *
 * Flujo del servidor:
 * 1. Crea el SistemaCajero y lee (deserializa) del archivo datos_cajero.dat los clientes, cuentas y transacciones guardados. Si el archivo no existe inicia vacío.
 * 2. Abre la ventana del servidor (VentanaServidor), que inicia el servidor (ServidorCajero) en el puerto definido en Protocolo.
 * 3. El servidor queda en escucha constante: por cada cajero que se conecta crea un hilo (HiloCliente) que atiende su petición, así varios cajeros
 *    pueden operar al mismo tiempo.
 * 4. El servidor es el único que lee y escribe el archivo de datos, los cajeros solo le envían peticiones.
 */
public class MainServidor {

    public static void main(String[] args) {

        SistemaCajero sistema = new SistemaCajero();

        // Si el archivo existe pero no se puede leer se avisa y no se inicia el servidor, para no sobrescribir los datos guardados con un sistema vacío
        try {
            sistema.cargarDatos();
        } catch (ExcepcionInterna e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() + "\nEl servidor no se iniciará para no perder los datos guardados.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Las ventanas de Swing se crean en el hilo de eventos
        SwingUtilities.invokeLater(() -> new VentanaServidor(sistema).setVisible(true));
    }
}
