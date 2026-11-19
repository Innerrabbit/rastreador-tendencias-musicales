package co.edu.sena.rastreador.conexion;

import co.edu.sena.rastreador.excepcion.AccesoDatosException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexiones JDBC hacia MySQL.
 * Los datos de conexión se leen de variables de entorno para no
 * almacenar credenciales en el código fuente ni en el repositorio.
 */
public final class ConexionBd {

    private static final String VARIABLE_URL = "BD_URL";
    private static final String VARIABLE_USUARIO = "BD_USUARIO";
    private static final String VARIABLE_CLAVE = "BD_CLAVE";

    private ConexionBd() {
    }

    /**
     * Abre una nueva conexión. Quien la solicita debe cerrarla,
     * preferiblemente con try-with-resources.
     *
     * @return conexión abierta a la base de datos
     * @throws SQLException si el servidor rechaza o no responde la conexión
     */
    public static Connection obtenerConexion() throws SQLException {
        String url = leerVariable(VARIABLE_URL);
        String usuario = leerVariable(VARIABLE_USUARIO);
        String clave = leerVariable(VARIABLE_CLAVE);
        return DriverManager.getConnection(url, usuario, clave);
    }

    private static String leerVariable(String nombreVariable) {
        String valor = System.getenv(nombreVariable);
        if (valor == null || valor.isBlank()) {
            throw new AccesoDatosException(
                    "Falta la variable de entorno " + nombreVariable + ".");
        }
        return valor;
    }
}
