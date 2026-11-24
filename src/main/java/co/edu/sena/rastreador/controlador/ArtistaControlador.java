package co.edu.sena.rastreador.controlador;

import co.edu.sena.rastreador.dto.ArtistaDto;
import co.edu.sena.rastreador.dto.ArtistaSolicitud;
import co.edu.sena.rastreador.excepcion.RecursoNoEncontradoException;
import co.edu.sena.rastreador.modelo.Artista;
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

/** API REST del catálogo de artistas (módulo 1, RF-04). Delgado: delega en el servicio. */
@RestController
@RequestMapping("/api/artistas")
public class ArtistaControlador {

    private final CatalogoServicio catalogoServicio;

    public ArtistaControlador(CatalogoServicio catalogoServicio) {
        this.catalogoServicio = catalogoServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistaDto registrar(@RequestBody ArtistaSolicitud solicitud) {
        return ArtistaDto.desde(catalogoServicio.registrarArtista(
                solicitud.idPlataforma(), solicitud.nombre(), solicitud.urlPlataforma()));
    }

    @GetMapping
    public List<ArtistaDto> listar(@RequestParam(name = "nombre", required = false) String nombre) {
        List<Artista> artistas = (nombre == null || nombre.isBlank())
                ? catalogoServicio.listarArtistas()
                : catalogoServicio.buscarArtistasPorNombre(nombre);
        return artistas.stream().map(ArtistaDto::desde).toList();
    }

    @GetMapping("/{id}")
    public ArtistaDto obtener(@PathVariable("id") int id) {
        return catalogoServicio.obtenerArtista(id)
                .map(ArtistaDto::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un artista con id " + id + "."));
    }

    @PutMapping("/{id}")
    public ArtistaDto actualizar(@PathVariable("id") int id, @RequestBody ArtistaSolicitud solicitud) {
        catalogoServicio.actualizarArtista(
                new Artista(id, null, solicitud.nombre(), solicitud.urlPlataforma()));
        return obtener(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("id") int id) {
        catalogoServicio.eliminarArtista(id);
    }
}
