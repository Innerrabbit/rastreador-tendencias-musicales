package co.edu.sena.rastreador.servicio;

import co.edu.sena.rastreador.dao.CancionDao;
import co.edu.sena.rastreador.dao.CancionDaoJdbc;
import co.edu.sena.rastreador.dao.ListaReproduccionDao;
import co.edu.sena.rastreador.dao.ListaReproduccionDaoJdbc;
import co.edu.sena.rastreador.dao.TendenciaDao;
import co.edu.sena.rastreador.dao.TendenciaDaoJdbc;
import co.edu.sena.rastreador.dto.CancionResumenDto;
import co.edu.sena.rastreador.dto.PuntoTrayectoriaDto;
import co.edu.sena.rastreador.dto.TipoMovimiento;
import co.edu.sena.rastreador.dto.TrayectoriaDto;
import co.edu.sena.rastreador.excepcion.RecursoNoEncontradoException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.Cancion;
import co.edu.sena.rastreador.modelo.ListaReproduccion;
import co.edu.sena.rastreador.modelo.Tendencia;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lógica de negocio de las tendencias: registro de capturas y consulta de la
 * trayectoria de una canción (CU-05) con el cálculo del movimiento (RF-03).
 *
 * Es el punto donde se integran los dos módulos: usa el {@link CancionDao} del
 * módulo de catálogo para comprobar que la canción existe y para mostrarla en
 * la trayectoria.
 */
public class TendenciaServicio {

    private static final String SIN_CAPTURAS = "La canción no tiene capturas registradas.";
    private static final String UNA_CAPTURA =
            "La canción solo tiene una captura registrada; aún no hay movimiento que comparar.";

    private final TendenciaDao tendenciaDao;
    private final CancionDao cancionDao;
    private final ListaReproduccionDao listaDao;
    private final Clock reloj;

    public TendenciaServicio() {
        this(new TendenciaDaoJdbc(), new CancionDaoJdbc(), new ListaReproduccionDaoJdbc(),
                Clock.systemDefaultZone());
    }

    /** El reloj se recibe como parámetro para poder fijar la hora "actual" en las pruebas. */
    public TendenciaServicio(TendenciaDao tendenciaDao, CancionDao cancionDao,
                             ListaReproduccionDao listaDao, Clock reloj) {
        this.tendenciaDao = tendenciaDao;
        this.cancionDao = cancionDao;
        this.listaDao = listaDao;
        this.reloj = reloj;
    }

    /**
     * Registra la posición de una canción en una lista.
     *
     * @param fechaCaptura momento de la captura; si es nulo se usa la fecha y hora actuales
     * @param streams      reproducciones; opcional
     */
    public Tendencia registrarTendencia(int idCancion, int idLista, LocalDateTime fechaCaptura,
                                        int posicion, Long streams) {
        if (posicion < 1) {
            throw new ReglaNegocioException("La posición debe ser un número entero mayor o igual a 1.");
        }
        if (streams != null && streams < 0) {
            throw new ReglaNegocioException("Los streams no pueden ser negativos.");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime fecha = fechaCaptura == null ? ahora : fechaCaptura.truncatedTo(ChronoUnit.SECONDS);
        if (fecha.isAfter(ahora)) {
            throw new ReglaNegocioException("La fecha de captura no puede ser futura.");
        }

        cancionDao.buscarPorId(idCancion).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe una canción con id " + idCancion + "."));
        listaDao.buscarPorId(idLista).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe una lista con id " + idLista + "."));

        if (tendenciaDao.existeCapturaDelDia(idCancion, idLista, fecha.toLocalDate())) {
            throw new ReglaNegocioException("La canción ya tiene una captura registrada en esa lista "
                    + "para el día " + fecha.toLocalDate() + ".");
        }
        return tendenciaDao.insertar(new Tendencia(idCancion, idLista, fecha, posicion, streams));
    }

    /**
     * Devuelve el historial de una canción en orden cronológico. Cada punto indica
     * si la canción subió, bajó, no cambió o es nueva respecto a su captura anterior
     * <b>en la misma lista</b> (una canción puede estar en varias listas a la vez).
     */
    public TrayectoriaDto consultarTrayectoria(int idCancion) {
        Cancion cancion = cancionDao.buscarPorId(idCancion).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe una canción con id " + idCancion + "."));

        List<Tendencia> historial = tendenciaDao.listarPorCancion(idCancion);
        Map<Integer, String> nombresDeListas = new HashMap<>();
        Map<Integer, Integer> ultimaPosicionPorLista = new HashMap<>();
        List<PuntoTrayectoriaDto> puntos = new ArrayList<>();

        for (Tendencia captura : historial) {
            int idLista = captura.getIdLista();
            Integer posicionAnterior = ultimaPosicionPorLista.get(idLista);
            String nombreLista = nombresDeListas.computeIfAbsent(idLista, this::nombreDeLista);

            puntos.add(new PuntoTrayectoriaDto(
                    captura.getIdTendencia(), idLista, nombreLista, captura.getFechaCaptura(),
                    captura.getPosicion(), captura.getStreams(),
                    calcularMovimiento(posicionAnterior, captura.getPosicion()),
                    calcularDiferencia(posicionAnterior, captura.getPosicion())));

            ultimaPosicionPorLista.put(idLista, captura.getPosicion());
        }
        return new TrayectoriaDto(CancionResumenDto.desde(cancion), puntos, mensajeDe(puntos.size()));
    }

    public void eliminarTendencia(int idTendencia) {
        if (!tendenciaDao.eliminar(idTendencia)) {
            throw new RecursoNoEncontradoException("No existe una captura con id " + idTendencia + ".");
        }
    }

    // ------------------------------------------------------------------
    // Cálculos auxiliares
    // ------------------------------------------------------------------

    /** Una posición numéricamente menor es mejor: pasar del 10 al 4 es SUBIO. */
    private static TipoMovimiento calcularMovimiento(Integer posicionAnterior, int posicionActual) {
        if (posicionAnterior == null) {
            return TipoMovimiento.NUEVA;
        }
        if (posicionActual < posicionAnterior) {
            return TipoMovimiento.SUBIO;
        }
        if (posicionActual > posicionAnterior) {
            return TipoMovimiento.BAJO;
        }
        return TipoMovimiento.SIN_CAMBIO;
    }

    /** Puestos ganados (positivo) o perdidos (negativo); nulo si no hay captura anterior. */
    private static Integer calcularDiferencia(Integer posicionAnterior, int posicionActual) {
        return posicionAnterior == null ? null : posicionAnterior - posicionActual;
    }

    private static String mensajeDe(int cantidadDePuntos) {
        if (cantidadDePuntos == 0) {
            return SIN_CAPTURAS;
        }
        return cantidadDePuntos == 1 ? UNA_CAPTURA : null;
    }

    private String nombreDeLista(int idLista) {
        return listaDao.buscarPorId(idLista)
                .map(ListaReproduccion::getNombre)
                .orElse("(lista no disponible)");
    }
}
