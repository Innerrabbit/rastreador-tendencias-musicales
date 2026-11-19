package co.edu.sena.rastreador.dao;

import co.edu.sena.rastreador.modelo.Artista;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones de persistencia sobre la tabla Artista.
 * Equivale al ArtistaRepository del diagrama de clases, implementado
 * con JDBC puro según el patrón DAO.
 */
public interface ArtistaDao {

    /** Inserta el artista y devuelve el mismo objeto con su id generado. */
    Artista insertar(Artista artista);

    Optional<Artista> buscarPorId(int idArtista);

    Optional<Artista> buscarPorIdPlataforma(String idPlataforma);

    /** Busca artistas cuyo nombre contenga el texto indicado. */
    List<Artista> buscarPorNombre(String nombre);

    List<Artista> listarTodos();

    /** @return true si existía un artista con ese id y fue actualizado */
    boolean actualizar(Artista artista);

    /** @return true si existía un artista con ese id y fue eliminado */
    boolean eliminar(int idArtista);
}
