package co.edu.sena.rastreador.dao.falso;

import co.edu.sena.rastreador.dao.CancionDao;
import co.edu.sena.rastreador.modelo.Cancion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Doble de prueba de {@link CancionDao}: guarda las canciones en memoria en lugar de
 * usar MySQL. Permite probar los servicios sin base de datos y sin Spring.
 */
public class CancionDaoFalso implements CancionDao {

    private final Map<Integer, Cancion> canciones = new LinkedHashMap<>();
    private int siguienteId = 1;

    /** Atajo para preparar datos en las pruebas. */
    public Cancion agregar(Cancion cancion) {
        return insertar(cancion);
    }

    @Override
    public Cancion insertar(Cancion cancion) {
        cancion.setIdCancion(siguienteId++);
        canciones.put(cancion.getIdCancion(), cancion);
        return cancion;
    }

    @Override
    public Optional<Cancion> buscarPorId(int idCancion) {
        return Optional.ofNullable(canciones.get(idCancion));
    }

    @Override
    public Optional<Cancion> buscarPorIdPlataforma(String idPlataforma) {
        return canciones.values().stream()
                .filter(c -> c.getIdPlataforma().equals(idPlataforma)).findFirst();
    }

    @Override
    public List<Cancion> buscarPorTitulo(String titulo) {
        String buscado = titulo.toLowerCase(Locale.ROOT);
        return canciones.values().stream()
                .filter(c -> c.getTitulo().toLowerCase(Locale.ROOT).contains(buscado)).toList();
    }

    @Override
    public List<Cancion> listarTodas() {
        return new ArrayList<>(canciones.values());
    }

    @Override
    public boolean actualizar(Cancion cancion) {
        return canciones.replace(cancion.getIdCancion(), cancion) != null;
    }

    @Override
    public boolean eliminar(int idCancion) {
        return canciones.remove(idCancion) != null;
    }
}
