package co.edu.sena.rastreador.servicio;

import co.edu.sena.rastreador.dao.ListaReproduccionDao;
import co.edu.sena.rastreador.dao.ListaReproduccionDaoJdbc;
import co.edu.sena.rastreador.excepcion.RecursoNoEncontradoException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.ListaReproduccion;
import co.edu.sena.rastreador.modelo.Plataforma;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Lógica de negocio de las listas de reproducción: valida los datos, evita
 * duplicados por idPlataforma y coordina el DAO. Primera mitad del módulo de
 * listas y tendencias (la otra mitad es {@link TendenciaServicio}).
 */
public class ListaServicio {

    private static final int MAX_ID_PLATAFORMA = 50;
    private static final int MAX_NOMBRE = 255;
    private static final int MAX_REGION = 50;
    private static final int MAX_URL = 500;

    private final ListaReproduccionDao listaDao;

    public ListaServicio() {
        this(new ListaReproduccionDaoJdbc());
    }

    public ListaServicio(ListaReproduccionDao listaDao) {
        this.listaDao = listaDao;
    }

    public ListaReproduccion registrarLista(String idPlataforma, String nombre, String plataforma,
                                            String region, String urlPlataforma) {
        String idValidado = exigirTexto(idPlataforma, "El idPlataforma de la lista", MAX_ID_PLATAFORMA);
        String nombreValidado = exigirTexto(nombre, "El nombre de la lista", MAX_NOMBRE);
        Plataforma plataformaValidada = convertirPlataforma(plataforma);
        String regionValidada = exigirTexto(region, "La región de la lista", MAX_REGION);
        String urlValidada = textoOpcional(urlPlataforma, "La URL de la lista", MAX_URL);

        if (listaDao.buscarPorIdPlataforma(idValidado).isPresent()) {
            throw new ReglaNegocioException(
                    "Ya existe una lista con idPlataforma '" + idValidado + "'.");
        }
        return listaDao.insertar(new ListaReproduccion(
                idValidado, nombreValidado, plataformaValidada, regionValidada, urlValidada));
    }

    public Optional<ListaReproduccion> obtenerLista(int idLista) {
        return listaDao.buscarPorId(idLista);
    }

    public List<ListaReproduccion> listarListas() {
        return listaDao.listarTodas();
    }

    /** Actualiza nombre, plataforma, región y URL; el idPlataforma no se modifica. */
    public ListaReproduccion actualizarLista(int idLista, String nombre, String plataforma,
                                             String region, String urlPlataforma) {
        String nombreValidado = exigirTexto(nombre, "El nombre de la lista", MAX_NOMBRE);
        Plataforma plataformaValidada = convertirPlataforma(plataforma);
        String regionValidada = exigirTexto(region, "La región de la lista", MAX_REGION);
        String urlValidada = textoOpcional(urlPlataforma, "La URL de la lista", MAX_URL);

        ListaReproduccion lista = listaDao.buscarPorId(idLista).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe una lista con id " + idLista + "."));
        lista.setNombre(nombreValidado);
        lista.setPlataforma(plataformaValidada);
        lista.setRegion(regionValidada);
        lista.setUrlPlataforma(urlValidada);
        if (!listaDao.actualizar(lista)) {
            throw new RecursoNoEncontradoException("No existe una lista con id " + idLista + ".");
        }
        return lista;
    }

    public void eliminarLista(int idLista) {
        if (!listaDao.eliminar(idLista)) {
            throw new RecursoNoEncontradoException("No existe una lista con id " + idLista + ".");
        }
    }

    // ------------------------------------------------------------------
    // Validaciones auxiliares
    // ------------------------------------------------------------------

    private Plataforma convertirPlataforma(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("La plataforma es obligatoria (" + valoresPlataforma() + ").");
        }
        try {
            return Plataforma.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException(
                    "La plataforma '" + texto.trim() + "' no es válida. Use " + valoresPlataforma() + ".");
        }
    }

    private String valoresPlataforma() {
        return String.join(" o ", Arrays.stream(Plataforma.values()).map(Enum::name).toList());
    }

    private String exigirTexto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(campo + " es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new ReglaNegocioException(campo + " no puede superar " + maximo + " caracteres.");
        }
        return limpio;
    }

    private String textoOpcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new ReglaNegocioException(campo + " no puede superar " + maximo + " caracteres.");
        }
        return limpio;
    }
}
