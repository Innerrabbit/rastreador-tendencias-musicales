package co.edu.sena.rastreador.dto;

import java.time.LocalDateTime;

/**
 * Cuerpo JSON para registrar una captura de tendencia manualmente.
 * Si fechaCaptura no se envía, se usa la fecha y hora actuales.
 */
public record TendenciaSolicitud(Integer idCancion, Integer idLista, LocalDateTime fechaCaptura,
                                 Integer posicion, Long streams) {
}
