import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Clase base abstracta de las transacciones del cajero. Retiro, Deposito y Transferencia heredan de esta clase, ya que todas comparten estos datos.
 * Al ser abstracta no se pueden crear objetos Transaccion directamente, y cada subclase da su propia descripción (polimorfismo).
 * Implementa Serializable para guardar el registro de transacciones en el archivo.
 */
public abstract class Transaccion implements Serializable {

    //Identificador de versión de la clase, necesario para leer correctamente los objetos guardados en el archivo
    private static final long serialVersionUID = 1L;

    private final int idTransaccion;
    private final String tipo;
    private final LocalDateTime fechaHora;
    private final double monto;
    private final String cuentaOrigen;
    private final String cuentaDestino;
    private final double saldoResultante;

    /**
     * La fecha y hora se toman al crear la transacción. La cuenta destino solo se usa en las transferencias, en las demás va null.
     */
    public Transaccion(int idTransaccion, String tipo, double monto, String cuentaOrigen, String cuentaDestino, double saldoResultante) {
        this.idTransaccion = idTransaccion;
        this.tipo = tipo;
        this.fechaHora = LocalDateTime.now();
        this.monto = monto;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.saldoResultante = saldoResultante;
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public double getMonto() {
        return monto;
    }

    public String getCuentaOrigen() {
        return cuentaOrigen;
    }

    public String getCuentaDestino() {
        return cuentaDestino;
    }

    public double getSaldoResultante() {
        return saldoResultante;
    }

    /**
     * Cada tipo de transacción arma su propio texto con lo que se hizo, por eso es abstracto.
     */
    public abstract String descripcion();

    @Override
    public String toString() {
        return "#" + idTransaccion + " " + Formato.formatearFecha(fechaHora) + " - " + descripcion();
    }
}
