package co.edu.sena.rastreador.controlador;

import co.edu.sena.rastreador.dto.ErrorRespuesta;
import co.edu.sena.rastreador.excepcion.AccesoDatosException;
import co.edu.sena.rastreador.excepcion.RecursoNoEncontradoException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Traduce las excepciones del sistema a respuestas HTTP con un cuerpo JSON uniforme.
 *   ReglaNegocioException        -> 400 (dato inválido o regla incumplida)
 *   RecursoNoEncontradoException -> 404 (subclase de la anterior; gana la más específica)
 *   AccesoDatosException         -> 500 (con mensaje genérico; el detalle va solo al log)
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger REGISTRO = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> alNoEncontrar(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, "No encontrado", e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> alIncumplirRegla(ReglaNegocioException e) {
        return responder(HttpStatus.BAD_REQUEST, "Solicitud inválida", e.getMessage());
    }

    @ExceptionHandler(AccesoDatosException.class)
    public ResponseEntity<ErrorRespuesta> alFallarElAccesoADatos(AccesoDatosException e) {
        REGISTRO.error("Error de acceso a datos", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error al acceder a la base de datos. Intente de nuevo más tarde.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> alLeerUnJsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "Solicitud inválida",
                "El cuerpo de la solicitud falta o no es un JSON válido (revise los tipos y el formato "
                        + "de las fechas: AAAA-MM-DDThh:mm:ss).");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> alRecibirUnParametroInvalido(MethodArgumentTypeMismatchException e) {
        return responder(HttpStatus.BAD_REQUEST, "Solicitud inválida",
                "El valor del parámetro '" + e.getName() + "' no es válido.");
    }

    private static ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String error, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new ErrorRespuesta(estado.value(), error, mensaje, LocalDateTime.now()));
    }
}
