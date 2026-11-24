package co.edu.sena.rastreador.dto;

import java.time.LocalDateTime;

/**
 * Un punto de la trayectoria de una canción: su posición en una captura y cómo
 * cambió respecto a la captura anterior en la misma lista.
 *
 * @param diferencia puestos ganados (positivo) o perdidos (negativo); nulo si es NUEVA
 */
public record PuntoTrayectoriaDto(Integer idTendencia, int idLista, String nombreLista,
                                  LocalDateTime fechaCaptura, int posicion, Long streams,
                                  TipoMovimiento movimiento, Integer diferencia) {
}
