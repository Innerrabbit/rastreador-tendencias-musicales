package co.edu.sena.rastreador.configuracion;

import co.edu.sena.rastreador.conexion.ConexionBd;
import co.edu.sena.rastreador.dao.ArtistaDao;
import co.edu.sena.rastreador.dao.ArtistaDaoJdbc;
import co.edu.sena.rastreador.dao.CancionDao;
import co.edu.sena.rastreador.dao.CancionDaoJdbc;
import co.edu.sena.rastreador.dao.ListaReproduccionDao;
import co.edu.sena.rastreador.dao.ListaReproduccionDaoJdbc;
import co.edu.sena.rastreador.dao.TendenciaDao;
import co.edu.sena.rastreador.dao.TendenciaDaoJdbc;
import co.edu.sena.rastreador.excepcion.AccesoDatosException;
import co.edu.sena.rastreador.servicio.CatalogoServicio;
import co.edu.sena.rastreador.servicio.ListaServicio;
import co.edu.sena.rastreador.servicio.TendenciaServicio;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;

/**
 * Registra en Spring los DAO y servicios de la EV01 y del módulo de listas y
 * tendencias. Esas clases no llevan anotaciones de Spring: aquí se crean y se
 * conectan entre sí (inyección de dependencias por constructor).
 */
@Configuration
public class ConfiguracionBeans {

    @Bean
    public ArtistaDao artistaDao() {
        return new ArtistaDaoJdbc();
    }

    @Bean
    public CancionDao cancionDao() {
        return new CancionDaoJdbc();
    }

    @Bean
    public ListaReproduccionDao listaReproduccionDao() {
        return new ListaReproduccionDaoJdbc();
    }

    @Bean
    public TendenciaDao tendenciaDao() {
        return new TendenciaDaoJdbc();
    }

    @Bean
    public CatalogoServicio catalogoServicio(ArtistaDao artistaDao, CancionDao cancionDao) {
        return new CatalogoServicio(artistaDao, cancionDao);
    }

    @Bean
    public ListaServicio listaServicio(ListaReproduccionDao listaDao) {
        return new ListaServicio(listaDao);
    }

    @Bean
    public TendenciaServicio tendenciaServicio(TendenciaDao tendenciaDao, CancionDao cancionDao,
                                               ListaReproduccionDao listaDao) {
        return new TendenciaServicio(tendenciaDao, cancionDao, listaDao, Clock.systemDefaultZone());
    }

    /**
     * Al arrancar comprueba la conexión con la base de datos, como hacía el menú de
     * consola. Si falla, la aplicación se detiene con un mensaje claro en lugar de
     * arrancar y fallar en la primera petición.
     */
    @Bean
    public ApplicationRunner verificarConexionBd() {
        return argumentos -> {
            try (Connection conexion = ConexionBd.obtenerConexion()) {
                if (!conexion.isValid(3)) {
                    throw new IllegalStateException("La conexión con la base de datos no es válida.");
                }
            } catch (SQLException | AccesoDatosException e) {
                throw new IllegalStateException(
                        "No se pudo conectar con la base de datos: " + e.getMessage(), e);
            }
        };
    }
}
