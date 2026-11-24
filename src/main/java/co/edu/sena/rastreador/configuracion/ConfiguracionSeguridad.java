package co.edu.sena.rastreador.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la API (RNF-04) con usuarios en memoria y autenticación HTTP Basic.
 *
 * Reglas de acceso:
 *   - Consultas (GET):                 cualquier usuario autenticado (ADMIN o ANALISTA).
 *   - Registrar, modificar y borrar:   solo ADMIN.
 *
 * Las claves se toman de las variables de entorno ADMIN_CLAVE y ANALISTA_CLAVE; nunca
 * están en el código. En memoria solo se conserva su hash BCrypt.
 */
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    public static final String USUARIO_ADMIN = "admin";
    public static final String USUARIO_ANALISTA = "analista";

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http) throws Exception {
        http
                // Sin sesiones ni cookies: cada petición lleva sus credenciales, por lo que
                // no aplica la protección CSRF (que existe para proteger sesiones de navegador).
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorDeClaves() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService usuariosEnMemoria(
            PasswordEncoder codificador,
            @Value("${ADMIN_CLAVE:}") String claveAdmin,
            @Value("${ANALISTA_CLAVE:}") String claveAnalista) {
        exigirClave("ADMIN_CLAVE", claveAdmin);
        exigirClave("ANALISTA_CLAVE", claveAnalista);

        UserDetails admin = User.withUsername(USUARIO_ADMIN)
                .password(codificador.encode(claveAdmin))
                .roles("ADMIN")
                .build();
        UserDetails analista = User.withUsername(USUARIO_ANALISTA)
                .password(codificador.encode(claveAnalista))
                .roles("ANALISTA")
                .build();
        return new InMemoryUserDetailsManager(admin, analista);
    }

    private static void exigirClave(String variable, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno " + variable
                    + " (clave del usuario). Defínala antes de iniciar la aplicación.");
        }
    }
}
