package co.edu.sena.rastreador.dao;

import co.edu.sena.rastreador.modelo.ListaReproduccion;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones de persistencia sobre la tabla ListaReproduccion.
 * Equivale al ListaReproduccionRepository del diagrama de clases.
 */
public interface ListaReproduccionDao {

    /** Inserta la lista y devuelve el mismo objeto con su id generado. */
    ListaReproduccion insertar(ListaReproduccion lista);

    Optional<ListaReproduccion> buscarPorId(int idLista);

    Optional<ListaReproduccion> buscarPorIdPlataforma(String idPlataforma);

    List<ListaReproduccion> listarTodas();

    /** Actualiza nombre, plataforma, región y URL (el idPlataforma no cambia). */
    boolean actualizar(ListaReproduccion lista);

    /** Elimina la lista; falla con regla de negocio si ya tiene historial en Tendencia. */
    boolean eliminar(int idLista);
}
