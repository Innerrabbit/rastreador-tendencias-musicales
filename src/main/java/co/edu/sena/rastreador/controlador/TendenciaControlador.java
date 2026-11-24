package co.edu.sena.rastreador.controlador;

import co.edu.sena.rastreador.dto.TendenciaDto;
import co.edu.sena.rastreador.dto.TendenciaSolicitud;
import co.edu.sena.rastreador.dto.TrayectoriaDto;
import co.edu.sena.rastreador.servicio.TendenciaServicio;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de las tendencias (módulo 2): registro manual de capturas y consulta de la
 * trayectoria de una canción (CU-05). La consulta cuelga de /api/canciones/{id} porque
 * une los dos módulos: la canción es del catálogo y su historial, de las tendencias.
 */
@RestController
@RequestMapping("/api")
public class TendenciaControlador {

    private final TendenciaServicio tendenciaServicio;

    public TendenciaControlador(TendenciaServicio tendenciaServicio) {
        this.tendenciaServicio = tendenciaServicio;
    }

    @PostMapping("/tendencias")
    @ResponseStatus(HttpStatus.CREATED)
    public TendenciaDto registrar(@RequestBody TendenciaSolicitud solicitud) {
        return TendenciaDto.desde(tendenciaServicio.registrarTendencia(
                ValidacionEntrada.exigirEntero(solicitud.idCancion(), "idCancion"),
                ValidacionEntrada.exigirEntero(solicitud.idLista(), "idLista"),
                solicitud.fechaCaptura(),
                ValidacionEntrada.exigirEntero(solicitud.posicion(), "posicion"),
                solicitud.streams()));
    }

    @GetMapping("/canciones/{id}/trayectoria")
    public TrayectoriaDto consultarTrayectoria(@PathVariable("id") int idCancion) {
        return tendenciaServicio.consultarTrayectoria(idCancion);
    }

    @DeleteMapping("/tendencias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("id") int idTendencia) {
        tendenciaServicio.eliminarTendencia(idTendencia);
    }
}
