package co.edu.sena.rastreador.modelo;

/**
 * Entidad que representa una lista de reproducción (por ejemplo, un ranking
 * de una plataforma en una región). Corresponde a la tabla ListaReproduccion.
 */
public class ListaReproduccion {

    private Integer idLista;
    private String idPlataforma;
    private String nombre;
    private Plataforma plataforma;
    private String region;
    private String urlPlataforma;

    public ListaReproduccion() {
    }

    public ListaReproduccion(String idPlataforma, String nombre, Plataforma plataforma,
                             String region, String urlPlataforma) {
        this.idPlataforma = idPlataforma;
        this.nombre = nombre;
        this.plataforma = plataforma;
        this.region = region;
        this.urlPlataforma = urlPlataforma;
    }

    public ListaReproduccion(Integer idLista, String idPlataforma, String nombre,
                             Plataforma plataforma, String region, String urlPlataforma) {
        this(idPlataforma, nombre, plataforma, region, urlPlataforma);
        this.idLista = idLista;
    }

    public Integer getIdLista() {
        return idLista;
    }

    public void setIdLista(Integer idLista) {
        this.idLista = idLista;
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

    public Plataforma getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(Plataforma plataforma) {
        this.plataforma = plataforma;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getUrlPlataforma() {
        return urlPlataforma;
    }

    public void setUrlPlataforma(String urlPlataforma) {
        this.urlPlataforma = urlPlataforma;
    }

    @Override
    public String toString() {
        return "ListaReproduccion{id=" + idLista + ", idPlataforma='" + idPlataforma
                + "', nombre='" + nombre + "', plataforma=" + plataforma + "}";
    }
}
