package co.edu.sena.rastreador.excepcion;

/**
 * Se lanza cuando se pide un registro que no existe (canción, artista, lista, etc.).
 * Es un caso particular de {@link ReglaNegocioException}: el menú de consola sigue
 * atrapándola como antes, y la API REST la traduce al código HTTP 404.
 */
public class RecursoNoEncontradoException extends ReglaNegocioException {

    private static final long serialVersionUID = 1L;

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
