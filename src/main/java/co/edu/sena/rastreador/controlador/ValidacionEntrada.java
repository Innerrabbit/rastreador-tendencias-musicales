package co.edu.sena.rastreador.controlador;

import co.edu.sena.rastreador.excepcion.ReglaNegocioException;

/** Validaciones comunes de los datos que llegan en el cuerpo JSON. */
final class ValidacionEntrada {

    private ValidacionEntrada() {
    }

    /** Los DTO de entrada usan Integer (que admite nulo); los servicios usan int. */
    static int exigirEntero(Integer valor, String campo) {
        if (valor == null) {
            throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio.");
        }
        return valor;
    }
}
