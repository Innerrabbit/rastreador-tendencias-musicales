# Rastreador de Tendencias Musicales

Evidencia GA8-220501096-AA1-EV02 · Módulos integrados (sobre la GA7-220501096-AA2-EV01 · Codificación de módulos)
Tecnólogo en Análisis y Desarrollo de Software (ficha 3186608) · Autor: Juan Pablo Millán Cruz · Versión 2.0.0

## Descripción

El Rastreador de Tendencias Musicales captura las posiciones de las canciones en listas de reproducción de plataformas de streaming y guarda su historial para analizar su evolución.

Esta versión integra **dos módulos** y los expone como una **API REST** con Spring Boot:

1. **Catálogo (RF-04)**, entregado en la EV01: artistas y canciones con relación muchos a muchos, sin duplicados.
2. **Listas y tendencias (RF-03, CU-05)**, nuevo: listas de reproducción, registro de capturas de posición y consulta de la trayectoria de una canción (si subió, bajó, no cambió o es nueva).

Los módulos se integran en `TendenciaServicio`, que usa el `CancionDao` del catálogo para validar la canción y mostrarla en su trayectoria. El acceso a datos sigue siendo **JDBC puro** con el patrón DAO: Spring se agregó solo como capa web y de seguridad. El menú de consola de la EV01 se conserva.

## Requisitos

- JDK 17 o superior
- Apache Maven 3.8 o superior
- MySQL 8.0 o superior
- Postman (opcional, para ejecutar los casos de prueba de la carpeta `postman/`)

## Base de datos

Ejecute el script `sql/esquema.sql` en MySQL. Crea la base `rastreador_tendencias`, las tablas y unos pocos datos de prueba. Esta versión **no modifica el esquema** de la EV01.

```
mysql -u TU_USUARIO -p < sql/esquema.sql
```

## Configuración

Todo lo secreto se lee de variables de entorno; nunca se guarda en el código ni en el repositorio. El archivo `config.properties.example` muestra qué valores definir.

| Variable | Uso |
|---|---|
| `BD_URL`, `BD_USUARIO`, `BD_CLAVE` | Conexión a MySQL |
| `ADMIN_CLAVE` | Clave del usuario `admin` (rol ADMIN) |
| `ANALISTA_CLAVE` | Clave del usuario `analista` (rol ANALISTA) |

Windows (PowerShell):
```
$env:BD_URL="jdbc:mysql://localhost:3306/rastreador_tendencias?useUnicode=true&characterEncoding=UTF-8"
$env:BD_USUARIO="tu_usuario"
$env:BD_CLAVE="tu_contrasena"
$env:ADMIN_CLAVE="una_clave_para_admin"
$env:ANALISTA_CLAVE="una_clave_para_analista"
```

Linux o macOS: use `export` en lugar de `$env:`.

Las propiedades de Spring están en `src/main/resources/application.properties` (puerto 8080 y solo escucha en `127.0.0.1`).

## Compilar y ejecutar

API web (Spring Boot):
```
mvn clean package
java -jar target/rastreador-tendencias-musicales-2.0.0.jar
```
También sirve `mvn spring-boot:run`. La API queda en `http://localhost:8080`. Si falta una variable de entorno o no se puede conectar a MySQL, la aplicación se detiene al arrancar con un mensaje claro.

Menú de consola de la EV01 (sin cambios):
```
mvn compile exec:java
```

Pruebas automáticas:
```
mvn test
```

## Seguridad (RNF-04)

Autenticación HTTP Basic con **usuarios en memoria** (Spring Security); las claves se guardan solo como hash BCrypt y vienen de variables de entorno. No hay sesiones ni cookies.

| Operación | ADMIN | ANALISTA | Sin credenciales |
|---|---|---|---|
| Consultar (`GET`) | Sí | Sí | 401 |
| Registrar, modificar y eliminar (`POST`, `PUT`, `DELETE`) | Sí | 403 | 401 |

La tabla `Usuario` con JWT del diseño original queda **pendiente** para una fase futura (ver "Decisiones y alcance").

## API REST

Todas las rutas empiezan por `/api`. Los cuerpos son JSON. Los errores devuelven `{estado, error, mensaje, fecha}`: 400 (dato inválido o regla de negocio), 404 (no existe), 500 (error de acceso a datos).

| Método | Ruta | Rol mínimo | Descripción |
|---|---|---|---|
| POST | `/api/artistas` | ADMIN | Registrar artista |
| GET | `/api/artistas` (`?nombre=`) | ANALISTA | Listar o buscar por nombre |
| GET | `/api/artistas/{id}` | ANALISTA | Consultar un artista |
| PUT | `/api/artistas/{id}` | ADMIN | Actualizar nombre y URL |
| DELETE | `/api/artistas/{id}` | ADMIN | Eliminar artista |
| POST | `/api/canciones` | ADMIN | Registrar canción con sus artistas |
| GET | `/api/canciones` (`?titulo=`) | ANALISTA | Listar o buscar por título |
| GET | `/api/canciones/{id}` | ANALISTA | Consultar una canción |
| PUT | `/api/canciones/{id}` | ADMIN | Actualizar título, duración, género y URL |
| DELETE | `/api/canciones/{id}` | ADMIN | Eliminar canción (no si tiene historial) |
| POST | `/api/listas` | ADMIN | Registrar lista de reproducción |
| GET | `/api/listas` | ANALISTA | Listar las listas |
| GET | `/api/listas/{id}` | ANALISTA | Consultar una lista |
| PUT | `/api/listas/{id}` | ADMIN | Actualizar nombre, plataforma, región y URL |
| DELETE | `/api/listas/{id}` | ADMIN | Eliminar lista (no si tiene historial) |
| POST | `/api/tendencias` | ADMIN | Registrar una captura de posición |
| DELETE | `/api/tendencias/{id}` | ADMIN | Eliminar una captura |
| GET | `/api/canciones/{id}/trayectoria` | ANALISTA | Trayectoria de una canción (CU-05) |

Ejemplo de captura: `POST /api/tendencias` con `{"idCancion":1,"idLista":1,"fechaCaptura":"2026-11-19T12:00:00","posicion":4,"streams":12345}`. Si no se envía `fechaCaptura`, se usa la fecha y hora actuales.

Reglas del módulo de listas y tendencias:
- La plataforma es `SPOTIFY` o `APPLE_MUSIC`; el `idPlataforma` de la lista es único.
- La posición debe ser un entero mayor o igual a 1; los streams, si se envían, no pueden ser negativos.
- La fecha de captura no puede ser futura.
- Solo puede haber **una captura por canción y lista cada día** (el registro es idempotente).
- El movimiento se calcula contra la captura anterior **de la misma lista**: una posición menor es mejor (del puesto 10 al 4 es `SUBIO`, con diferencia +6).
- Una lista con historial no puede eliminarse, igual que una canción con historial.

## Estructura de paquetes

Paquete base: `co.edu.sena.rastreador`

| Paquete | Contenido | Equivalente en el diagrama de clases |
|---|---|---|
| `AplicacionWeb` | Punto de entrada de Spring Boot | |
| `modelo` | `Artista`, `Cancion`, `ListaReproduccion`, `Tendencia`, `Plataforma` | model |
| `conexion` | `ConexionBd` | (infraestructura JDBC) |
| `dao` | `ArtistaDao`, `CancionDao`, `ListaReproduccionDao`, `TendenciaDao` y sus implementaciones `...DaoJdbc` | repository |
| `servicio` | `CatalogoServicio`, `ListaServicio`, `TendenciaServicio` | service |
| `dto` | `ArtistaDto`, `CancionResumenDto`, `ListaDto`, `TendenciaDto`, `PuntoTrayectoriaDto`, `TrayectoriaDto`, `TipoMovimiento` y los `...Solicitud` | dto |
| `controlador` | `ArtistaControlador`, `CancionControlador`, `ListaControlador`, `TendenciaControlador`, `ManejadorErrores` | controller |
| `configuracion` | `ConfiguracionBeans`, `ConfiguracionSeguridad` | |
| `consola` | `MenuCatalogo` (EV01) | presentación de consola |
| `excepcion` | `AccesoDatosException`, `ReglaNegocioException`, `RecursoNoEncontradoException` | |

Los DAO y servicios no llevan anotaciones de Spring: `ConfiguracionBeans` los crea y los conecta por constructor. Los controladores no exponen entidades, solo DTO.

## Pruebas

- **Unitarias (JUnit 5, sin base de datos):** `TendenciaServicioTest` y `ListaServicioTest` usan DAO en memoria (`dao/falso`). Cubren validaciones, el cálculo de movimiento y la trayectoria.
- **Capa web:** `SeguridadYErroresTest` (MockMvc) verifica 401, 403 y la traducción de errores a 400 y 404.
- **Extremo a extremo:** la colección `postman/rastreador-pruebas.postman_collection.json` ejecuta los casos CP-01 a CP-46 contra la API y la base reales. Defina `adminClave` y `analistaClave` en las variables de la colección y ejecútela con Collection Runner; crea sus propios datos y los elimina al final.

## Despliegue

El sistema se ejecuta **de forma local** (cualquier sistema operativo con JRE 17 o superior), como indica el diagrama de despliegue: aplicación en `localhost:8080` y MySQL en `localhost:3306`. No se desplegó en un servicio en la nube: las opciones evaluadas (por ejemplo Heroku) ya no ofrecen un nivel gratuito y quedan fuera del presupuesto y el alcance del proyecto.

## Decisiones y alcance

- **Alcance:** catálogo y listas/tendencias. Quedan fuera: captura automática desde Spotify (RF-01, RF-02, CU-01), reporte de las 5 canciones que más suben (RF-06) y gráficas (RF-05).
- **Spotify:** no se integra la API oficial por limitaciones técnicas y de presupuesto (modo de desarrollo restringido, acceso ampliado con requisitos de uso y suscripción Premium). Como evolución futura se plantea implementar `MusicApiClient` con la API pública de Deezer. Por ahora las capturas se registran manualmente.
- **Autenticación:** usuarios en memoria (ADMIN y ANALISTA) en lugar de la tabla `Usuario` con JWT. Es una desviación consciente respecto a la ficha técnica, para esta evidencia.
- **Códigos HTTP:** toda regla de negocio incumplida responde 400 (también los duplicados); se puede refinar a 409 más adelante.
- **Cambio sobre el código de la EV01:** únicamente en `CatalogoServicio`, donde los casos "no existe" ahora lanzan `RecursoNoEncontradoException` (subclase de `ReglaNegocioException`, por lo que el menú de consola funciona igual). El `pom.xml` pasó a usar el padre de Spring Boot, conservando el driver MySQL 8.4.0.

## Estándares de codificación

- Paquetes en minúscula, sin tildes ni guiones.
- Clases e interfaces en UpperCamelCase.
- Métodos y variables en lowerCamelCase, con nombres descriptivos que no usan tildes ni eñes.
- Constantes en MAYUSCULAS_CON_GUION_BAJO.
- Consultas con `PreparedStatement` (evitan la inyección SQL) y recursos cerrados con try-with-resources.

## Ajustes al modelo de datos

Respecto al diccionario de datos original (EV02), el script `sql/esquema.sql` adopta:
- Columna `idPlataforma` única en `Artista` y en `ListaReproduccion`, para distinguir artistas homónimos e identificar cada lista ante la API.
- Índice compuesto `(idCancion, fechaCaptura)` en `Tendencia`, para cumplir el RNF-03 (consultas de historial rápidas).

Reglas de borrado: al eliminar una canción o un artista se borran sus filas de `Cancion_Artista` (`ON DELETE CASCADE`); una canción o una lista con historial en `Tendencia` no puede eliminarse.

## Control de versiones

Git y GitHub, con commits pequeños y mensajes claros. Ninguna contraseña se versiona.
