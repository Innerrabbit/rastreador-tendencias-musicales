package co.edu.sena.rastreador.dao;

import co.edu.sena.rastreador.modelo.Cancion;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones de persistencia sobre la tabla Cancion y su relación
 * con Artista (tabla Cancion_Artista).
 * Equivale al CancionRepository del diagrama de clases.
 */
public interface CancionDao {

    /**
     * Inserta la canción y sus relaciones con los artistas en una sola
     * transacción. Los artistas deben existir previamente (con idArtista).
     */
    Cancion insertar(Cancion cancion);

    Optional<Cancion> buscarPorId(int idCancion);

    Optional<Cancion> buscarPorIdPlataforma(String idPlataforma);

    /** Busca canciones cuyo título contenga el texto indicado. */
    List<Cancion> buscarPorTitulo(String titulo);

    List<Cancion> listarTodas();

    /** Actualiza título, duración, género y URL (no modifica los artistas). */
    boolean actualizar(Cancion cancion);

    /** Elimina la canción y sus filas en Cancion_Artista. */
    boolean eliminar(int idCancion);
}
