import java.io.Serializable;

/**
 * Persona que abre una o más cuentas en el cajero. Implementa Serializable para poder guardarla en el archivo de datos junto con sus cuentas.
 */
public class Cliente implements Serializable {

    //Identificador de versión de la clase, necesario para leer correctamente los objetos guardados en el archivo
    private static final long serialVersionUID = 1L;

    private final String cedula;
    private final String nombre;
    private final String apellidos;
    private final String telefono;

    public Cliente(String cedula, String nombre, String apellidos, String telefono) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    /**
     * Devuelve el nombre y los apellidos juntos, es el que se usa en el saludo y en el comprobante.
     */
    public String nombreCompleto() {
        return nombre + " " + apellidos;
    }

    @Override
    public String toString() {
        return nombreCompleto() + " (cédula " + cedula + ")";
    }
}
