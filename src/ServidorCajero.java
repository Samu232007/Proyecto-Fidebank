import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Servidor de FideBank. Queda en escucha constante de las peticiones de varios cajeros: un hilo propio espera conexiones con ServerSocket.accept()
 * y por cada cliente que se conecta crea un HiloCliente que se ejecuta en un pool de hilos. Así el servidor puede atender a un cajero mientras
 * sigue esperando a los demás. VentanaServidor usa esta clase para iniciarlo, detenerlo y mostrar su estado.
 */
public class ServidorCajero {

    //Sistema con los clientes, cuentas y transacciones, es el mismo para todos los hilos
    private final SistemaCajero sistema;

    //Método con el que el servidor escribe sus mensajes (la ventana del servidor los muestra en su registro)
    private final Consumer<String> registro;

    //Contador para darle un identificador a cada hilo (AtomicInteger porque lo usa el hilo que escucha y puede leerse desde otros)
    private final AtomicInteger idHilo = new AtomicInteger(0);

    private ServerSocket serverSocket;
    private ThreadPoolExecutor pool;
    private Thread hiloEscucha;
    private volatile boolean ejecutando = false;

    public ServidorCajero(SistemaCajero sistema, Consumer<String> registro) {
        this.sistema = sistema;
        this.registro = registro;
    }

    /**
     * Abre el puerto del servidor, crea el pool de hilos y arranca el hilo que escucha las conexiones. Si el puerto ya está en uso lanza IOException.
     * Si el servidor ya estaba iniciado no hace nada.
     */
    public synchronized void iniciar() throws IOException {
        if (ejecutando) {
            return;
        }
        serverSocket = new ServerSocket(Protocolo.PUERTO);
        pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        ejecutando = true;

        hiloEscucha = new Thread(this::escuchar, "Servidor-escucha");
        hiloEscucha.start();
        registro.accept("Servidor iniciado en el puerto " + Protocolo.PUERTO);
    }

    /**
     * Ciclo del hilo que escucha: espera a que un cajero se conecte y le entrega la conexión a un HiloCliente del pool.
     * Cuando el servidor se detiene se cierra el ServerSocket, accept() lanza una excepción y el ciclo termina.
     */
    private void escuchar() {
        while (ejecutando) {
            try {
                Socket clienteSocket = serverSocket.accept();
                try {
                    pool.submit(new HiloCliente(clienteSocket, idHilo.incrementAndGet(), sistema, registro));
                } catch (RejectedExecutionException e) {
                    // El servidor se detuvo justo cuando llegó este cliente, se cierra su conexión
                    clienteSocket.close();
                }
            } catch (IOException e) {
                // Si el servidor sigue activo es un error real, si no es el cierre normal del ServerSocket al detener
                if (ejecutando) {
                    registro.accept("Error al aceptar una conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Detiene el servidor: cierra el ServerSocket (con lo que el hilo que escucha termina) y cierra el pool, dejando que los hilos que ya
     * estaban atendiendo a un cliente terminen su trabajo. Si el servidor ya estaba detenido no hace nada.
     */
    public synchronized void detener() {
        if (!ejecutando) {
            return;
        }
        ejecutando = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            registro.accept("Error al cerrar el puerto: " + e.getMessage());
        }
        pool.shutdown();
        registro.accept("Servidor detenido");
    }

    public boolean estaEjecutando() {
        return ejecutando;
    }

    /**
     * Cantidad de hilos que en este momento están atendiendo a un cliente.
     */
    public int getHilosActivos() {
        ThreadPoolExecutor actual = pool;
        return actual == null ? 0 : actual.getActiveCount();
    }

    /**
     * Cantidad total de hilos (peticiones) que se han generado desde que se inició el servidor.
     */
    public long getHilosGenerados() {
        ThreadPoolExecutor actual = pool;
        return actual == null ? 0 : actual.getTaskCount();
    }
}
