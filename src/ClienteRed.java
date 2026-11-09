import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Se encarga de la comunicación del cajero (cliente) con el servidor. Por cada petición abre un socket, envía el comando y sus datos, lee la respuesta
 * y cierra el socket. Las ventanas usan esta clase para no manejar sockets directamente.
 * Estos métodos esperan la respuesta del servidor, por eso las ventanas los llaman desde un hilo aparte (SwingWorker) y no desde el hilo de eventos.
 * Si el servidor responde con ERROR se lanza ExcepcionInterna con su mensaje, si no se puede hablar con el servidor se lanza IOException.
 */
public class ClienteRed {

    /**
     * Pide al servidor crear la cuenta y devuelve el número de cuenta que se le asignó.
     */
    public static String abrirCuenta(Cliente cliente, String pin) throws ExcepcionInterna, IOException {
        ArrayList<String> respuesta = enviar(Protocolo.ABRIR_CUENTA,
                cliente.getCedula(), cliente.getNombre(), cliente.getApellidos(), cliente.getTelefono(), pin);
        return dato(respuesta, 1);
    }

    /**
     * Envía el número de cuenta y el PIN. Es el servidor quien los verifica en sus datos, si son correctos devuelve la sesión con el nombre del cliente.
     */
    public static SesionCliente iniciarSesion(String numeroCuenta, String pin) throws ExcepcionInterna, IOException {
        ArrayList<String> respuesta = enviar(Protocolo.INICIAR_SESION, numeroCuenta, pin);
        return new SesionCliente(numeroCuenta, pin, dato(respuesta, 1));
    }

    /**
     * Consulta el saldo actual de la cuenta, se pide al servidor cada vez porque otro cliente pudo haberle transferido dinero.
     */
    public static double consultarSaldo(SesionCliente sesion) throws ExcepcionInterna, IOException {
        ArrayList<String> respuesta = enviar(Protocolo.CONSULTAR_SALDO, sesion.getNumeroCuenta(), sesion.getPin());
        return Double.parseDouble(dato(respuesta, 1));
    }

    public static ResultadoOperacion retirar(SesionCliente sesion, double monto) throws ExcepcionInterna, IOException {
        return leerResultado(enviar(Protocolo.RETIRAR, sesion.getNumeroCuenta(), sesion.getPin(), String.valueOf(monto)));
    }

    public static ResultadoOperacion depositar(SesionCliente sesion, double monto) throws ExcepcionInterna, IOException {
        return leerResultado(enviar(Protocolo.DEPOSITAR, sesion.getNumeroCuenta(), sesion.getPin(), String.valueOf(monto)));
    }

    /**
     * Le pide al servidor que revise la transferencia sin ejecutarla. Devuelve el nombre del titular de la cuenta destino para mostrarlo en la confirmación.
     */
    public static String validarTransferencia(SesionCliente sesion, String cuentaDestino, double monto) throws ExcepcionInterna, IOException {
        ArrayList<String> respuesta = enviar(Protocolo.VALIDAR_TRANSFERENCIA,
                sesion.getNumeroCuenta(), sesion.getPin(), cuentaDestino, String.valueOf(monto));
        return dato(respuesta, 1);
    }

    public static ResultadoOperacion transferir(SesionCliente sesion, String cuentaDestino, double monto) throws ExcepcionInterna, IOException {
        return leerResultado(enviar(Protocolo.TRANSFERIR,
                sesion.getNumeroCuenta(), sesion.getPin(), cuentaDestino, String.valueOf(monto)));
    }

    /**
     * Pide el texto del comprobante de una transacción. Todas las líneas después del código forman el texto.
     */
    public static String obtenerComprobante(SesionCliente sesion, int idTransaccion) throws ExcepcionInterna, IOException {
        ArrayList<String> respuesta = enviar(Protocolo.COMPROBANTE,
                sesion.getNumeroCuenta(), sesion.getPin(), String.valueOf(idTransaccion));
        return String.join("\n", respuesta.subList(1, respuesta.size()));
    }

    /**
     * Convierte la respuesta de un retiro, depósito o transferencia: número de transacción, monto y nuevo saldo.
     */
    private static ResultadoOperacion leerResultado(ArrayList<String> respuesta) throws IOException {
        return new ResultadoOperacion(Integer.parseInt(dato(respuesta, 1)),
                Double.parseDouble(dato(respuesta, 2)), Double.parseDouble(dato(respuesta, 3)));
    }

    /**
     * Abre la conexión con el servidor, envía cada dato en una línea y devuelve todas las líneas de la respuesta (la primera es el código).
     * El servidor cierra la conexión al terminar de responder, por eso se lee hasta que no haya más líneas.
     * Si el código es ERROR lanza ExcepcionInterna con el mensaje del servidor.
     */
    private static ArrayList<String> enviar(String... lineas) throws ExcepcionInterna, IOException {
        ArrayList<String> respuesta = new ArrayList<>();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(Protocolo.HOST, Protocolo.PUERTO), Protocolo.TIEMPO_ESPERA);
            socket.setSoTimeout(Protocolo.TIEMPO_ESPERA);

            PrintWriter salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            for (String linea : lineas) {
                salida.println(linea);
            }

            String linea;
            while ((linea = entrada.readLine()) != null) {
                respuesta.add(linea);
            }
        } catch (java.net.ConnectException | java.net.SocketTimeoutException e) {
            // Mensaje claro para el cliente cuando el servidor no está encendido o no responde
            throw new IOException("No se pudo conectar con el servidor. Verifique que la aplicación servidor esté en ejecución.", e);
        }

        if (respuesta.isEmpty()) {
            throw new IOException("El servidor cerró la conexión sin responder.");
        }
        if (respuesta.get(0).equals(Protocolo.ERROR)) {
            throw new ExcepcionInterna(respuesta.size() > 1 ? respuesta.get(1) : "No se pudo realizar la operación");
        }
        if (!respuesta.get(0).equals(Protocolo.OK)) {
            throw new IOException("Respuesta inesperada del servidor: " + respuesta.get(0));
        }
        return respuesta;
    }

    /**
     * Devuelve la línea de la respuesta en esa posición, si el servidor no la envió lanza IOException.
     */
    private static String dato(ArrayList<String> respuesta, int posicion) throws IOException {
        if (respuesta.size() <= posicion) {
            throw new IOException("La respuesta del servidor está incompleta.");
        }
        return respuesta.get(posicion);
    }
}
