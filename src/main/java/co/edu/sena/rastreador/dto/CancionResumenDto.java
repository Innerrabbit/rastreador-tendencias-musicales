package co.edu.sena.rastreador.dto;

import co.edu.sena.rastreador.modelo.Cancion;

import java.util.List;

/** Canción con sus artistas, tal como se devuelve por la API. */
public record CancionResumenDto(Integer idCancion, String idPlataforma, String titulo,
                                int duracionMs, String genero, String urlPlataforma,
                                List<ArtistaDto> artistas) {

    public static CancionResumenDto desde(Cancion cancion) {
        List<ArtistaDto> artistas = cancion.getArtistas().stream().map(ArtistaDto::desde).toList();
        return new CancionResumenDto(cancion.getIdCancion(), cancion.getIdPlataforma(),
                cancion.getTitulo(), cancion.getDuracionMs(), cancion.getGenero(),
                cancion.getUrlPlataforma(), artistas);
    }
}
