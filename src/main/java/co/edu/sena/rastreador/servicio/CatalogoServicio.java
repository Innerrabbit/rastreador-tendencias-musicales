package co.edu.sena.rastreador.servicio;

import co.edu.sena.rastreador.dao.ArtistaDao;
import co.edu.sena.rastreador.dao.ArtistaDaoJdbc;
import co.edu.sena.rastreador.dao.CancionDao;
import co.edu.sena.rastreador.dao.CancionDaoJdbc;
import co.edu.sena.rastreador.excepcion.RecursoNoEncontradoException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.Artista;
import co.edu.sena.rastreador.modelo.Cancion;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Lógica de negocio del catálogo (RF-04): valida los datos, evita duplicados
 * por idPlataforma y coordina los DAO. Corresponde al CatalogoService del
 * diagrama de clases.
 */
public class CatalogoServicio {

    private static final int MAX_ID_PLATAFORMA = 50;
    private static final int MAX_TITULO = 255;
    private static final int MAX_NOMBRE = 255;
    private static final int MAX_GENERO = 100;
    private static final int MAX_URL = 500;

    private final ArtistaDao artistaDao;
    private final CancionDao cancionDao;

    public CatalogoServicio() {
        this(new ArtistaDaoJdbc(), new CancionDaoJdbc());
    }

    public CatalogoServicio(ArtistaDao artistaDao, CancionDao cancionDao) {
        this.artistaDao = artistaDao;
        this.cancionDao = cancionDao;
    }

    // ------------------------------------------------------------------
    // Artistas
    // ------------------------------------------------------------------

    public Artista registrarArtista(String idPlataforma, String nombre, String urlPlataforma) {
        String idValidado = exigirTexto(idPlataforma, "El idPlataforma del artista", MAX_ID_PLATAFORMA);
        String nombreValidado = exigirTexto(nombre, "El nombre del artista", MAX_NOMBRE);
        String urlValidada = textoOpcional(urlPlataforma, "La URL del artista", MAX_URL);

        if (artistaDao.buscarPorIdPlataforma(idValidado).isPresent()) {
            throw new ReglaNegocioException(
                    "Ya existe un artista con idPlataforma '" + idValidado + "'.");
        }
        return artistaDao.insertar(new Artista(idValidado, nombreValidado, urlValidada));
    }

    public Optional<Artista> obtenerArtista(int idArtista) {
        return artistaDao.buscarPorId(idArtista);
    }

    public Optional<Artista> buscarArtistaPorIdPlataforma(String idPlataforma) {
        return artistaDao.buscarPorIdPlataforma(idPlataforma.trim());
    }

    public List<Artista> buscarArtistasPorNombre(String nombre) {
        return artistaDao.buscarPorNombre(nombre.trim());
    }

    public List<Artista> listarArtistas() {
        return artistaDao.listarTodos();
    }

    public void actualizarArtista(Artista artista) {
        artista.setNombre(exigirTexto(artista.getNombre(), "El nombre del artista", MAX_NOMBRE));
        artista.setUrlPlataforma(
                textoOpcional(artista.getUrlPlataforma(), "La URL del artista", MAX_URL));
        if (!artistaDao.actualizar(artista)) {
            throw new RecursoNoEncontradoException(
                    "No existe un artista con id " + artista.getIdArtista() + ".");
        }
    }

    public void eliminarArtista(int idArtista) {
        if (!artistaDao.eliminar(idArtista)) {
            throw new RecursoNoEncontradoException("No existe un artista con id " + idArtista + ".");
        }
    }

    // ------------------------------------------------------------------
    // Canciones
    // ------------------------------------------------------------------

    /**
     * Registra una canción asociada a uno o más artistas ya registrados.
     *
     * @param idsPlataformaArtistas idPlataforma de cada artista intérprete
     */
    public Cancion registrarCancion(String idPlataforma, String titulo, int duracionMs,
                                    String genero, String urlPlataforma,
                                    List<String> idsPlataformaArtistas) {
        String idValidado = exigirTexto(idPlataforma, "El idPlataforma de la canción", MAX_ID_PLATAFORMA);
        String tituloValidado = exigirTexto(titulo, "El título de la canción", MAX_TITULO);
        validarDuracion(duracionMs);
        String generoValidado = textoOpcional(genero, "El género", MAX_GENERO);
        String urlValidada = textoOpcional(urlPlataforma, "La URL de la canción", MAX_URL);

        if (cancionDao.buscarPorIdPlataforma(idValidado).isPresent()) {
            throw new ReglaNegocioException(
                    "Ya existe una canción con idPlataforma '" + idValidado + "'.");
        }

        Cancion cancion = new Cancion(idValidado, tituloValidado, duracionMs,
                generoValidado, urlValidada);
        cancion.setArtistas(resolverArtistas(idsPlataformaArtistas));
        return cancionDao.insertar(cancion);
    }

    public Optional<Cancion> obtenerCancion(int idCancion) {
        return cancionDao.buscarPorId(idCancion);
    }

    public Optional<Cancion> buscarCancionPorIdPlataforma(String idPlataforma) {
        return cancionDao.buscarPorIdPlataforma(idPlataforma.trim());
    }

    public List<Cancion> buscarCancionesPorTitulo(String titulo) {
        return cancionDao.buscarPorTitulo(titulo.trim());
    }

    public List<Cancion> listarCanciones() {
        return cancionDao.listarTodas();
    }

    public void actualizarCancion(Cancion cancion) {
        cancion.setTitulo(exigirTexto(cancion.getTitulo(), "El título de la canción", MAX_TITULO));
        validarDuracion(cancion.getDuracionMs());
        cancion.setGenero(textoOpcional(cancion.getGenero(), "El género", MAX_GENERO));
        cancion.setUrlPlataforma(
                textoOpcional(cancion.getUrlPlataforma(), "La URL de la canción", MAX_URL));
        if (!cancionDao.actualizar(cancion)) {
            throw new RecursoNoEncontradoException(
                    "No existe una canción con id " + cancion.getIdCancion() + ".");
        }
    }

    public void eliminarCancion(int idCancion) {
        if (!cancionDao.eliminar(idCancion)) {
            throw new RecursoNoEncontradoException("No existe una canción con id " + idCancion + ".");
        }
    }

    // ------------------------------------------------------------------
    // Validaciones auxiliares
    // ------------------------------------------------------------------

    private List<Artista> resolverArtistas(List<String> idsPlataformaArtistas) {
        if (idsPlataformaArtistas == null || idsPlataformaArtistas.isEmpty()) {
            throw new ReglaNegocioException("La canción debe tener al menos un artista.");
        }
        Set<String> idsUnicos = new LinkedHashSet<>();
        for (String id : idsPlataformaArtistas) {
            if (id != null && !id.isBlank()) {
                idsUnicos.add(id.trim());
            }
        }
        if (idsUnicos.isEmpty()) {
            throw new ReglaNegocioException("La canción debe tener al menos un artista.");
        }
        List<Artista> artistas = new ArrayList<>();
        for (String id : idsUnicos) {
            Artista artista = artistaDao.buscarPorIdPlataforma(id).orElseThrow(() ->
                    new ReglaNegocioException("El artista con idPlataforma '" + id
                            + "' no está registrado. Regístrelo primero."));
            artistas.add(artista);
        }
        return artistas;
    }

    private void validarDuracion(int duracionMs) {
        if (duracionMs <= 0) {
            throw new ReglaNegocioException("La duración debe ser un número positivo de milisegundos.");
        }
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
