/**
 * Transacción de transferencia entre cuentas. Es la única que usa la cuenta destino, y el saldo resultante es el de la cuenta de origen.
 */
public class Transferencia extends Transaccion {

    private static final long serialVersionUID = 1L;

    public Transferencia(int idTransaccion, double monto, String cuentaOrigen, String cuentaDestino, double saldoResultante) {
        super(idTransaccion, "Transferencia", monto, cuentaOrigen, cuentaDestino, saldoResultante);
    }

    @Override
    public String descripcion() {
        return "Transferencia de " + Formato.formatearMonto(getMonto()) + " de la cuenta " + getCuentaOrigen() + " a la cuenta " + getCuentaDestino();
    }
}
