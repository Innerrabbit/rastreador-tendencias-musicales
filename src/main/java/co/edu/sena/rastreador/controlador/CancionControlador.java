package co.edu.sena.rastreador.controlador;

import co.edu.sena.rastreador.dto.CancionResumenDto;
import co.edu.sena.rastreador.dto.CancionSolicitud;
import co.edu.sena.rastreador.excepcion.RecursoNoEncontradoException;
import co.edu.sena.rastreador.modelo.Cancion;
import co.edu.sena.rastreador.servicio.CatalogoServicio;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API REST del catálogo de canciones (módulo 1, RF-04). Delgado: delega en el servicio. */
@RestController
@RequestMapping("/api/canciones")
public class CancionControlador {

    private final CatalogoServicio catalogoServicio;

    public CancionControlador(CatalogoServicio catalogoServicio) {
        this.catalogoServicio = catalogoServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CancionResumenDto registrar(@RequestBody CancionSolicitud solicitud) {
        Cancion cancion = catalogoServicio.registrarCancion(
                solicitud.idPlataforma(), solicitud.titulo(),
                ValidacionEntrada.exigirEntero(solicitud.duracionMs(), "duracionMs"),
                solicitud.genero(), solicitud.urlPlataforma(), solicitud.artistas());
        return CancionResumenDto.desde(cancion);
    }

    @GetMapping
    public List<CancionResumenDto> listar(@RequestParam(name = "titulo", required = false) String titulo) {
        List<Cancion> canciones = (titulo == null || titulo.isBlank())
                ? catalogoServicio.listarCanciones()
                : catalogoServicio.buscarCancionesPorTitulo(titulo);
        return canciones.stream().map(CancionResumenDto::desde).toList();
    }

    @GetMapping("/{id}")
    public CancionResumenDto obtener(@PathVariable("id") int id) {
        return catalogoServicio.obtenerCancion(id)
                .map(CancionResumenDto::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una canción con id " + id + "."));
    }

    @PutMapping("/{id}")
    public CancionResumenDto actualizar(@PathVariable("id") int id,
                                        @RequestBody CancionSolicitud solicitud) {
        Cancion cancion = new Cancion(null, solicitud.titulo(),
                ValidacionEntrada.exigirEntero(solicitud.duracionMs(), "duracionMs"),
                solicitud.genero(), solicitud.urlPlataforma());
        cancion.setIdCancion(id);
        catalogoServicio.actualizarCancion(cancion);
        return obtener(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("id") int id) {
        catalogoServicio.eliminarCancion(id);
    }
}
