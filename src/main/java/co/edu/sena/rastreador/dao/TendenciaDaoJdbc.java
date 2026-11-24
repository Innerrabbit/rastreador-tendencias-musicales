package co.edu.sena.rastreador.dao;

import co.edu.sena.rastreador.conexion.ConexionBd;
import co.edu.sena.rastreador.excepcion.AccesoDatosException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.Tendencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de {@link TendenciaDao}.
 * La consulta del historial usa el índice (idCancion, fechaCaptura) del esquema,
 * que es lo que permite cumplir el RNF-03 (búsquedas de historial rápidas).
 */
public class TendenciaDaoJdbc implements TendenciaDao {

    private static final String SQL_INSERTAR =
            "INSERT INTO Tendencia (idCancion, idLista, fechaCaptura, posicion, streams) "
                    + "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_EXISTE_CAPTURA_DEL_DIA =
            "SELECT COUNT(*) FROM Tendencia "
                    + "WHERE idCancion = ? AND idLista = ? AND fechaCaptura >= ? AND fechaCaptura < ?";
    private static final String SQL_POR_CANCION =
            "SELECT idTendencia, idCancion, idLista, fechaCaptura, posicion, streams "
                    + "FROM Tendencia WHERE idCancion = ? ORDER BY fechaCaptura, idTendencia";
    private static final String SQL_ELIMINAR =
            "DELETE FROM Tendencia WHERE idTendencia = ?";

    @Override
    public Tendencia insertar(Tendencia tendencia) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, tendencia.getIdCancion());
            sentencia.setInt(2, tendencia.getIdLista());
            sentencia.setTimestamp(3, Timestamp.valueOf(tendencia.getFechaCaptura()));
            sentencia.setInt(4, tendencia.getPosicion());
            if (tendencia.getStreams() == null) {
                sentencia.setNull(5, Types.BIGINT);
            } else {
                sentencia.setLong(5, tendencia.getStreams());
            }
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    tendencia.setIdTendencia(claves.getInt(1));
                }
            }
            return tendencia;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ReglaNegocioException(
                    "No se pudo registrar la captura: la canción o la lista indicada no existe.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo insertar la captura de tendencia.", e);
        }
    }

    @Override
    public boolean existeCapturaDelDia(int idCancion, int idLista, LocalDate dia) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_EXISTE_CAPTURA_DEL_DIA)) {
            sentencia.setInt(1, idCancion);
            sentencia.setInt(2, idLista);
            sentencia.setTimestamp(3, Timestamp.valueOf(dia.atStartOfDay()));
            sentencia.setTimestamp(4, Timestamp.valueOf(dia.plusDays(1).atStartOfDay()));
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo verificar la captura del día.", e);
        }
    }

    @Override
    public List<Tendencia> listarPorCancion(int idCancion) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_POR_CANCION)) {
            sentencia.setInt(1, idCancion);
            List<Tendencia> historial = new ArrayList<>();
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    historial.add(mapear(resultado));
                }
            }
            return historial;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el historial de la canción.", e);
        }
    }

    @Override
    public boolean eliminar(int idTendencia) {
        try (Connection conexion = ConexionBd.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, idTendencia);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo eliminar la captura de tendencia.", e);
        }
    }

    private Tendencia mapear(ResultSet resultado) throws SQLException {
        long streams = resultado.getLong("streams");
        Long streamsOpcional = resultado.wasNull() ? null : streams;
        Tendencia tendencia = new Tendencia(
                resultado.getInt("idCancion"),
                resultado.getInt("idLista"),
                resultado.getTimestamp("fechaCaptura").toLocalDateTime(),
                resultado.getInt("posicion"),
                streamsOpcional);
        tendencia.setIdTendencia(resultado.getInt("idTendencia"));
        return tendencia;
    }
}
