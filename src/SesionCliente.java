/**
 * Datos de la sesión que tiene abierta el cliente en el cajero. El servidor no guarda sesiones, por eso en cada operación el cajero le envía
 * la cuenta y el PIN y el servidor los verifica de nuevo.
 */
public class SesionCliente {

    private final String numeroCuenta;
    private final String pin;
    private final String nombreCompleto;

    public SesionCliente(String numeroCuenta, String pin, String nombreCompleto) {
        this.numeroCuenta = numeroCuenta;
        this.pin = pin;
        this.nombreCompleto = nombreCompleto;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public String getPin() {
        return pin;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }
}
