package co.edu.sena.rastreador.servicio;

import co.edu.sena.rastreador.dao.falso.CancionDaoFalso;
import co.edu.sena.rastreador.dao.falso.ListaDaoFalso;
import co.edu.sena.rastreador.dao.falso.TendenciaDaoFalso;
import co.edu.sena.rastreador.dto.PuntoTrayectoriaDto;
import co.edu.sena.rastreador.dto.TipoMovimiento;
import co.edu.sena.rastreador.dto.TrayectoriaDto;
import co.edu.sena.rastreador.excepcion.RecursoNoEncontradoException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.Cancion;
import co.edu.sena.rastreador.modelo.ListaReproduccion;
import co.edu.sena.rastreador.modelo.Plataforma;
import co.edu.sena.rastreador.modelo.Tendencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de TendenciaServicio: no usan base de datos ni Spring. */
class TendenciaServicioTest {

    // "Ahora" fijo: 20 de noviembre de 2026, 10:00 hora de Bogotá.
    private static final Clock RELOJ =
            Clock.fixed(Instant.parse("2026-11-20T15:00:00Z"), ZoneId.of("America/Bogota"));

    private TendenciaDaoFalso tendenciaDao;
    private TendenciaServicio servicio;
    private int idCancion;
    private int idLista;
    private int idOtraLista;

    @BeforeEach
    void preparar() {
        CancionDaoFalso cancionDao = new CancionDaoFalso();
        ListaDaoFalso listaDao = new ListaDaoFalso();
        tendenciaDao = new TendenciaDaoFalso();
        servicio = new TendenciaServicio(tendenciaDao, cancionDao, listaDao, RELOJ);

        idCancion = cancionDao.agregar(new Cancion("CAN-1", "Canción Uno", 200000, "Pop", null))
                .getIdCancion();
        idLista = listaDao.agregar(new ListaReproduccion(
                "LST-1", "Top Colombia", Plataforma.SPOTIFY, "CO", null)).getIdLista();
        idOtraLista = listaDao.agregar(new ListaReproduccion(
                "LST-2", "Top Global", Plataforma.SPOTIFY, "GLOBAL", null)).getIdLista();
    }

    private static LocalDateTime dia(int diaDelMes) {
        return LocalDateTime.of(2026, 11, diaDelMes, 8, 0);
    }

    // ------------------------------------------------------------------
    // registrarTendencia
    // ------------------------------------------------------------------

    @Test
    void registrarTendencia_conDatosValidos_guardaLaCaptura() {
        Tendencia captura = servicio.registrarTendencia(idCancion, idLista, dia(19), 5, 1000L);

        assertNotNull(captura.getIdTendencia());
        assertEquals(5, captura.getPosicion());
        assertEquals(1000L, captura.getStreams());
        assertEquals(1, tendenciaDao.listarPorCancion(idCancion).size());
    }

    @Test
    void registrarTendencia_sinFecha_usaLaFechaActual() {
        Tendencia captura = servicio.registrarTendencia(idCancion, idLista, null, 5, null);

        assertEquals(LocalDateTime.of(2026, 11, 20, 10, 0, 0), captura.getFechaCaptura());
        assertNull(captura.getStreams());
    }

    @Test
    void registrarTendencia_conPosicionMenorAUno_lanzaReglaDeNegocio() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarTendencia(idCancion, idLista, dia(19), 0, null));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarTendencia(idCancion, idLista, dia(19), -3, null));
    }

    @Test
    void registrarTendencia_conStreamsNegativos_lanzaReglaDeNegocio() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarTendencia(idCancion, idLista, dia(19), 5, -1L));
    }

    @Test
    void registrarTendencia_conFechaFutura_lanzaReglaDeNegocio() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarTendencia(idCancion, idLista, dia(21), 5, null));
        assertTrue(error.getMessage().contains("futura"));
    }

    @Test
    void registrarTendencia_conCancionInexistente_lanzaRecursoNoEncontrado() {
        RecursoNoEncontradoException error = assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.registrarTendencia(999, idLista, dia(19), 5, null));
        assertTrue(error.getMessage().contains("999"));
    }

    @Test
    void registrarTendencia_conListaInexistente_lanzaRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.registrarTendencia(idCancion, 999, dia(19), 5, null));
    }

    @Test
    void registrarTendencia_repetidaEnElMismoDiaYLista_lanzaReglaDeNegocio() {
        servicio.registrarTendencia(idCancion, idLista, dia(19), 5, null);

        // Mismo día (aunque a otra hora) y misma lista: no se permite.
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarTendencia(
                idCancion, idLista, LocalDateTime.of(2026, 11, 19, 20, 0), 6, null));
        // Otra lista el mismo día, y la misma lista otro día: sí se permite.
        servicio.registrarTendencia(idCancion, idOtraLista, dia(19), 9, null);
        servicio.registrarTendencia(idCancion, idLista, dia(18), 7, null);
        assertEquals(3, tendenciaDao.listarPorCancion(idCancion).size());
    }

    // ------------------------------------------------------------------
    // consultarTrayectoria
    // ------------------------------------------------------------------

    @Test
    void consultarTrayectoria_calculaNuevaSubioBajoYSinCambio() {
        // Se registran desordenadas a propósito: la trayectoria debe salir cronológica.
        servicio.registrarTendencia(idCancion, idLista, dia(17), 7, null);
        servicio.registrarTendencia(idCancion, idLista, dia(15), 10, null);
        servicio.registrarTendencia(idCancion, idLista, dia(18), 7, null);
        servicio.registrarTendencia(idCancion, idLista, dia(16), 4, null);

        List<PuntoTrayectoriaDto> puntos = servicio.consultarTrayectoria(idCancion).puntos();

        assertEquals(4, puntos.size());
        assertEquals(TipoMovimiento.NUEVA, puntos.get(0).movimiento());
        assertNull(puntos.get(0).diferencia());
        assertEquals(TipoMovimiento.SUBIO, puntos.get(1).movimiento());   // 10 -> 4
        assertEquals(6, puntos.get(1).diferencia());
        assertEquals(TipoMovimiento.BAJO, puntos.get(2).movimiento());    // 4 -> 7
        assertEquals(-3, puntos.get(2).diferencia());
        assertEquals(TipoMovimiento.SIN_CAMBIO, puntos.get(3).movimiento()); // 7 -> 7
        assertEquals(0, puntos.get(3).diferencia());
    }

    @Test
    void consultarTrayectoria_calculaElMovimientoPorLista() {
        servicio.registrarTendencia(idCancion, idLista, dia(15), 10, null);
        servicio.registrarTendencia(idCancion, idOtraLista, dia(16), 3, null);
        servicio.registrarTendencia(idCancion, idLista, dia(17), 8, null);

        List<PuntoTrayectoriaDto> puntos = servicio.consultarTrayectoria(idCancion).puntos();

        // La primera captura en la otra lista es NUEVA (no se compara con la lista 1).
        assertEquals(TipoMovimiento.NUEVA, puntos.get(1).movimiento());
        // Y el 8 se compara con el 10 de su misma lista, no con el 3 de la otra.
        assertEquals(TipoMovimiento.SUBIO, puntos.get(2).movimiento());
        assertEquals(2, puntos.get(2).diferencia());
    }

    @Test
    void consultarTrayectoria_incluyeLaCancionYElNombreDeLaLista() {
        servicio.registrarTendencia(idCancion, idLista, dia(15), 10, 500L);

        TrayectoriaDto trayectoria = servicio.consultarTrayectoria(idCancion);

        assertEquals("Canción Uno", trayectoria.cancion().titulo());
        assertEquals("Top Colombia", trayectoria.puntos().get(0).nombreLista());
        assertEquals(500L, trayectoria.puntos().get(0).streams());
    }

    @Test
    void consultarTrayectoria_conVariasCapturas_noLlevaMensaje() {
        servicio.registrarTendencia(idCancion, idLista, dia(15), 10, null);
        servicio.registrarTendencia(idCancion, idLista, dia(16), 9, null);

        assertNull(servicio.consultarTrayectoria(idCancion).mensaje());
    }

    @Test
    void consultarTrayectoria_conUnaSolaCaptura_incluyeMensaje() {
        servicio.registrarTendencia(idCancion, idLista, dia(15), 10, null);

        TrayectoriaDto trayectoria = servicio.consultarTrayectoria(idCancion);

        assertEquals(1, trayectoria.puntos().size());
        assertTrue(trayectoria.mensaje().contains("solo tiene una captura"));
    }

    @Test
    void consultarTrayectoria_sinCapturas_devuelveListaVaciaConMensaje() {
        TrayectoriaDto trayectoria = servicio.consultarTrayectoria(idCancion);

        assertTrue(trayectoria.puntos().isEmpty());
        assertTrue(trayectoria.mensaje().contains("no tiene capturas"));
    }

    @Test
    void consultarTrayectoria_conCancionInexistente_lanzaRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.consultarTrayectoria(999));
    }

    // ------------------------------------------------------------------
    // eliminarTendencia
    // ------------------------------------------------------------------

    @Test
    void eliminarTendencia_existente_laQuitaDelHistorial() {
        Tendencia captura = servicio.registrarTendencia(idCancion, idLista, dia(15), 10, null);

        servicio.eliminarTendencia(captura.getIdTendencia());

        assertTrue(tendenciaDao.listarPorCancion(idCancion).isEmpty());
    }

    @Test
    void eliminarTendencia_inexistente_lanzaRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.eliminarTendencia(999));
    }
}
