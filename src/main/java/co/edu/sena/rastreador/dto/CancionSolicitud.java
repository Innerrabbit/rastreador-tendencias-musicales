package co.edu.sena.rastreador.dto;

import java.util.List;

/**
 * Cuerpo JSON para registrar o actualizar una canción. En "artistas" se envían los
 * idPlataforma de los artistas (por ejemplo ["ART-0001"]). Al actualizar, idPlataforma
 * y artistas se ignoran, igual que en el menú de la EV01.
 */
public record CancionSolicitud(String idPlataforma, String titulo, Integer duracionMs,
                               String genero, String urlPlataforma, List<String> artistas) {
}
