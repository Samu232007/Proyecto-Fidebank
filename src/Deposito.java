/**
 * Transacción de depósito de dinero. Hereda los datos comunes de Transaccion y no usa cuenta destino.
 */
public class Deposito extends Transaccion {

    private static final long serialVersionUID = 1L;

    public Deposito(int idTransaccion, double monto, String cuentaOrigen, double saldoResultante) {
        super(idTransaccion, "Depósito", monto, cuentaOrigen, null, saldoResultante);
    }

    @Override
    public String descripcion() {
        return "Depósito de " + Formato.formatearMonto(getMonto()) + " en la cuenta " + getCuentaOrigen();
    }
}
