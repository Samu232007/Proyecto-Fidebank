import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Métodos de apoyo que usan tanto el servidor como el cliente: dar formato a montos y fechas, redondear dinero y leer montos escritos por el cliente.
 * Se usa Locale.US para que el punto sea siempre el separador decimal, así el monto que se escribe y el que se muestra se ven igual en cualquier computadora.
 */
public class Formato {

    //Formato de la fecha y hora de las transacciones y los comprobantes
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Devuelve el monto con separador de miles y dos decimales.
     */
    public static String formatearMonto(double monto) {
        return String.format(Locale.US, "%,.2f", monto);
    }

    /**
     * Devuelve la fecha y hora con el formato de los comprobantes.
     */
    public static String formatearFecha(LocalDateTime fecha) {
        return fecha.format(FORMATO_FECHA);
    }

    /**
     * Redondea a dos decimales, para que las sumas y restas de double no dejen residuos en el saldo.
     */
    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    /**
     * Revisa que el texto tenga al menos un dígito y que todo sean dígitos, se usa para cédula, teléfono y números de cuenta.
     */
    public static boolean esNumero(String texto) {
        return texto != null && texto.matches("\\d+");
    }

    /**
     * Convierte el texto escrito por el cliente en un monto. Acepta solo números positivos con punto y hasta dos decimales.
     * Si el campo está vacío, no es un número o es cero lanza ExcepcionInterna con el mensaje "Monto no válido".
     */
    public static double leerMonto(String texto) throws ExcepcionInterna {
        String limpio = texto == null ? "" : texto.trim();
        if (!limpio.matches("\\d{1,12}(\\.\\d{1,2})?")) {
            throw new ExcepcionInterna("Monto no válido");
        }
        double monto = Double.parseDouble(limpio);
        if (monto <= 0) {
            throw new ExcepcionInterna("Monto no válido");
        }
        return monto;
    }
}
