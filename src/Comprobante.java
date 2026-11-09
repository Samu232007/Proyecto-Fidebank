/**
 * Desglose de una transacción. Se muestra en pantalla como si fuera el comprobante impreso. Depende de la Transaccion y del Cliente titular.
 */
public class Comprobante {

    private final Transaccion transaccion;
    private final Cliente titular;

    public Comprobante(Transaccion transaccion, Cliente titular) {
        this.transaccion = transaccion;
        this.titular = titular;
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public Cliente getTitular() {
        return titular;
    }

    /**
     * Arma el texto del comprobante tal como se muestra en pantalla. La cuenta destino solo aparece cuando la transacción es una transferencia.
     */
    public String generarTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================\n");
        sb.append("  Comprobante de transacción\n");
        sb.append("==============================\n");
        sb.append("Transacción número: ").append(transaccion.getIdTransaccion()).append("\n");
        sb.append("Fecha y hora: ").append(Formato.formatearFecha(transaccion.getFechaHora())).append("\n");
        sb.append("Cliente: ").append(titular.nombreCompleto()).append("\n");
        sb.append("Tipo: ").append(transaccion.getTipo()).append("\n");
        sb.append("Cuenta de origen: ").append(transaccion.getCuentaOrigen()).append("\n");
        if (transaccion.getCuentaDestino() != null) {
            sb.append("Cuenta destino: ").append(transaccion.getCuentaDestino()).append("\n");
        }
        sb.append("Monto: ").append(Formato.formatearMonto(transaccion.getMonto())).append("\n");
        sb.append("Saldo resultante: ").append(Formato.formatearMonto(transaccion.getSaldoResultante())).append("\n");
        sb.append("==============================");
        return sb.toString();
    }
}
