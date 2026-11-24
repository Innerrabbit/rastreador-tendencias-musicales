package co.edu.sena.rastreador.dto;

/** Cuerpo JSON para registrar o actualizar un artista (al actualizar, idPlataforma se ignora). */
public record ArtistaSolicitud(String idPlataforma, String nombre, String urlPlataforma) {
}
