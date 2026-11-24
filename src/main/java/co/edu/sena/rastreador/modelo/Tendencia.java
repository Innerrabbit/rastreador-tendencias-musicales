package co.edu.sena.rastreador.modelo;

import java.time.LocalDateTime;

/**
 * Entidad que representa la posición de una canción en una lista en un momento
 * dado. Una fila de la tabla Tendencia es una "captura"; el conjunto de capturas
 * de una canción forma su historial.
 */
public class Tendencia {

    private Integer idTendencia;
    private int idCancion;
    private int idLista;
    private LocalDateTime fechaCaptura;
    private int posicion;
    private Long streams;

    public Tendencia() {
    }

    public Tendencia(int idCancion, int idLista, LocalDateTime fechaCaptura,
                     int posicion, Long streams) {
        this.idCancion = idCancion;
        this.idLista = idLista;
        this.fechaCaptura = fechaCaptura;
        this.posicion = posicion;
        this.streams = streams;
    }

    public Integer getIdTendencia() {
        return idTendencia;
    }

    public void setIdTendencia(Integer idTendencia) {
        this.idTendencia = idTendencia;
    }

    public int getIdCancion() {
        return idCancion;
    }

    public void setIdCancion(int idCancion) {
        this.idCancion = idCancion;
    }

    public int getIdLista() {
        return idLista;
    }

    public void setIdLista(int idLista) {
        this.idLista = idLista;
    }

    public LocalDateTime getFechaCaptura() {
        return fechaCaptura;
    }

    public void setFechaCaptura(LocalDateTime fechaCaptura) {
        this.fechaCaptura = fechaCaptura;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    /** Número de reproducciones; es opcional (puede ser nulo). */
    public Long getStreams() {
        return streams;
    }

    public void setStreams(Long streams) {
        this.streams = streams;
    }

    @Override
    public String toString() {
        return "Tendencia{id=" + idTendencia + ", cancion=" + idCancion + ", lista=" + idLista
                + ", fecha=" + fechaCaptura + ", posicion=" + posicion + "}";
    }
}
