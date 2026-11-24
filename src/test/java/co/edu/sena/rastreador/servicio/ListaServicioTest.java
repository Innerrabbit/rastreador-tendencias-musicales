package co.edu.sena.rastreador.servicio;

import co.edu.sena.rastreador.dao.falso.ListaDaoFalso;
import co.edu.sena.rastreador.excepcion.RecursoNoEncontradoException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.ListaReproduccion;
import co.edu.sena.rastreador.modelo.Plataforma;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de ListaServicio: no usan base de datos ni Spring. */
class ListaServicioTest {

    private ListaDaoFalso listaDao;
    private ListaServicio servicio;

    @BeforeEach
    void preparar() {
        listaDao = new ListaDaoFalso();
        servicio = new ListaServicio(listaDao);
    }

    @Test
    void registrarLista_conDatosValidos_laGuardaLimpiandoEspaciosYMayusculas() {
        ListaReproduccion lista = servicio.registrarLista(
                " LST-1 ", " Top Colombia ", "spotify", " CO ", "  ");

        assertNotNull(lista.getIdLista());
        assertEquals("LST-1", lista.getIdPlataforma());
        assertEquals("Top Colombia", lista.getNombre());
        assertEquals(Plataforma.SPOTIFY, lista.getPlataforma());
        assertEquals("CO", lista.getRegion());
        assertNull(lista.getUrlPlataforma());
    }

    @Test
    void registrarLista_conPlataformaInvalida_indicaLosValoresPermitidos() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarLista("LST-1", "Top", "TIDAL", "CO", null));

        assertTrue(error.getMessage().contains("SPOTIFY"));
        assertTrue(error.getMessage().contains("APPLE_MUSIC"));
    }

    @Test
    void registrarLista_sinCamposObligatorios_lanzaReglaDeNegocio() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarLista(null, "Top", "SPOTIFY", "CO", null));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarLista("LST-1", " ", "SPOTIFY", "CO", null));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarLista("LST-1", "Top", null, "CO", null));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarLista("LST-1", "Top", "SPOTIFY", "", null));
    }

    @Test
    void registrarLista_conNombreMuyLargo_lanzaReglaDeNegocio() {
        String nombreLargo = "x".repeat(256);

        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarLista("LST-1", nombreLargo, "SPOTIFY", "CO", null));
    }

    @Test
    void registrarLista_conIdPlataformaRepetido_lanzaReglaDeNegocio() {
        servicio.registrarLista("LST-1", "Top Colombia", "SPOTIFY", "CO", null);

        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarLista("LST-1", "Otra", "SPOTIFY", "MX", null));
    }

    @Test
    void actualizarLista_cambiaLosCamposPeroNoElIdPlataforma() {
        ListaReproduccion creada = servicio.registrarLista("LST-1", "Top", "SPOTIFY", "CO", null);

        ListaReproduccion actualizada = servicio.actualizarLista(
                creada.getIdLista(), "Top 50 Colombia", "APPLE_MUSIC", "CO-BOG", "https://ejemplo.co");

        assertEquals("LST-1", actualizada.getIdPlataforma());
        assertEquals("Top 50 Colombia", actualizada.getNombre());
        assertEquals(Plataforma.APPLE_MUSIC, actualizada.getPlataforma());
        assertEquals("https://ejemplo.co", actualizada.getUrlPlataforma());
    }

    @Test
    void actualizarLista_inexistente_lanzaRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.actualizarLista(999, "Top", "SPOTIFY", "CO", null));
    }

    @Test
    void eliminarLista_existente_laQuita() {
        ListaReproduccion creada = servicio.registrarLista("LST-1", "Top", "SPOTIFY", "CO", null);

        servicio.eliminarLista(creada.getIdLista());

        assertTrue(servicio.obtenerLista(creada.getIdLista()).isEmpty());
    }

    @Test
    void eliminarLista_inexistente_lanzaRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.eliminarLista(999));
    }
}
