import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Guarda los datos del sistema (clientes, cuentas y transacciones) y ejecuta las operaciones del cajero. Solo la aplicación servidor usa esta clase,
 * así el archivo de datos lo lee y lo escribe un único lugar. Como el servidor atiende a varios clientes al mismo tiempo (un hilo por cliente),
 * todos los métodos públicos son sincronizados: así dos hilos nunca modifican las colecciones o el archivo a la vez.
 * Los datos se guardan (serializan) en el archivo datos_cajero.dat después de cada operación, así no se pierden al cerrar el programa.
 */
public class SistemaCajero {

    //Nombre del archivo donde se guardan los datos (se crea en la carpeta desde donde se ejecuta el programa)
    public static final String ARCHIVO = "datos_cajero.dat";

    private ArrayList<Cliente> clientes = new ArrayList<>();
    private ArrayList<Cuenta> cuentas = new ArrayList<>();
    private ArrayList<Transaccion> transacciones = new ArrayList<>();

    //Último número de transacción que se asignó, la siguiente transacción usa este valor más uno
    private int ultimoIdTransaccion = 0;

    private final SecureRandom aleatorio = new SecureRandom();

    /**
     * Crea una cuenta nueva con saldo de cero. Valida los datos del cliente y que el PIN sea de 4 dígitos, si algo no es válido lanza ExcepcionInterna.
     * Si la cédula ya pertenece a un cliente registrado, la cuenta nueva se asocia a ese cliente en lugar de crear otro.
     */
    public synchronized Cuenta abrirCuenta(Cliente cliente, String pin) throws ExcepcionInterna {
        if (cliente == null || !Formato.esNumero(cliente.getCedula()) || !Formato.esNumero(cliente.getTelefono())
                || vacio(cliente.getNombre()) || vacio(cliente.getApellidos())
                || pin == null || !pin.matches("\\d{4}")) {
            throw new ExcepcionInterna("Revise los datos ingresados");
        }

        Cliente titular = buscarCliente(cliente.getCedula());
        if (titular == null) {
            titular = cliente;
            clientes.add(titular);
        }

        Cuenta cuenta = new Cuenta(generarNumeroCuenta(), titular, pin);
        cuentas.add(cuenta);
        guardarDatos();
        return cuenta;
    }

    /**
     * Busca la cuenta y verifica el PIN. El mensaje es el mismo si falla la cuenta o el PIN, para no dar pistas de cuál de los dos es el incorrecto.
     */
    public synchronized Cuenta iniciarSesion(String numeroCuenta, String pin) throws ExcepcionInterna {
        Cuenta cuenta = buscarCuenta(numeroCuenta);
        if (cuenta == null || !cuenta.verificarPin(pin)) {
            throw new ExcepcionInterna("Número de cuenta o PIN incorrecto");
        }
        return cuenta;
    }

    /**
     * Devuelve la cuenta con ese número, o null si no existe.
     */
    public synchronized Cuenta buscarCuenta(String numeroCuenta) {
        for (Cuenta c : cuentas) {
            if (c.getNumeroCuenta().equals(numeroCuenta)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Retira dinero de la cuenta y registra la transacción. Si el monto no es válido o el saldo no alcanza lanza ExcepcionInterna y no se registra nada.
     */
    public synchronized Transaccion retirar(Cuenta cuenta, double monto) throws ExcepcionInterna {
        double montoValido = validarMonto(monto);
        cuenta.retirar(montoValido);

        Transaccion t = new Retiro(++ultimoIdTransaccion, montoValido, cuenta.getNumeroCuenta(), cuenta.getSaldo());
        transacciones.add(t);
        guardarDatos();
        return t;
    }

    /**
     * Deposita dinero en la cuenta y registra la transacción. Si el monto no es válido lanza ExcepcionInterna.
     */
    public synchronized Transaccion depositar(Cuenta cuenta, double monto) throws ExcepcionInterna {
        double montoValido = validarMonto(monto);
        cuenta.depositar(montoValido);

        Transaccion t = new Deposito(++ultimoIdTransaccion, montoValido, cuenta.getNumeroCuenta(), cuenta.getSaldo());
        transacciones.add(t);
        guardarDatos();
        return t;
    }

    /**
     * Revisa que una transferencia se pueda hacer, sin ejecutarla, y devuelve la cuenta destino. Se usa para mostrarle al cliente el titular
     * de la cuenta destino antes de preguntarle si desea continuar. El orden de las validaciones es: cuenta destino, monto y saldo.
     */
    public synchronized Cuenta validarTransferencia(Cuenta origen, String cuentaDestino, double monto) throws ExcepcionInterna {
        Cuenta destino = buscarCuenta(cuentaDestino);
        if (destino == null || destino == origen) {
            throw new ExcepcionInterna("Cuenta destino no válida");
        }
        double montoValido = validarMonto(monto);
        if (montoValido > origen.getSaldo()) {
            throw new ExcepcionInterna("Saldo insuficiente");
        }
        return destino;
    }

    /**
     * Transfiere dinero de la cuenta de origen a la cuenta destino y registra la transacción. Si alguna validación falla lanza ExcepcionInterna
     * y la transferencia no se realiza ni se registra.
     */
    public synchronized Transaccion transferir(Cuenta origen, String cuentaDestino, double monto) throws ExcepcionInterna {
        Cuenta destino = validarTransferencia(origen, cuentaDestino, monto);
        double montoValido = Formato.redondear(monto);

        origen.retirar(montoValido);
        destino.depositar(montoValido);

        Transaccion t = new Transferencia(++ultimoIdTransaccion, montoValido, origen.getNumeroCuenta(), destino.getNumeroCuenta(), origen.getSaldo());
        transacciones.add(t);
        guardarDatos();
        return t;
    }

    /**
     * Busca una transacción por su número, si no existe lanza ExcepcionInterna.
     */
    public synchronized Transaccion buscarTransaccion(int idTransaccion) throws ExcepcionInterna {
        for (Transaccion t : transacciones) {
            if (t.getIdTransaccion() == idTransaccion) {
                return t;
            }
        }
        throw new ExcepcionInterna("No se encontró la transacción");
    }

    /**
     * Arma el comprobante de una transacción con los datos del titular de la cuenta.
     */
    public synchronized Comprobante generarComprobante(Transaccion transaccion, Cuenta cuenta) {
        return new Comprobante(transaccion, cuenta.getTitular());
    }

    /**
     * Lee (deserializa) los clientes, cuentas y transacciones guardados en el archivo. Si el archivo todavía no existe el sistema inicia vacío.
     * Las tres colecciones se guardan en el mismo flujo, por eso al leerlas cada cuenta sigue apuntando al mismo objeto Cliente que está en la colección de clientes.
     */
    @SuppressWarnings("unchecked")
    public synchronized void cargarDatos() throws ExcepcionInterna {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            return;
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            clientes = (ArrayList<Cliente>) entrada.readObject();
            cuentas = (ArrayList<Cuenta>) entrada.readObject();
            transacciones = (ArrayList<Transaccion>) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ExcepcionInterna("No se pudo leer el archivo de datos: " + e.getMessage());
        }

        // El siguiente número de transacción continúa después del más alto que ya está guardado
        ultimoIdTransaccion = 0;
        for (Transaccion t : transacciones) {
            ultimoIdTransaccion = Math.max(ultimoIdTransaccion, t.getIdTransaccion());
        }
    }

    /**
     * Guarda (serializa) los clientes, cuentas y transacciones en el archivo, reemplazando lo que hubiera antes.
     */
    public synchronized void guardarDatos() throws ExcepcionInterna {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ARCHIVO))) {
            salida.writeObject(clientes);
            salida.writeObject(cuentas);
            salida.writeObject(transacciones);
        } catch (IOException e) {
            throw new ExcepcionInterna("No se pudieron guardar los datos: " + e.getMessage());
        }
    }

    /**
     * Cantidad de cuentas registradas, la ventana del servidor la usa para mostrar el estado.
     */
    public synchronized int cantidadCuentas() {
        return cuentas.size();
    }

    /**
     * Revisa que el monto sea mayor a cero y lo devuelve redondeado a dos decimales. Si no es válido lanza ExcepcionInterna.
     */
    private double validarMonto(double monto) throws ExcepcionInterna {
        if (Double.isNaN(monto) || Double.isInfinite(monto) || Formato.redondear(monto) <= 0) {
            throw new ExcepcionInterna("Monto no válido");
        }
        return Formato.redondear(monto);
    }

    /**
     * Busca un cliente por su cédula, o devuelve null si todavía no está registrado.
     */
    private Cliente buscarCliente(String cedula) {
        for (Cliente c : clientes) {
            if (c.getCedula().equals(cedula)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Genera un número de cuenta de 4 dígitos que no esté repetido.
     */
    private String generarNumeroCuenta() {
        String numero;
        do {
            numero = String.format("%04d", aleatorio.nextInt(10000));
        } while (buscarCuenta(numero) != null);
        return numero;
    }

    private boolean vacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
