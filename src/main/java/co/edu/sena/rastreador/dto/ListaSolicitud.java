package co.edu.sena.rastreador.dto;

/**
 * Cuerpo JSON para registrar o actualizar una lista de reproducción.
 * La plataforma se envía como texto (SPOTIFY o APPLE_MUSIC). Al actualizar,
 * idPlataforma se ignora.
 */
public record ListaSolicitud(String idPlataforma, String nombre, String plataforma,
                             String region, String urlPlataforma) {
}
