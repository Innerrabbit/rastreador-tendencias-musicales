package co.edu.sena.rastreador;

import co.edu.sena.rastreador.conexion.ConexionBd;
import co.edu.sena.rastreador.consola.MenuCatalogo;
import co.edu.sena.rastreador.excepcion.AccesoDatosException;
import co.edu.sena.rastreador.servicio.CatalogoServicio;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Punto de entrada del módulo de catálogo del Rastreador de Tendencias Musicales.
 */
public class Aplicacion {

    private static final int VERIFICACION_SEGUNDOS = 3;

    public static void main(String[] args) {
        if (!verificarConexion()) {
            System.exit(1);
        }
        new MenuCatalogo(new CatalogoServicio()).iniciar();
    }

    /** Comprueba que la base de datos responde antes de mostrar el menú. */
    private static boolean verificarConexion() {
        try (Connection conexion = ConexionBd.obtenerConexion()) {
            if (conexion.isValid(VERIFICACION_SEGUNDOS)) {
                System.out.println("Conexión con la base de datos establecida.");
                return true;
            }
            System.out.println("La conexión con la base de datos no es válida.");
        } catch (AccesoDatosException e) {
            System.out.println("Configuración incompleta: " + e.getMessage());
            System.out.println("Defina BD_URL, BD_USUARIO y BD_CLAVE (ver README.md).");
        } catch (SQLException e) {
            System.out.println("No se pudo conectar con la base de datos: " + e.getMessage());
        }
        return false;
    }
}
