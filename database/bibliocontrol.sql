-- =====================================================================
-- Bibliocontrol - Sistema de Inventario para Biblioteca y Préstamo de Libros
-- Base de datos MySQL / MariaDB (XAMPP)
-- Importar en phpMyAdmin: pestaña "Importar" -> seleccionar este archivo.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS bibliocontrol
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bibliocontrol;

-- ---------------------------------------------------------------------
-- Usuarios de la biblioteca
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  documento  VARCHAR(20)  NOT NULL,
  nombre     VARCHAR(150) NOT NULL,
  email      VARCHAR(150) NULL,
  telefono   VARCHAR(20)  NULL,
  tipo       VARCHAR(20)  NOT NULL,          -- ESTUDIANTE | DOCENTE
  activo     BIT(1)       NOT NULL DEFAULT b'1',
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuarios_documento (documento)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Libros (información general)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS libros (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  titulo     VARCHAR(200) NOT NULL,
  autor      VARCHAR(150) NOT NULL,
  categoria  VARCHAR(80)  NOT NULL,
  isbn       VARCHAR(13)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_libros_isbn (isbn)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Ejemplares (copias físicas de cada libro)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ejemplares (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  codigo     VARCHAR(20)  NOT NULL,
  estado     VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE',  -- DISPONIBLE | PRESTADO | EN_REPARACION
  ubicacion  VARCHAR(100) NULL,
  libro_id   BIGINT       NOT NULL,
  version    BIGINT       NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ejemplares_codigo (codigo),
  CONSTRAINT fk_ejemplares_libro FOREIGN KEY (libro_id) REFERENCES libros (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Préstamos
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prestamos (
  id                 BIGINT      NOT NULL AUTO_INCREMENT,
  usuario_id         BIGINT      NOT NULL,
  ejemplar_id        BIGINT      NOT NULL,
  fecha_prestamo     DATE        NOT NULL,
  fecha_vencimiento  DATE        NOT NULL,
  fecha_devolucion   DATE        NULL,
  estado             VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',  -- ACTIVO | DEVUELTO
  PRIMARY KEY (id),
  KEY idx_prestamos_estado (estado),
  CONSTRAINT fk_prestamos_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id),
  CONSTRAINT fk_prestamos_ejemplar FOREIGN KEY (ejemplar_id) REFERENCES ejemplares (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Devoluciones
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS devoluciones (
  id                BIGINT       NOT NULL AUTO_INCREMENT,
  prestamo_id       BIGINT       NOT NULL,
  fecha_devolucion  DATE         NOT NULL,
  condicion         VARCHAR(20)  NOT NULL,     -- BUENO | DANADO
  dias_retraso      BIGINT       NOT NULL DEFAULT 0,
  observaciones     VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_devoluciones_prestamo (prestamo_id),
  CONSTRAINT fk_devoluciones_prestamo FOREIGN KEY (prestamo_id) REFERENCES prestamos (id)
) ENGINE=InnoDB;

-- =====================================================================
-- Datos de ejemplo
-- =====================================================================
INSERT IGNORE INTO usuarios (id, documento, nombre, email, telefono, tipo, activo) VALUES
  (1, '1090000001', 'Laura Gómez',   'laura.gomez@fesc.edu.co',   '3001112233', 'ESTUDIANTE', b'1'),
  (2, '1090000002', 'Carlos Pérez',  'carlos.perez@fesc.edu.co',  '3004445566', 'DOCENTE',    b'1'),
  (3, '1090000003', 'Ana Ruiz',      'ana.ruiz@fesc.edu.co',      '3007778899', 'ESTUDIANTE', b'0'),
  (4, '1090000004', 'Miguel Torres', 'miguel.torres@fesc.edu.co', NULL,         'ESTUDIANTE', b'1');

INSERT IGNORE INTO libros (id, titulo, autor, categoria, isbn) VALUES
  (1, 'Cien años de soledad',            'Gabriel García Márquez', 'Novela',        '9780307474728'),
  (2, 'Clean Code',                      'Robert C. Martin',       'Ingeniería de software', '9780132350884'),
  (3, 'Patrones de diseño',              'Erich Gamma y otros',    'Ingeniería de software', '9788478290598'),
  (4, 'Fundamentos de bases de datos',   'Abraham Silberschatz',   'Bases de datos', '9788448156046');

INSERT IGNORE INTO ejemplares (id, codigo, estado, ubicacion, libro_id, version) VALUES
  (1, '9780307474728-1', 'DISPONIBLE',    'Estantería A-3', 1, 0),
  (2, '9780307474728-2', 'DISPONIBLE',    'Estantería A-3', 1, 0),
  (3, '9780132350884-1', 'DISPONIBLE',    'Estantería C-1', 2, 0),
  (4, '9780132350884-2', 'EN_REPARACION', 'Estantería C-1', 2, 0),
  (5, '9788478290598-1', 'DISPONIBLE',    'Estantería C-2', 3, 0),
  (6, '9788448156046-1', 'DISPONIBLE',    'Estantería D-1', 4, 0),
  (7, '9788448156046-2', 'DISPONIBLE',    'Estantería D-1', 4, 0);
