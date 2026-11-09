/**
 * Respuesta del servidor cuando un retiro, depósito o transferencia se realizó con éxito: número de transacción, monto y nuevo saldo.
 */
public class ResultadoOperacion {

    private final int idTransaccion;
    private final double monto;
    private final double saldo;

    public ResultadoOperacion(int idTransaccion, double monto, double saldo) {
        this.idTransaccion = idTransaccion;
        this.monto = monto;
        this.saldo = saldo;
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public double getMonto() {
        return monto;
    }

    public double getSaldo() {
        return saldo;
    }
}
