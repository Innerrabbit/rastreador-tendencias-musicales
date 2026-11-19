package co.edu.sena.rastreador.excepcion;

/**
 * Se lanza cuando ocurre un error técnico al acceder a la base de datos
 * (conexión, sintaxis, red, etc.). Envuelve la SQLException original.
 */
public class AccesoDatosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccesoDatosException(String mensaje) {
        super(mensaje);
    }

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
