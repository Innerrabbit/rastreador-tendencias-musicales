package co.edu.sena.rastreador.dto;

import co.edu.sena.rastreador.modelo.Tendencia;

import java.time.LocalDateTime;

/** Captura de tendencia tal como se devuelve al registrarla. */
public record TendenciaDto(Integer idTendencia, int idCancion, int idLista,
                           LocalDateTime fechaCaptura, int posicion, Long streams) {

    public static TendenciaDto desde(Tendencia tendencia) {
        return new TendenciaDto(tendencia.getIdTendencia(), tendencia.getIdCancion(),
                tendencia.getIdLista(), tendencia.getFechaCaptura(), tendencia.getPosicion(),
                tendencia.getStreams());
    }
}
