package co.edu.sena.rastreador.dao;

import co.edu.sena.rastreador.modelo.Tendencia;

import java.time.LocalDate;
import java.util.List;

/**
 * Operaciones de persistencia sobre la tabla Tendencia (historial de posiciones).
 * Equivale al TendenciaRepository del diagrama de clases.
 */
public interface TendenciaDao {

    /** Inserta la captura y devuelve el mismo objeto con su id generado. */
    Tendencia insertar(Tendencia tendencia);

    /**
     * Indica si ya existe una captura de esa canción en esa lista durante el día
     * indicado. Permite que el registro sea idempotente (una captura por día).
     */
    boolean existeCapturaDelDia(int idCancion, int idLista, LocalDate dia);

    /** Historial completo de una canción, ordenado de la captura más antigua a la más reciente. */
    List<Tendencia> listarPorCancion(int idCancion);

    /** @return true si existía una captura con ese id y fue eliminada */
    boolean eliminar(int idTendencia);
}
