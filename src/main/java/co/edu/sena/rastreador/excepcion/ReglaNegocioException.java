package co.edu.sena.rastreador.excepcion;

/**
 * Se lanza cuando una operación incumple una regla del negocio
 * (dato obligatorio vacío, registro duplicado, registro inexistente, etc.).
 * Su mensaje está pensado para mostrarse directamente al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
