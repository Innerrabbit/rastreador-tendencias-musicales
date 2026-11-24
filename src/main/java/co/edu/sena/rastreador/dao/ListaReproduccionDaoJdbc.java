package co.edu.sena.rastreador.dao;

import co.edu.sena.rastreador.conexion.ConexionBd;
import co.edu.sena.rastreador.excepcion.AccesoDatosException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.ListaReproduccion;
import co.edu.sena.rastreador.modelo.Plataforma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link ListaReproduccionDao}.
 * Usa PreparedStatement en todas las consultas y try-with-resources,
 * igual que {@link ArtistaDaoJdbc}.
 */
public class ListaReproduccionDaoJdbc implements ListaReproduccionDao {

    private static final String SQL_INSERTAR =
            "INSERT INTO ListaReproduccion (idPlataforma, nombre, plataforma, region, urlPlataforma) "
                    + "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECCIONAR =
            "SELECT idLista, idPlataforma, nombre, plataforma, region, urlPlataforma "
                    + "FROM ListaReproduccion";
    private static final String SQL_ACTUALIZAR =
            "UPDATE ListaReproduccion SET nombre = ?, plataforma = ?, region = ?, urlPlataforma = ? "
                    + "WHERE idLista = ?";
    private static final String SQL_ELIMINAR =
            "DELETE FROM ListaReproduccion WHERE idLista = ?";

    @Override
    public ListaReproduccion insertar(ListaReproduccion lista) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, lista.getIdPlataforma());
            sentencia.setString(2, lista.getNombre());
            sentencia.setString(3, lista.getPlataforma().name());
            sentencia.setString(4, lista.getRegion());
            sentencia.setString(5, lista.getUrlPlataforma());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    lista.setIdLista(claves.getInt(1));
                }
            }
            return lista;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ReglaNegocioException(
                    "Ya existe una lista con el idPlataforma indicado.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo insertar la lista de reproducción.", e);
        }
    }

    @Override
    public Optional<ListaReproduccion> buscarPorId(int idLista) {
        List<ListaReproduccion> resultado = consultar(SQL_SELECCIONAR + " WHERE idLista = ?", idLista);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public Optional<ListaReproduccion> buscarPorIdPlataforma(String idPlataforma) {
        List<ListaReproduccion> resultado =
                consultar(SQL_SELECCIONAR + " WHERE idPlataforma = ?", idPlataforma);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<ListaReproduccion> listarTodas() {
        return consultar(SQL_SELECCIONAR + " ORDER BY nombre", null);
    }

    @Override
    public boolean actualizar(ListaReproduccion lista) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            sentencia.setString(1, lista.getNombre());
            sentencia.setString(2, lista.getPlataforma().name());
            sentencia.setString(3, lista.getRegion());
            sentencia.setString(4, lista.getUrlPlataforma());
            sentencia.setInt(5, lista.getIdLista());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar la lista de reproducción.", e);
        }
    }

    @Override
    public boolean eliminar(int idLista) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, idLista);
            return sentencia.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ReglaNegocioException("No se puede eliminar la lista porque tiene "
                    + "historial de tendencias asociado.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo eliminar la lista de reproducción.", e);
        }
    }

    /** Ejecuta una consulta con a lo sumo un parámetro y mapea las filas a ListaReproduccion. */
    private List<ListaReproduccion> consultar(String sql, Object parametro) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (parametro != null) {
                sentencia.setObject(1, parametro);
            }
            List<ListaReproduccion> listas = new ArrayList<>();
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    listas.add(mapear(resultado));
                }
            }
            return listas;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar las listas de reproducción.", e);
        }
    }

    private ListaReproduccion mapear(ResultSet resultado) throws SQLException {
        String textoPlataforma = resultado.getString("plataforma");
        Plataforma plataforma;
        try {
            plataforma = Plataforma.valueOf(textoPlataforma);
        } catch (IllegalArgumentException e) {
            throw new AccesoDatosException(
                    "La base de datos contiene una plataforma desconocida: " + textoPlataforma, e);
        }
        return new ListaReproduccion(
                resultado.getInt("idLista"),
                resultado.getString("idPlataforma"),
                resultado.getString("nombre"),
                plataforma,
                resultado.getString("region"),
                resultado.getString("urlPlataforma"));
    }
}
