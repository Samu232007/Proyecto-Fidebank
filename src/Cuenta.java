import java.io.Serializable;

/**
 * Cuenta bancaria de un cliente. Guarda el saldo y el PIN con el que se ingresa al cajero. Los métodos que tocan el saldo son sincronizados
 * porque el servidor atiende a varios clientes al mismo tiempo, cada uno en su propio hilo.
 */
public class Cuenta implements Serializable {

    //Identificador de versión de la clase, necesario para leer correctamente los objetos guardados en el archivo
    private static final long serialVersionUID = 1L;

    private final String numeroCuenta;
    private final Cliente titular;
    private final String pin;
    private double saldo;

    /**
     * Crea la cuenta con saldo de cero, así inician todas las cuentas nuevas.
     */
    public Cuenta(String numeroCuenta, Cliente titular, String pin) {
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.pin = pin;
        this.saldo = 0;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public Cliente getTitular() {
        return titular;
    }

    public synchronized double getSaldo() {
        return saldo;
    }

    /**
     * Compara el PIN recibido con el de la cuenta.
     */
    public boolean verificarPin(String pin) {
        return this.pin.equals(pin);
    }

    /**
     * Suma el monto al saldo. El monto ya viene validado desde SistemaCajero.
     */
    public synchronized void depositar(double monto) {
        saldo = Formato.redondear(saldo + monto);
    }

    /**
     * Resta el monto al saldo. Si el saldo no alcanza lanza ExcepcionInterna y el saldo queda igual.
     */
    public synchronized void retirar(double monto) throws ExcepcionInterna {
        if (monto > saldo) {
            throw new ExcepcionInterna("Saldo insuficiente");
        }
        saldo = Formato.redondear(saldo - monto);
    }
}
