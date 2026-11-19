package co.edu.sena.rastreador.consola;

import co.edu.sena.rastreador.excepcion.AccesoDatosException;
import co.edu.sena.rastreador.excepcion.ReglaNegocioException;
import co.edu.sena.rastreador.modelo.Artista;
import co.edu.sena.rastreador.modelo.Cancion;
import co.edu.sena.rastreador.servicio.CatalogoServicio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Menú de consola para gestionar el catálogo de artistas y canciones.
 * Es la capa de presentación de esta evidencia: no contiene reglas de negocio,
 * solo lee datos, invoca al servicio y muestra resultados.
 */
public class MenuCatalogo {

    private static final String BORRAR_CAMPO = "-";

    private final CatalogoServicio servicio;
    private final Scanner lector;

    public MenuCatalogo(CatalogoServicio servicio) {
        this.servicio = servicio;
        this.lector = new Scanner(System.in);
    }

    /** Ejecuta el ciclo principal del menú hasta que el usuario elige salir. */
    public void iniciar() {
        boolean continuar = true;
        try {
            while (continuar) {
                mostrarMenu();
                int opcion = leerEntero("Seleccione una opción: ");
                try {
                    continuar = ejecutarOpcion(opcion);
                } catch (ReglaNegocioException e) {
                    System.out.println("Aviso: " + e.getMessage());
                } catch (AccesoDatosException e) {
                    System.out.println("Error de acceso a datos: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.out.println("Detalle técnico: " + e.getCause().getMessage());
                    }
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Entrada finalizada.");
        }
        System.out.println("Hasta pronto.");
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("=== Rastreador de Tendencias Musicales: Catálogo ===");
        System.out.println("-- Artistas --");
        System.out.println(" 1. Registrar artista");
        System.out.println(" 2. Listar artistas");
        System.out.println(" 3. Buscar artistas por nombre");
        System.out.println(" 4. Buscar artista por idPlataforma");
        System.out.println(" 5. Actualizar artista");
        System.out.println(" 6. Eliminar artista");
        System.out.println("-- Canciones --");
        System.out.println(" 7. Registrar canción");
        System.out.println(" 8. Listar canciones");
        System.out.println(" 9. Buscar canciones por título");
        System.out.println("10. Buscar canción por idPlataforma");
        System.out.println("11. Actualizar canción");
        System.out.println("12. Eliminar canción");
        System.out.println(" 0. Salir");
    }

    /** @return false si el usuario eligió salir */
    private boolean ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> registrarArtista();
            case 2 -> mostrarArtistas(servicio.listarArtistas());
            case 3 -> buscarArtistasPorNombre();
            case 4 -> buscarArtistaPorIdPlataforma();
            case 5 -> actualizarArtista();
            case 6 -> eliminarArtista();
            case 7 -> registrarCancion();
            case 8 -> mostrarCanciones(servicio.listarCanciones());
            case 9 -> buscarCancionesPorTitulo();
            case 10 -> buscarCancionPorIdPlataforma();
            case 11 -> actualizarCancion();
            case 12 -> eliminarCancion();
            case 0 -> {
                return false;
            }
            default -> System.out.println("Opción no válida.");
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Opciones de artistas
    // ------------------------------------------------------------------

    private void registrarArtista() {
        String idPlataforma = leerTexto("idPlataforma del artista: ");
        String nombre = leerTexto("Nombre: ");
        String url = leerTexto("URL en la plataforma (Enter para omitir): ");
        Artista artista = servicio.registrarArtista(idPlataforma, nombre, url);
        System.out.println("Artista registrado con id " + artista.getIdArtista() + ".");
    }

    private void buscarArtistasPorNombre() {
        String nombre = leerTexto("Nombre o parte del nombre: ");
        mostrarArtistas(servicio.buscarArtistasPorNombre(nombre));
    }

    private void buscarArtistaPorIdPlataforma() {
        String idPlataforma = leerTexto("idPlataforma: ");
        Optional<Artista> artista = servicio.buscarArtistaPorIdPlataforma(idPlataforma);
        if (artista.isPresent()) {
            System.out.println(formatearArtista(artista.get()));
        } else {
            System.out.println("No se encontró un artista con ese idPlataforma.");
        }
    }

    private void actualizarArtista() {
        int id = leerEntero("id del artista a actualizar: ");
        Optional<Artista> encontrado = servicio.obtenerArtista(id);
        if (encontrado.isEmpty()) {
            System.out.println("No existe un artista con id " + id + ".");
            return;
        }
        Artista artista = encontrado.get();
        System.out.println("Actual: " + formatearArtista(artista));
        System.out.println("(Enter conserva el valor actual; '" + BORRAR_CAMPO
                + "' borra un campo opcional)");
        artista.setNombre(leerTextoConValorActual("Nombre", artista.getNombre()));
        artista.setUrlPlataforma(leerOpcionalConValorActual("URL", artista.getUrlPlataforma()));
        servicio.actualizarArtista(artista);
        System.out.println("Artista actualizado.");
    }

    private void eliminarArtista() {
        int id = leerEntero("id del artista a eliminar: ");
        if (confirmar("Se eliminará también su relación con las canciones. ¿Continuar? (s/n): ")) {
            servicio.eliminarArtista(id);
            System.out.println("Artista eliminado.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // ------------------------------------------------------------------
    // Opciones de canciones
    // ------------------------------------------------------------------

    private void registrarCancion() {
        String idPlataforma = leerTexto("idPlataforma de la canción: ");
        String titulo = leerTexto("Título: ");
        int duracionMs = leerEntero("Duración en milisegundos: ");
        String genero = leerTexto("Género (Enter para omitir): ");
        String url = leerTexto("URL en la plataforma (Enter para omitir): ");
        String artistas = leerTexto("idPlataforma de los artistas, separados por coma: ");
        List<String> idsArtistas = new ArrayList<>(Arrays.asList(artistas.split(",")));
        Cancion cancion = servicio.registrarCancion(
                idPlataforma, titulo, duracionMs, genero, url, idsArtistas);
        System.out.println("Canción registrada con id " + cancion.getIdCancion() + ".");
    }

    private void buscarCancionesPorTitulo() {
        String titulo = leerTexto("Título o parte del título: ");
        mostrarCanciones(servicio.buscarCancionesPorTitulo(titulo));
    }

    private void buscarCancionPorIdPlataforma() {
        String idPlataforma = leerTexto("idPlataforma: ");
        Optional<Cancion> cancion = servicio.buscarCancionPorIdPlataforma(idPlataforma);
        if (cancion.isPresent()) {
            System.out.println(formatearCancion(cancion.get()));
        } else {
            System.out.println("No se encontró una canción con ese idPlataforma.");
        }
    }

    private void actualizarCancion() {
        int id = leerEntero("id de la canción a actualizar: ");
        Optional<Cancion> encontrada = servicio.obtenerCancion(id);
        if (encontrada.isEmpty()) {
            System.out.println("No existe una canción con id " + id + ".");
            return;
        }
        Cancion cancion = encontrada.get();
        System.out.println("Actual: " + formatearCancion(cancion));
        System.out.println("(Enter conserva el valor actual; '" + BORRAR_CAMPO
                + "' borra un campo opcional)");
        cancion.setTitulo(leerTextoConValorActual("Título", cancion.getTitulo()));
        cancion.setDuracionMs(leerEnteroConValorActual(
                "Duración en milisegundos", cancion.getDuracionMs()));
        cancion.setGenero(leerOpcionalConValorActual("Género", cancion.getGenero()));
        cancion.setUrlPlataforma(leerOpcionalConValorActual("URL", cancion.getUrlPlataforma()));
        servicio.actualizarCancion(cancion);
        System.out.println("Canción actualizada.");
    }

    private void eliminarCancion() {
        int id = leerEntero("id de la canción a eliminar: ");
        if (confirmar("¿Seguro que desea eliminar la canción? (s/n): ")) {
            servicio.eliminarCancion(id);
            System.out.println("Canción eliminada.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // ------------------------------------------------------------------
    // Presentación
    // ------------------------------------------------------------------

    private void mostrarArtistas(List<Artista> artistas) {
        if (artistas.isEmpty()) {
            System.out.println("No hay artistas para mostrar.");
            return;
        }
        for (Artista artista : artistas) {
            System.out.println(formatearArtista(artista));
        }
        System.out.println("Total: " + artistas.size());
    }

    private void mostrarCanciones(List<Cancion> canciones) {
        if (canciones.isEmpty()) {
            System.out.println("No hay canciones para mostrar.");
            return;
        }
        for (Cancion cancion : canciones) {
            System.out.println(formatearCancion(cancion));
        }
        System.out.println("Total: " + canciones.size());
    }

    private String formatearArtista(Artista artista) {
        return "id " + artista.getIdArtista()
                + " | " + artista.getIdPlataforma()
                + " | " + artista.getNombre()
                + " | URL: " + valorOVacio(artista.getUrlPlataforma());
    }

    private String formatearCancion(Cancion cancion) {
        String nombresArtistas = cancion.getArtistas().stream()
                .map(Artista::getNombre)
                .collect(Collectors.joining(", "));
        return "id " + cancion.getIdCancion()
                + " | " + cancion.getIdPlataforma()
                + " | " + cancion.getTitulo()
                + " | " + formatearDuracion(cancion.getDuracionMs())
                + " | Género: " + valorOVacio(cancion.getGenero())
                + " | Artistas: " + nombresArtistas;
    }

    private String formatearDuracion(int duracionMs) {
        int segundosTotales = duracionMs / 1000;
        return String.format("%d:%02d", segundosTotales / 60, segundosTotales % 60);
    }

    private String valorOVacio(String valor) {
        return valor == null ? "-" : valor;
    }

    // ------------------------------------------------------------------
    // Lectura de datos
    // ------------------------------------------------------------------

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return lector.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número entero válido.");
            }
        }
    }

    private boolean confirmar(String mensaje) {
        String respuesta = leerTexto(mensaje).toLowerCase();
        return respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí");
    }

    private String leerTextoConValorActual(String campo, String valorActual) {
        String texto = leerTexto(campo + " [" + valorActual + "]: ");
        return texto.isEmpty() ? valorActual : texto;
    }

    private String leerOpcionalConValorActual(String campo, String valorActual) {
        String texto = leerTexto(campo + " [" + valorOVacio(valorActual) + "]: ");
        if (texto.isEmpty()) {
            return valorActual;
        }
        return texto.equals(BORRAR_CAMPO) ? null : texto;
    }

    private int leerEnteroConValorActual(String campo, int valorActual) {
        while (true) {
            String texto = leerTexto(campo + " [" + valorActual + "]: ");
            if (texto.isEmpty()) {
                return valorActual;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número entero válido.");
            }
        }
    }
}
