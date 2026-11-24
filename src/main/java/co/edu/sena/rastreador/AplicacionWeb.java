package co.edu.sena.rastreador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación web (API REST).
 *
 * Se ejecuta con:  java -jar target/rastreador-tendencias-musicales-2.0.0.jar
 * El menú de consola de la EV01 sigue disponible en {@link Aplicacion}.
 */
@SpringBootApplication
public class AplicacionWeb {

    public static void main(String[] args) {
        SpringApplication.run(AplicacionWeb.class, args);
    }
}
