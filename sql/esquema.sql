-- ============================================================
-- Rastreador de Tendencias Musicales
-- Script DDL para MySQL 8.0+
-- Evidencia GA7-220501096-AA2-EV01
--
-- Ajustes respecto al diccionario de datos de la EV02:
--   * idPlataforma (UNIQUE) agregado en Artista y ListaReproduccion.
--   * Índice compuesto (idCancion, fechaCaptura) en Tendencia.
-- La tabla Usuario queda pendiente (no la requiere este módulo).
-- ============================================================

CREATE DATABASE IF NOT EXISTS rastreador_tendencias
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE rastreador_tendencias;

-- ------------------------------------------------------------
-- Cancion
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Cancion (
    idCancion     INT           NOT NULL AUTO_INCREMENT,
    idPlataforma  VARCHAR(50)   NOT NULL,
    titulo        VARCHAR(255)  NOT NULL,
    duracionMs    INT           NOT NULL,
    genero        VARCHAR(100)  NULL,
    urlPlataforma VARCHAR(500)  NULL,
    PRIMARY KEY (idCancion),
    CONSTRAINT uq_cancion_idplataforma UNIQUE (idPlataforma)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Artista
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Artista (
    idArtista     INT           NOT NULL AUTO_INCREMENT,
    idPlataforma  VARCHAR(50)   NOT NULL,
    nombre        VARCHAR(255)  NOT NULL,
    urlPlataforma VARCHAR(500)  NULL,
    PRIMARY KEY (idArtista),
    CONSTRAINT uq_artista_idplataforma UNIQUE (idPlataforma)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Cancion_Artista (relación N:M)
-- ON DELETE CASCADE: al borrar una canción o un artista se
-- eliminan sus filas de la tabla de unión.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Cancion_Artista (
    idCancion INT NOT NULL,
    idArtista INT NOT NULL,
    PRIMARY KEY (idCancion, idArtista),
    CONSTRAINT fk_ca_cancion FOREIGN KEY (idCancion)
        REFERENCES Cancion (idCancion) ON DELETE CASCADE,
    CONSTRAINT fk_ca_artista FOREIGN KEY (idArtista)
        REFERENCES Artista (idArtista) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- ListaReproduccion
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ListaReproduccion (
    idLista       INT           NOT NULL AUTO_INCREMENT,
    idPlataforma  VARCHAR(50)   NOT NULL,
    nombre        VARCHAR(255)  NOT NULL,
    plataforma    VARCHAR(50)   NOT NULL,
    region        VARCHAR(50)   NOT NULL,
    urlPlataforma VARCHAR(500)  NULL,
    PRIMARY KEY (idLista),
    CONSTRAINT uq_lista_idplataforma UNIQUE (idPlataforma)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tendencia (histórico de posiciones)
-- No se borra en cascada desde Cancion: se conserva la
-- integridad del historial (ver nota en el README).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Tendencia (
    idTendencia  INT       NOT NULL AUTO_INCREMENT,
    idCancion    INT       NOT NULL,
    idLista      INT       NOT NULL,
    fechaCaptura DATETIME  NOT NULL,
    posicion     INT       NOT NULL,
    streams      BIGINT    NULL,
    PRIMARY KEY (idTendencia),
    CONSTRAINT fk_tendencia_cancion FOREIGN KEY (idCancion)
        REFERENCES Cancion (idCancion),
    CONSTRAINT fk_tendencia_lista FOREIGN KEY (idLista)
        REFERENCES ListaReproduccion (idLista),
    INDEX idx_tendencia_fecha (fechaCaptura),
    INDEX idx_tendencia_cancion_fecha (idCancion, fechaCaptura)
) ENGINE=InnoDB;

-- ============================================================
-- DML inicial (datos de prueba)
-- ============================================================
INSERT INTO Artista (idPlataforma, nombre, urlPlataforma) VALUES
    ('ART-0001', 'Artista de Prueba Uno', NULL),
    ('ART-0002', 'Artista de Prueba Dos', NULL);

INSERT INTO Cancion (idPlataforma, titulo, duracionMs, genero, urlPlataforma) VALUES
    ('CAN-0001', 'Canción de Prueba', 210000, 'Pop', NULL);

INSERT INTO Cancion_Artista (idCancion, idArtista)
SELECT c.idCancion, a.idArtista
FROM Cancion c, Artista a
WHERE c.idPlataforma = 'CAN-0001'
  AND a.idPlataforma IN ('ART-0001', 'ART-0002');
