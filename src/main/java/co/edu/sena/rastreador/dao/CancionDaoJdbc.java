package co.edu.sena.rastreador.dao;

import co.edu.sena.rastreador.conexion.ConexionBd;
import co.edu.sena.rastreador.excepcion.AccesoDatosException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.Artista;
import co.edu.sena.rastreador.modelo.Cancion;

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
 * Implementación JDBC de {@link CancionDao}.
 * La inserción de la canción y de sus relaciones con los artistas se ejecuta
 * en una sola transacción: o se guarda todo o no se guarda nada.
 */
public class CancionDaoJdbc implements CancionDao {

    private static final String SQL_INSERTAR =
            "INSERT INTO Cancion (idPlataforma, titulo, duracionMs, genero, urlPlataforma) "
                    + "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_INSERTAR_RELACION =
            "INSERT INTO Cancion_Artista (idCancion, idArtista) VALUES (?, ?)";
    private static final String SQL_SELECCIONAR =
            "SELECT idCancion, idPlataforma, titulo, duracionMs, genero, urlPlataforma FROM Cancion";
    private static final String SQL_ARTISTAS_DE_CANCION =
            "SELECT a.idArtista, a.idPlataforma, a.nombre, a.urlPlataforma "
                    + "FROM Artista a INNER JOIN Cancion_Artista ca ON ca.idArtista = a.idArtista "
                    + "WHERE ca.idCancion = ? ORDER BY a.nombre";
    private static final String SQL_ACTUALIZAR =
            "UPDATE Cancion SET titulo = ?, duracionMs = ?, genero = ?, urlPlataforma = ? "
                    + "WHERE idCancion = ?";
    private static final String SQL_ELIMINAR =
            "DELETE FROM Cancion WHERE idCancion = ?";

    @Override
    public Cancion insertar(Cancion cancion) {
        try (Connection conexion = ConexionBd.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                insertarCancion(conexion, cancion);
                insertarRelaciones(conexion, cancion);
                conexion.commit();
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
            return cancion;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ReglaNegocioException("No se pudo registrar la canción: el idPlataforma "
                    + "ya existe o alguno de los artistas indicados no existe.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo insertar la canción.", e);
        }
    }

    @Override
    public Optional<Cancion> buscarPorId(int idCancion) {
        List<Cancion> resultado = consultar(SQL_SELECCIONAR + " WHERE idCancion = ?", idCancion);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public Optional<Cancion> buscarPorIdPlataforma(String idPlataforma) {
        List<Cancion> resultado = consultar(SQL_SELECCIONAR + " WHERE idPlataforma = ?", idPlataforma);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<Cancion> buscarPorTitulo(String titulo) {
        return consultar(SQL_SELECCIONAR + " WHERE titulo LIKE ? ORDER BY titulo",
                "%" + titulo + "%");
    }

    @Override
    public List<Cancion> listarTodas() {
        return consultar(SQL_SELECCIONAR + " ORDER BY titulo", null);
    }

    @Override
    public boolean actualizar(Cancion cancion) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            sentencia.setString(1, cancion.getTitulo());
            sentencia.setInt(2, cancion.getDuracionMs());
            sentencia.setString(3, cancion.getGenero());
            sentencia.setString(4, cancion.getUrlPlataforma());
            sentencia.setInt(5, cancion.getIdCancion());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar la canción.", e);
        }
    }

    @Override
    public boolean eliminar(int idCancion) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, idCancion);
            return sentencia.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ReglaNegocioException("No se puede eliminar la canción porque tiene "
                    + "historial de tendencias asociado.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo eliminar la canción.", e);
        }
    }

    private void insertarCancion(Connection conexion, Cancion cancion) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, cancion.getIdPlataforma());
            sentencia.setString(2, cancion.getTitulo());
            sentencia.setInt(3, cancion.getDuracionMs());
            sentencia.setString(4, cancion.getGenero());
            sentencia.setString(5, cancion.getUrlPlataforma());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    cancion.setIdCancion(claves.getInt(1));
                }
            }
        }
    }

    private void insertarRelaciones(Connection conexion, Cancion cancion) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR_RELACION)) {
            for (Artista artista : cancion.getArtistas()) {
                sentencia.setInt(1, cancion.getIdCancion());
                sentencia.setInt(2, artista.getIdArtista());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    /** Ejecuta una consulta con a lo sumo un parámetro y carga los artistas de cada canción. */
    private List<Cancion> consultar(String sql, Object parametro) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (parametro != null) {
                sentencia.setObject(1, parametro);
            }
            List<Cancion> canciones = new ArrayList<>();
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    canciones.add(mapear(resultado));
                }
            }
            for (Cancion cancion : canciones) {
                cancion.setArtistas(cargarArtistas(conexion, cancion.getIdCancion()));
            }
            return canciones;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar las canciones.", e);
        }
    }

    private List<Artista> cargarArtistas(Connection conexion, int idCancion) throws SQLException {
        List<Artista> artistas = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ARTISTAS_DE_CANCION)) {
            sentencia.setInt(1, idCancion);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    artistas.add(new Artista(
                            resultado.getInt("idArtista"),
                            resultado.getString("idPlataforma"),
                            resultado.getString("nombre"),
                            resultado.getString("urlPlataforma")));
                }
            }
        }
        return artistas;
    }

    private Cancion mapear(ResultSet resultado) throws SQLException {
        Cancion cancion = new Cancion(
                resultado.getString("idPlataforma"),
                resultado.getString("titulo"),
                resultado.getInt("duracionMs"),
                resultado.getString("genero"),
                resultado.getString("urlPlataforma"));
        cancion.setIdCancion(resultado.getInt("idCancion"));
        return cancion;
    }
}
