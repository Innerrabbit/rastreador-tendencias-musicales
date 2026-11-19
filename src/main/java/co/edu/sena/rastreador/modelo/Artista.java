package co.edu.sena.rastreador.modelo;

/**
 * Entidad que representa a un artista que interpreta canciones.
 * Corresponde a la tabla Artista de la base de datos.
 */
public class Artista {

    private Integer idArtista;
    private String idPlataforma;
    private String nombre;
    private String urlPlataforma;

    public Artista() {
    }

    public Artista(String idPlataforma, String nombre, String urlPlataforma) {
        this.idPlataforma = idPlataforma;
        this.nombre = nombre;
        this.urlPlataforma = urlPlataforma;
    }

    public Artista(Integer idArtista, String idPlataforma, String nombre, String urlPlataforma) {
        this(idPlataforma, nombre, urlPlataforma);
        this.idArtista = idArtista;
    }

    public Integer getIdArtista() {
        return idArtista;
    }

    public void setIdArtista(Integer idArtista) {
        this.idArtista = idArtista;
    }

    public String getIdPlataforma() {
        return idPlataforma;
    }

    public void setIdPlataforma(String idPlataforma) {
        this.idPlataforma = idPlataforma;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrlPlataforma() {
        return urlPlataforma;
    }

    public void setUrlPlataforma(String urlPlataforma) {
        this.urlPlataforma = urlPlataforma;
    }

    @Override
    public String toString() {
        return "Artista{id=" + idArtista + ", idPlataforma='" + idPlataforma
                + "', nombre='" + nombre + "'}";
    }
}
