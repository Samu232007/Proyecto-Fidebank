import javax.swing.SwingUtilities;

/**
 * Desarrollado por Samuel Mora Chaves, para el Avance 2 del Proyecto Final de la materia Programación Cliente/Servidor Concurrente, en la Universidad Fidélitas
 *
 * CÓMO INICIAR EL AMBIENTE DE CLIENTE/SERVIDOR:
 * -Ejecute primero el archivo MainServidor para iniciar el servidor (Si NetBeans aún no tiene acceso a redes públicas y privadas es posible que se solicite el permiso).
 * -Posteriormente, puede ejecutar el archivo Main para abrir el cajero automático.
 * -Adicionalmente, se pueden abrir varios cajeros al mismo tiempo, el servidor los atiende cada uno en su propio hilo.
 *
 * Flujo del programa:
 * 1. Abre la pantalla de inicio (VentanaInicio) con las opciones Ingresar, Abrir cuenta y Salir. El cajero no guarda datos, solo se comunica con el servidor.
 * 2. Abrir cuenta (VentanaApertura) envía los datos al servidor, que crea la cuenta con un número de 10 dígitos y la guarda en el archivo.
 * 3. Ingresar (VentanaIngreso) envía el número de cuenta y el PIN al servidor, que los busca en sus datos. Si son correctos se abre el menú principal (VentanaMenu).
 * 4. Desde el menú se puede retirar (VentanaRetiro), depositar (VentanaDeposito) y transferir (VentanaTransferencia). Cada transacción exitosa se registra en el servidor
 *    y muestra el aviso de VentanaTransaccionExitosa, desde donde se puede ver el comprobante (VentanaComprobante).
 */
public class Main {

    public static void main(String[] args) {

        // Las ventanas de Swing se crean en el hilo de eventos
        SwingUtilities.invokeLater(() -> new VentanaInicio().setVisible(true));
    }
}
