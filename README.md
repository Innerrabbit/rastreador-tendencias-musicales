# Rastreador de Tendencias Musicales: módulo de catálogo

Evidencia GA7-220501096-AA2-EV01 · Codificación de módulos del software
Tecnólogo en Análisis y Desarrollo de Software (ficha 3186608) · Autor: Juan Pablo Millán Cruz

## Descripción

El Rastreador de Tendencias Musicales captura cada día las posiciones de las canciones en listas de reproducción de plataformas de streaming y guarda su historial para analizar su evolución.

Este repositorio contiene el **módulo de catálogo (RF-04)**: gestión de artistas y canciones, con la relación muchos a muchos entre ambos, sin registros duplicados. Incluye las cuatro operaciones del CRUD (inserción, consulta, actualización y eliminación) con conexión a MySQL mediante **JDBC puro** y el patrón DAO. La interfaz es un menú de consola.

## Requisitos

- JDK 17 o superior
- Apache Maven 3.8 o superior
- MySQL 8.0 o superior

## Base de datos

Ejecute el script `sql/esquema.sql` en MySQL. Crea la base `rastreador_tendencias`, las tablas y unos pocos datos de prueba:

```
mysql -u TU_USUARIO -p < sql/esquema.sql
```

## Configuración

La conexión se lee de variables de entorno; las credenciales nunca se guardan en el código ni en el repositorio. El archivo `config.properties.example` muestra qué valores definir.

Linux o macOS:
```
export BD_URL="jdbc:mysql://localhost:3306/rastreador_tendencias?useUnicode=true&characterEncoding=UTF-8"
export BD_USUARIO="tu_usuario"
export BD_CLAVE="tu_contrasena"
```

Windows (PowerShell):
```
$env:BD_URL="jdbc:mysql://localhost:3306/rastreador_tendencias?useUnicode=true&characterEncoding=UTF-8"
$env:BD_USUARIO="tu_usuario"
$env:BD_CLAVE="tu_contrasena"
```

## Compilar y ejecutar

```
mvn compile exec:java
```

Si en Windows las tildes se ven mal en la consola, ejecute antes `chcp 65001`.

## Estructura de paquetes

Paquete base: `co.edu.sena.rastreador`

| Paquete | Contenido | Equivalente en el diagrama de clases |
|---|---|---|
| `modelo` | `Artista`, `Cancion` | paquete model |
| `conexion` | `ConexionBd` | (infraestructura JDBC) |
| `dao` | interfaces `ArtistaDao`, `CancionDao` y sus implementaciones `...DaoJdbc` | paquete repository |
| `servicio` | `CatalogoServicio` | `CatalogoService` |
| `consola` | `MenuCatalogo` | presentación (menú de consola) |
| `excepcion` | `AccesoDatosException`, `ReglaNegocioException` | |

En JDBC puro, los "repositorios" del diagrama de clases se implementan con el patrón DAO. Como los repositorios son interfaces, más adelante pueden reemplazarse por Spring Data JPA sin cambiar el servicio.

## Estándares de codificación

- Paquetes en minúscula, sin tildes ni guiones.
- Clases e interfaces en UpperCamelCase.
- Métodos y variables en lowerCamelCase, con nombres descriptivos que no usan tildes ni eñes.
- Constantes en MAYUSCULAS_CON_GUION_BAJO.
- Consultas con `PreparedStatement` (evitan la inyección SQL) y recursos cerrados con try-with-resources.

## Funcionalidades (CRUD)

| Operación | Artista | Canción |
|---|---|---|
| Inserción | registrar artista | registrar canción con sus artistas (transacción única) |
| Consulta | listar, por nombre, por idPlataforma | listar, por título, por idPlataforma |
| Actualización | nombre y URL | título, duración, género y URL |
| Eliminación | eliminar artista | eliminar canción |

Reglas de negocio: `idPlataforma` único para canciones y artistas; título, nombre e `idPlataforma` obligatorios; duración positiva; una canción debe tener al menos un artista registrado.

Notas de funcionamiento:
- Eliminar una canción o un artista borra sus filas en `Cancion_Artista` (`ON DELETE CASCADE`).
- Una canción que ya tiene historial en la tabla `Tendencia` no puede eliminarse: se conserva la integridad del historial y el menú muestra un aviso.
- Al actualizar una canción no se modifican sus artistas.

## Artefactos usados como base

Requisitos funcionales y no funcionales, casos de uso, diagrama de clases, modelo de datos (diccionario de datos), diagrama de despliegue y el cronograma de seis semanas de los términos de referencia (EV01 de la ficha técnica).

No se elaboraron historias de usuario ni prototipos de interfaz para este proyecto: el alcance de esta evidencia es el backend (modelo, acceso a datos y lógica) con un menú de consola, por lo que se usaron como base los requisitos y los casos de uso.

## Ajustes al modelo de datos

Respecto al diccionario de datos original (EV02), el script `sql/esquema.sql` adopta:
- Columna `idPlataforma` única en `Artista` y en `ListaReproduccion`, para distinguir artistas homónimos e identificar cada lista ante la API.
- Índice compuesto `(idCancion, fechaCaptura)` en `Tendencia`, para cumplir el RNF-03 (consultas de historial rápidas).

Pendiente: la tabla `Usuario` (autenticación, RNF-04) aún no está decidida, porque este módulo no la requiere.

## Control de versiones

Git y GitHub, con commits pequeños y mensajes claros. La contraseña de la base de datos no se versiona.
