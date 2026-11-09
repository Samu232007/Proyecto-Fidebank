import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Hilo del servidor que atiende a un cajero. Implementa Runnable, el servidor crea uno por cada conexión aceptada y lo ejecuta en su pool de hilos,
 * así se pueden atender varios cajeros al mismo tiempo. Lee el comando y sus datos, ejecuta la operación en SistemaCajero, responde y cierra la conexión.
 * Todos los errores de las operaciones llegan como ExcepcionInterna y se le responden al cajero con el código ERROR y el mensaje.
 */
public class HiloCliente implements Runnable {

    //Formato de la fecha y hora que se muestra en el registro del servidor (DateTimeFormatter es seguro de usar desde varios hilos)
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");

    private final Socket clienteSocket;
    private final int id;
    private final SistemaCajero sistema;
    private final Consumer<String> registro;

    public HiloCliente(Socket clienteSocket, int id, SistemaCajero sistema, Consumer<String> registro) {
        this.clienteSocket = clienteSocket;
        this.id = id;
        this.sistema = sistema;
        this.registro = registro;
    }

    /**
     * Atiende la petición: lee el comando y llama al método que lo procesa. Si el comando no se conoce responde ERROR.
     */
    @Override
    public void run() {
        registro.accept("Nueva petición aceptada [id: " + id + "] - " + fechaActual());

        // El socket y los flujos se cierran solos al terminar el try, así el cajero sabe que la respuesta terminó
        try (Socket socket = clienteSocket;
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // Si el cajero no envía nada se deja de esperar, para que el hilo no quede ocupado para siempre
            socket.setSoTimeout(Protocolo.TIEMPO_ESPERA);

            String comando = entrada.readLine();
            try {
                if (comando == null) {
                    throw new ExcepcionInterna("No se recibió ningún comando");
                }
                switch (comando) {
                    case Protocolo.ABRIR_CUENTA -> procesarAbrirCuenta(entrada, salida);
                    case Protocolo.INICIAR_SESION -> procesarIniciarSesion(entrada, salida);
                    case Protocolo.CONSULTAR_SALDO -> procesarConsultarSaldo(entrada, salida);
                    case Protocolo.RETIRAR -> procesarRetirar(entrada, salida);
                    case Protocolo.DEPOSITAR -> procesarDepositar(entrada, salida);
                    case Protocolo.VALIDAR_TRANSFERENCIA -> procesarValidarTransferencia(entrada, salida);
                    case Protocolo.TRANSFERIR -> procesarTransferir(entrada, salida);
                    case Protocolo.COMPROBANTE -> procesarComprobante(entrada, salida);
                    default -> throw new ExcepcionInterna("Comando desconocido");
                }
            } catch (ExcepcionInterna e) {
                registro.accept("Hilo [id: " + id + "] operación rechazada: " + e.getMessage());
                responder(salida, Protocolo.ERROR, e.getMessage());
            }
        } catch (IOException e) {
            registro.accept("Hilo [id: " + id + "] error de comunicación: " + e.getMessage());
        }

        registro.accept("Hilo [id: " + id + "] termina - " + fechaActual());
    }

    /**
     * Abrir cuenta: lee los datos del cliente y el PIN, y SistemaCajero crea la cuenta y guarda los datos en el archivo.
     */
    private void procesarAbrirCuenta(BufferedReader entrada, PrintWriter salida) throws IOException, ExcepcionInterna {
        String[] d = leer(entrada, 5);
        Cliente cliente = new Cliente(d[0].trim(), d[1].trim(), d[2].trim(), d[3].trim());

        Cuenta cuenta = sistema.abrirCuenta(cliente, d[4]);
        registro.accept("Hilo [id: " + id + "] cuenta creada " + cuenta.getNumeroCuenta() + " para " + cuenta.getTitular().nombreCompleto());
        responder(salida, Protocolo.OK, cuenta.getNumeroCuenta());
    }

    /**
     * Ingreso con PIN: devuelve el nombre completo del cliente y el saldo, nunca el PIN.
     */
    private void procesarIniciarSesion(BufferedReader entrada, PrintWriter salida) throws IOException, ExcepcionInterna {
        Cuenta cuenta = autenticar(entrada);
        registro.accept("Hilo [id: " + id + "] ingreso a la cuenta " + cuenta.getNumeroCuenta());
        responder(salida, Protocolo.OK, cuenta.getTitular().nombreCompleto(), String.valueOf(cuenta.getSaldo()));
    }

    private void procesarConsultarSaldo(BufferedReader entrada, PrintWriter salida) throws IOException, ExcepcionInterna {
        Cuenta cuenta = autenticar(entrada);
        responder(salida, Protocolo.OK, String.valueOf(cuenta.getSaldo()));
    }

    private void procesarRetirar(BufferedReader entrada, PrintWriter salida) throws IOException, ExcepcionInterna {
        Cuenta cuenta = autenticar(entrada);
        double monto = leerMonto(entrada);

        Transaccion t = sistema.retirar(cuenta, monto);
        responderTransaccion(salida, t);
    }

    private void procesarDepositar(BufferedReader entrada, PrintWriter salida) throws IOException, ExcepcionInterna {
        Cuenta cuenta = autenticar(entrada);
        double monto = leerMonto(entrada);

        Transaccion t = sistema.depositar(cuenta, monto);
        responderTransaccion(salida, t);
    }

    /**
     * Revisa la transferencia sin ejecutarla y devuelve el nombre del titular de la cuenta destino, para que el cajero se lo muestre al cliente.
     */
    private void procesarValidarTransferencia(BufferedReader entrada, PrintWriter salida) throws IOException, ExcepcionInterna {
        Cuenta origen = autenticar(entrada);
        String cuentaDestino = leer(entrada, 1)[0].trim();
        double monto = leerMonto(entrada);

        Cuenta destino = sistema.validarTransferencia(origen, cuentaDestino, monto);
        responder(salida, Protocolo.OK, destino.getTitular().nombreCompleto());
    }

    private void procesarTransferir(BufferedReader entrada, PrintWriter salida) throws IOException, ExcepcionInterna {
        Cuenta origen = autenticar(entrada);
        String cuentaDestino = leer(entrada, 1)[0].trim();
        double monto = leerMonto(entrada);

        Transaccion t = sistema.transferir(origen, cuentaDestino, monto);
        responderTransaccion(salida, t);
    }

    /**
     * Devuelve el texto del comprobante, una línea de la respuesta por cada línea del comprobante.
     * Solo se entrega si la transacción es de la cuenta que lo pide.
     */
    private void procesarComprobante(BufferedReader entrada, PrintWriter salida) throws IOException, ExcepcionInterna {
        Cuenta cuenta = autenticar(entrada);
        int idTransaccion;
        try {
            idTransaccion = Integer.parseInt(leer(entrada, 1)[0].trim());
        } catch (NumberFormatException e) {
            throw new ExcepcionInterna("No se encontró la transacción");
        }

        Transaccion t = sistema.buscarTransaccion(idTransaccion);
        if (!t.getCuentaOrigen().equals(cuenta.getNumeroCuenta())) {
            throw new ExcepcionInterna("No se encontró la transacción");
        }

        Comprobante comprobante = sistema.generarComprobante(t, cuenta);
        registro.accept("Hilo [id: " + id + "] comprobante de la transacción " + t.getIdTransaccion());
        responder(salida, Protocolo.OK, comprobante.generarTexto().split("\n"));
    }

    /**
     * Lee el número de cuenta y el PIN, y verifica que sean correctos. Todas las operaciones empiezan por aquí, porque el servidor no guarda sesiones.
     */
    private Cuenta autenticar(BufferedReader entrada) throws IOException, ExcepcionInterna {
        String[] d = leer(entrada, 2);
        return sistema.iniciarSesion(d[0].trim(), d[1]);
    }

    /**
     * Lee el monto como texto y lo convierte, si no es un número lanza ExcepcionInterna. La validación de si es mayor a cero la hace SistemaCajero.
     */
    private double leerMonto(BufferedReader entrada) throws IOException, ExcepcionInterna {
        try {
            return Double.parseDouble(leer(entrada, 1)[0].trim());
        } catch (NumberFormatException e) {
            throw new ExcepcionInterna("Monto no válido");
        }
    }

    /**
     * Lee la cantidad de líneas indicada. Si el cajero envió menos lanza ExcepcionInterna.
     */
    private String[] leer(BufferedReader entrada, int cantidad) throws IOException, ExcepcionInterna {
        String[] datos = new String[cantidad];
        for (int i = 0; i < cantidad; i++) {
            datos[i] = entrada.readLine();
            if (datos[i] == null) {
                throw new ExcepcionInterna("Faltan datos en la petición");
            }
        }
        return datos;
    }

    /**
     * Responde el resultado de un retiro, depósito o transferencia: número de transacción, monto y saldo resultante.
     * También deja en el registro del servidor la descripción de la transacción, que cada subclase de Transaccion arma a su manera.
     */
    private void responderTransaccion(PrintWriter salida, Transaccion t) {
        registro.accept("Hilo [id: " + id + "] " + t.descripcion());
        responder(salida, Protocolo.OK, String.valueOf(t.getIdTransaccion()), String.valueOf(t.getMonto()), String.valueOf(t.getSaldoResultante()));
    }

    /**
     * Envía la respuesta al cajero: primero el código y luego cada dato en una línea.
     */
    private void responder(PrintWriter salida, String codigo, String... datos) {
        salida.println(codigo);
        for (String dato : datos) {
            salida.println(dato);
        }
        salida.flush();
    }

    /**
     * Devuelve la fecha y hora actual con el formato del registro.
     */
    private String fechaActual() {
        return LocalDateTime.now().format(FORMATO_FECHA);
    }
}
