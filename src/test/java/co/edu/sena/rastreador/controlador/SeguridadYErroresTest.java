package co.edu.sena.rastreador.controlador;

import co.edu.sena.rastreador.configuracion.ConfiguracionSeguridad;
import co.edu.sena.rastreador.dto.CancionResumenDto;
import co.edu.sena.rastreador.dto.PuntoTrayectoriaDto;
import co.edu.sena.rastreador.dto.TipoMovimiento;
import co.edu.sena.rastreador.dto.TrayectoriaDto;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.ListaReproduccion;
import co.edu.sena.rastreador.modelo.Plataforma;
import co.edu.sena.rastreador.servicio.ListaServicio;
import co.edu.sena.rastreador.servicio.TendenciaServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la capa web: seguridad por roles (RNF-04) y traducción de errores a HTTP.
 * Los servicios se reemplazan por dobles (@MockitoBean): aquí no se usa base de datos.
 */
@WebMvcTest(controllers = {ListaControlador.class, TendenciaControlador.class})
@Import(ConfiguracionSeguridad.class)
@TestPropertySource(properties = {
        "ADMIN_CLAVE=clave-admin-prueba",
        "ANALISTA_CLAVE=clave-analista-prueba"
})
class SeguridadYErroresTest {

    private static final String CUERPO_LISTA = """
            {"idPlataforma":"LST-1","nombre":"Top Colombia","plataforma":"SPOTIFY","region":"CO"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListaServicio listaServicio;

    @MockitoBean
    private TendenciaServicio tendenciaServicio;

    // ------------------------------------------------------------------
    // Autenticación y roles
    // ------------------------------------------------------------------

    @Test
    void sinCredenciales_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conClaveIncorrecta_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas").with(httpBasic("admin", "clave-equivocada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void analista_puedeConsultar() throws Exception {
        when(listaServicio.listarListas()).thenReturn(List.of());

        mockMvc.perform(get("/api/listas").with(httpBasic("analista", "clave-analista-prueba")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void analista_noPuedeRegistrar_devuelve403() throws Exception {
        mockMvc.perform(post("/api/listas")
                        .with(httpBasic("analista", "clave-analista-prueba"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_LISTA))
                .andExpect(status().isForbidden());
    }

    @Test
    void analista_noPuedeBorrar_devuelve403() throws Exception {
        mockMvc.perform(delete("/api/listas/1")
                        .with(httpBasic("analista", "clave-analista-prueba")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_puedeRegistrar_devuelve201ConLaListaCreada() throws Exception {
        ListaReproduccion creada = new ListaReproduccion(
                1, "LST-1", "Top Colombia", Plataforma.SPOTIFY, "CO", null);
        when(listaServicio.registrarLista(any(), any(), any(), any(), any())).thenReturn(creada);

        mockMvc.perform(post("/api/listas")
                        .with(httpBasic("admin", "clave-admin-prueba"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_LISTA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idLista").value(1))
                .andExpect(jsonPath("$.plataforma").value("SPOTIFY"));
    }

    @Test
    void admin_puedeBorrar_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/listas/1").with(httpBasic("admin", "clave-admin-prueba")))
                .andExpect(status().isNoContent());

        verify(listaServicio).eliminarLista(1);
    }

    // ------------------------------------------------------------------
    // Traducción de errores
    // ------------------------------------------------------------------

    @Test
    void recursoInexistente_devuelve404ConCuerpoJson() throws Exception {
        when(listaServicio.obtenerLista(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/listas/99").with(httpBasic("analista", "clave-analista-prueba")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe una lista con id 99."));
    }

    @Test
    void reglaDeNegocioIncumplida_devuelve400ConElMensaje() throws Exception {
        when(listaServicio.registrarLista(any(), any(), any(), any(), any()))
                .thenThrow(new ReglaNegocioException("Ya existe una lista con idPlataforma 'LST-1'."));

        mockMvc.perform(post("/api/listas")
                        .with(httpBasic("admin", "clave-admin-prueba"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_LISTA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una lista con idPlataforma 'LST-1'."));
    }

    @Test
    void cuerpoQueNoEsJson_devuelve400() throws Exception {
        mockMvc.perform(post("/api/listas")
                        .with(httpBasic("admin", "clave-admin-prueba"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    void idNoNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/listas/abc").with(httpBasic("analista", "clave-analista-prueba")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tendenciaSinCamposObligatorios_devuelve400() throws Exception {
        mockMvc.perform(post("/api/tendencias")
                        .with(httpBasic("admin", "clave-admin-prueba"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idLista\":1,\"posicion\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El campo 'idCancion' es obligatorio."));
    }

    // ------------------------------------------------------------------
    // Trayectoria (CU-05)
    // ------------------------------------------------------------------

    @Test
    void trayectoria_conAnalista_devuelveLosPuntosConSuMovimiento() throws Exception {
        CancionResumenDto cancion = new CancionResumenDto(
                1, "CAN-1", "Canción Uno", 200000, "Pop", null, List.of());
        PuntoTrayectoriaDto punto = new PuntoTrayectoriaDto(
                10, 1, "Top Colombia", LocalDateTime.of(2026, 11, 16, 8, 0), 4, null,
                TipoMovimiento.SUBIO, 6);
        when(tendenciaServicio.consultarTrayectoria(1))
                .thenReturn(new TrayectoriaDto(cancion, List.of(punto), null));

        mockMvc.perform(get("/api/canciones/1/trayectoria")
                        .with(httpBasic("analista", "clave-analista-prueba")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancion.titulo").value("Canción Uno"))
                .andExpect(jsonPath("$.puntos[0].movimiento").value("SUBIO"))
                .andExpect(jsonPath("$.puntos[0].diferencia").value(6));
    }
}
