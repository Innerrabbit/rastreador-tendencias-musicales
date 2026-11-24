package co.edu.sena.rastreador.dto;

/**
 * Cambio de posición de una canción respecto a su captura anterior en la misma
 * lista (RF-03). Una posición menor es mejor: pasar del puesto 10 al 4 es SUBIO.
 */
public enum TipoMovimiento {
    SUBIO,
    BAJO,
    NUEVA,
    SIN_CAMBIO
}
