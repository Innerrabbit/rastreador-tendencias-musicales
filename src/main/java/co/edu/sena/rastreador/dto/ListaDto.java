package co.edu.sena.rastreador.dto;

import co.edu.sena.rastreador.modelo.ListaReproduccion;

/** Lista de reproducción tal como se devuelve por la API. */
public record ListaDto(Integer idLista, String idPlataforma, String nombre, String plataforma,
                       String region, String urlPlataforma) {

    public static ListaDto desde(ListaReproduccion lista) {
        return new ListaDto(lista.getIdLista(), lista.getIdPlataforma(), lista.getNombre(),
                lista.getPlataforma().name(), lista.getRegion(), lista.getUrlPlataforma());
    }
}
