package co.edu.sena.rastreador.controlador;

import co.edu.sena.rastreador.dto.ListaDto;
import co.edu.sena.rastreador.dto.ListaSolicitud;
import co.edu.sena.rastreador.excepcion.RecursoNoEncontradoException;
import co.edu.sena.rastreador.servicio.ListaServicio;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API REST de las listas de reproducción (módulo 2). Delgado: delega en el servicio. */
@RestController
@RequestMapping("/api/listas")
public class ListaControlador {

    private final ListaServicio listaServicio;

    public ListaControlador(ListaServicio listaServicio) {
        this.listaServicio = listaServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListaDto registrar(@RequestBody ListaSolicitud solicitud) {
        return ListaDto.desde(listaServicio.registrarLista(
                solicitud.idPlataforma(), solicitud.nombre(), solicitud.plataforma(),
                solicitud.region(), solicitud.urlPlataforma()));
    }

    @GetMapping
    public List<ListaDto> listar() {
        return listaServicio.listarListas().stream().map(ListaDto::desde).toList();
    }

    @GetMapping("/{id}")
    public ListaDto obtener(@PathVariable("id") int id) {
        return listaServicio.obtenerLista(id)
                .map(ListaDto::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una lista con id " + id + "."));
    }

    @PutMapping("/{id}")
    public ListaDto actualizar(@PathVariable("id") int id, @RequestBody ListaSolicitud solicitud) {
        return ListaDto.desde(listaServicio.actualizarLista(
                id, solicitud.nombre(), solicitud.plataforma(),
                solicitud.region(), solicitud.urlPlataforma()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("id") int id) {
        listaServicio.eliminarLista(id);
    }
}
