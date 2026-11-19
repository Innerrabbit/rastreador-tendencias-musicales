package co.edu.sena.rastreador.dao;

import co.edu.sena.rastreador.conexion.ConexionBd;
import co.edu.sena.rastreador.excepcion.AccesoDatosException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.Artista;

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
 * Implementación JDBC de {@link ArtistaDao}.
 * Usa PreparedStatement en todas las consultas para evitar inyección SQL
 * y try-with-resources para cerrar los recursos automáticamente.
 */
public class ArtistaDaoJdbc implements ArtistaDao {

    private static final String SQL_INSERTAR =
            "INSERT INTO Artista (idPlataforma, nombre, urlPlataforma) VALUES (?, ?, ?)";
    private static final String SQL_SELECCIONAR =
            "SELECT idArtista, idPlataforma, nombre, urlPlataforma FROM Artista";
    private static final String SQL_ACTUALIZAR =
            "UPDATE Artista SET nombre = ?, urlPlataforma = ? WHERE idArtista = ?";
    private static final String SQL_ELIMINAR =
            "DELETE FROM Artista WHERE idArtista = ?";

    @Override
    public Artista insertar(Artista artista) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, artista.getIdPlataforma());
            sentencia.setString(2, artista.getNombre());
            sentencia.setString(3, artista.getUrlPlataforma());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    artista.setIdArtista(claves.getInt(1));
                }
            }
            return artista;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ReglaNegocioException(
                    "Ya existe un artista con el idPlataforma indicado.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo insertar el artista.", e);
        }
    }

    @Override
    public Optional<Artista> buscarPorId(int idArtista) {
        List<Artista> resultado = consultar(SQL_SELECCIONAR + " WHERE idArtista = ?", idArtista);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public Optional<Artista> buscarPorIdPlataforma(String idPlataforma) {
        List<Artista> resultado = consultar(SQL_SELECCIONAR + " WHERE idPlataforma = ?", idPlataforma);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<Artista> buscarPorNombre(String nombre) {
        return consultar(SQL_SELECCIONAR + " WHERE nombre LIKE ? ORDER BY nombre",
                "%" + nombre + "%");
    }

    @Override
    public List<Artista> listarTodos() {
        return consultar(SQL_SELECCIONAR + " ORDER BY nombre", null);
    }

    @Override
    public boolean actualizar(Artista artista) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            sentencia.setString(1, artista.getNombre());
            sentencia.setString(2, artista.getUrlPlataforma());
            sentencia.setInt(3, artista.getIdArtista());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el artista.", e);
        }
    }

    @Override
    public boolean eliminar(int idArtista) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, idArtista);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo eliminar el artista.", e);
        }
    }

    /** Ejecuta una consulta con a lo sumo un parámetro y mapea las filas a Artista. */
    private List<Artista> consultar(String sql, Object parametro) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (parametro != null) {
                sentencia.setObject(1, parametro);
            }
            List<Artista> artistas = new ArrayList<>();
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    artistas.add(mapear(resultado));
                }
            }
            return artistas;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar los artistas.", e);
        }
    }

    private Artista mapear(ResultSet resultado) throws SQLException {
        return new Artista(
                resultado.getInt("idArtista"),
                resultado.getString("idPlataforma"),
                resultado.getString("nombre"),
                resultado.getString("urlPlataforma"));
    }
}
