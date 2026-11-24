package co.edu.sena.rastreador.dao.falso;

import co.edu.sena.rastreador.dao.TendenciaDao;
import co.edu.sena.rastreador.modelo.Tendencia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Doble de prueba de {@link TendenciaDao}: guarda las capturas en memoria.
 * El historial se devuelve ordenado por fecha, igual que la consulta real.
 */
public class TendenciaDaoFalso implements TendenciaDao {

    private final Map<Integer, Tendencia> capturas = new LinkedHashMap<>();
    private int siguienteId = 1;

    @Override
    public Tendencia insertar(Tendencia tendencia) {
        tendencia.setIdTendencia(siguienteId++);
        capturas.put(tendencia.getIdTendencia(), tendencia);
        return tendencia;
    }

    @Override
    public boolean existeCapturaDelDia(int idCancion, int idLista, LocalDate dia) {
        return capturas.values().stream().anyMatch(t -> t.getIdCancion() == idCancion
                && t.getIdLista() == idLista && t.getFechaCaptura().toLocalDate().equals(dia));
    }

    @Override
    public List<Tendencia> listarPorCancion(int idCancion) {
        List<Tendencia> historial = new ArrayList<>(capturas.values().stream()
                .filter(t -> t.getIdCancion() == idCancion).toList());
        historial.sort(Comparator.comparing(Tendencia::getFechaCaptura)
                .thenComparing(Tendencia::getIdTendencia));
        return historial;
    }

    @Override
    public boolean eliminar(int idTendencia) {
        return capturas.remove(idTendencia) != null;
    }
}
