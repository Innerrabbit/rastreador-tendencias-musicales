package co.edu.sena.rastreador.dto;

import java.util.List;

/**
 * Respuesta de la consulta de trayectoria (CU-05): la canción, sus puntos en orden
 * cronológico y, cuando aplica, un mensaje (por ejemplo, si solo hay una captura).
 */
public record TrayectoriaDto(CancionResumenDto cancion, List<PuntoTrayectoriaDto> puntos,
                             String mensaje) {
}
