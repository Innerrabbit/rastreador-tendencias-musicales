package co.edu.sena.rastreador.dto;

import java.time.LocalDateTime;

/** Cuerpo JSON uniforme para todos los errores de la API. */
public record ErrorRespuesta(int estado, String error, String mensaje, LocalDateTime fecha) {
}
