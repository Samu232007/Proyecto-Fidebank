/**
 * Define el protocolo de comunicación entre la aplicación cliente (el cajero) y la aplicación servidor (FideBank). Lo usan las dos aplicaciones,
 * así el puerto, los comandos y las respuestas quedan en un solo lugar.
 *
 * Cada petición es de texto y se envía una línea por dato. La primera línea es el comando y las siguientes son sus datos:
 *   ABRIR_CUENTA          -> cédula, nombre, apellidos, teléfono, PIN
 *   INICIAR_SESION        -> cuenta, PIN
 *   CONSULTAR_SALDO       -> cuenta, PIN
 *   RETIRAR               -> cuenta, PIN, monto
 *   DEPOSITAR             -> cuenta, PIN, monto
 *   VALIDAR_TRANSFERENCIA -> cuenta, PIN, cuenta destino, monto
 *   TRANSFERIR            -> cuenta, PIN, cuenta destino, monto
 *   COMPROBANTE           -> cuenta, PIN, número de transacción
 * La respuesta también es de texto. La primera línea es el código y las siguientes son sus datos:
 *   OK     -> ABRIR_CUENTA: número de cuenta / INICIAR_SESION: nombre completo y saldo / CONSULTAR_SALDO: saldo
 *             RETIRAR, DEPOSITAR y TRANSFERIR: número de transacción, monto y saldo / VALIDAR_TRANSFERENCIA: nombre del titular destino
 *             COMPROBANTE: las líneas del texto del comprobante
 *   ERROR  -> mensaje que se le muestra al cliente (es el mensaje de la ExcepcionInterna)
 */
public class Protocolo {

    //Dirección y puerto donde escucha el servidor (se usa localhost porque el proyecto se ejecuta en una sola computadora)
    public static final String HOST = "localhost";
    public static final int PUERTO = 5001;

    //Tiempo máximo de espera (en milisegundos) para conectarse al servidor y para esperar su respuesta
    public static final int TIEMPO_ESPERA = 5000;

    //Comandos que envía el cliente
    public static final String ABRIR_CUENTA = "ABRIR_CUENTA";
    public static final String INICIAR_SESION = "INICIAR_SESION";
    public static final String CONSULTAR_SALDO = "CONSULTAR_SALDO";
    public static final String RETIRAR = "RETIRAR";
    public static final String DEPOSITAR = "DEPOSITAR";
    public static final String VALIDAR_TRANSFERENCIA = "VALIDAR_TRANSFERENCIA";
    public static final String TRANSFERIR = "TRANSFERIR";
    public static final String COMPROBANTE = "COMPROBANTE";

    //Códigos de respuesta que envía el servidor
    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
}
