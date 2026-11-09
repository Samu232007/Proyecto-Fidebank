/**
 * Transacción de retiro de fondos. Hereda los datos comunes de Transaccion y no usa cuenta destino.
 */
public class Retiro extends Transaccion {

    private static final long serialVersionUID = 1L;

    public Retiro(int idTransaccion, double monto, String cuentaOrigen, double saldoResultante) {
        super(idTransaccion, "Retiro", monto, cuentaOrigen, null, saldoResultante);
    }

    @Override
    public String descripcion() {
        return "Retiro de " + Formato.formatearMonto(getMonto()) + " de la cuenta " + getCuentaOrigen();
    }
}
