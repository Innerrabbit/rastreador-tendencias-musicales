package co.edu.sena.rastreador.dto;

import co.edu.sena.rastreador.modelo.Artista;

/** Artista tal como se devuelve por la API (no se expone la entidad). */
public record ArtistaDto(Integer idArtista, String idPlataforma, String nombre, String urlPlataforma) {

    public static ArtistaDto desde(Artista artista) {
        return new ArtistaDto(artista.getIdArtista(), artista.getIdPlataforma(),
                artista.getNombre(), artista.getUrlPlataforma());
    }
}
