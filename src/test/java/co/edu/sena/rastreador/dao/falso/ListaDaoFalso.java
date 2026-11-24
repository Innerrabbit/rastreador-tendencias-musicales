package co.edu.sena.rastreador.dao.falso;

import co.edu.sena.rastreador.dao.ListaReproduccionDao;
import co.edu.sena.rastreador.modelo.ListaReproduccion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Doble de prueba de {@link ListaReproduccionDao}: guarda las listas en memoria. */
public class ListaDaoFalso implements ListaReproduccionDao {

    private final Map<Integer, ListaReproduccion> listas = new LinkedHashMap<>();
    private int siguienteId = 1;

    /** Atajo para preparar datos en las pruebas. */
    public ListaReproduccion agregar(ListaReproduccion lista) {
        return insertar(lista);
    }

    @Override
    public ListaReproduccion insertar(ListaReproduccion lista) {
        lista.setIdLista(siguienteId++);
        listas.put(lista.getIdLista(), lista);
        return lista;
    }

    @Override
    public Optional<ListaReproduccion> buscarPorId(int idLista) {
        return Optional.ofNullable(listas.get(idLista));
    }

    @Override
    public Optional<ListaReproduccion> buscarPorIdPlataforma(String idPlataforma) {
        return listas.values().stream()
                .filter(l -> l.getIdPlataforma().equals(idPlataforma)).findFirst();
    }

    @Override
    public List<ListaReproduccion> listarTodas() {
        return new ArrayList<>(listas.values());
    }

    @Override
    public boolean actualizar(ListaReproduccion lista) {
        return listas.replace(lista.getIdLista(), lista) != null;
    }

    @Override
    public boolean eliminar(int idLista) {
        return listas.remove(idLista) != null;
    }
}
