package co.edu.sena.rastreador.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una canción del catálogo.
 * Corresponde a la tabla Cancion y mantiene la relación muchos a muchos
 * con Artista (tabla de unión Cancion_Artista).
 */
public class Cancion {

    private Integer idCancion;
    private String idPlataforma;
    private String titulo;
    private int duracionMs;
    private String genero;
    private String urlPlataforma;
    private List<Artista> artistas = new ArrayList<>();

    public Cancion() {
    }

    public Cancion(String idPlataforma, String titulo, int duracionMs,
                   String genero, String urlPlataforma) {
        this.idPlataforma = idPlataforma;
        this.titulo = titulo;
        this.duracionMs = duracionMs;
        this.genero = genero;
        this.urlPlataforma = urlPlataforma;
    }

    public Integer getIdCancion() {
        return idCancion;
    }

    public void setIdCancion(Integer idCancion) {
        this.idCancion = idCancion;
    }

    public String getIdPlataforma() {
        return idPlataforma;
    }

    public void setIdPlataforma(String idPlataforma) {
        this.idPlataforma = idPlataforma;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getDuracionMs() {
        return duracionMs;
    }

    public void setDuracionMs(int duracionMs) {
        this.duracionMs = duracionMs;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getUrlPlataforma() {
        return urlPlataforma;
    }

    public void setUrlPlataforma(String urlPlataforma) {
        this.urlPlataforma = urlPlataforma;
    }

    public List<Artista> getArtistas() {
        return artistas;
    }

    public void setArtistas(List<Artista> artistas) {
        this.artistas = artistas;
    }

    @Override
    public String toString() {
        return "Cancion{id=" + idCancion + ", idPlataforma='" + idPlataforma
                + "', titulo='" + titulo + "', artistas=" + artistas.size() + "}";
    }
}
