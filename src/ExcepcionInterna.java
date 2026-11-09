/**
 * Excepción propia del sistema. Se lanza cuando una operación del cajero no se puede realizar: número de cuenta o PIN incorrecto, monto no válido,
 * saldo insuficiente o cuenta destino no válida. El mensaje es el texto que se le muestra al cliente.
 */
public class ExcepcionInterna extends Exception {

    //Identificador de versión de la clase (las excepciones son Serializable por defecto)
    private static final long serialVersionUID = 1L;

    public ExcepcionInterna(String mensaje) {
        super(mensaje);
    }
}
