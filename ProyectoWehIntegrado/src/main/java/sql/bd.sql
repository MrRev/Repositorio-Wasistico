-- Eliminar la base de datos si existe (para entornos de desarrollo/pruebas)
-- DROP DATABASE IF EXISTS BodegaDonPablo;

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS BodegaDonPablo;
USE BodegaDonPablo;

-- Tabla Sexos: Almacena géneros de clientes y usuarios
CREATE TABLE Sexos (
    idSexo INT PRIMARY KEY AUTO_INCREMENT,
    descripcion VARCHAR(20) NOT NULL
);

-- Tabla Roles: Define roles para los usuarios del sistema
CREATE TABLE Roles (
    idRol INT PRIMARY KEY AUTO_INCREMENT,
    descripcion VARCHAR(50) NOT NULL
);

-- Tabla Clientes: Registra información de clientes
CREATE TABLE Clientes (
    idCliente INT PRIMARY KEY AUTO_INCREMENT,
    dni VARCHAR(8) NOT NULL UNIQUE,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    correo VARCHAR(100),
    telefono VARCHAR(12),
    direccion VARCHAR(150),
    idSexo INT,
    FOREIGN KEY (idSexo) REFERENCES Sexos(idSexo),
    INDEX idx_dni (dni),
    INDEX idx_correo (correo),
    INDEX idx_nombre (nombre)
);

-- Tabla Usuarios: Almacena información de usuarios del sistema
CREATE TABLE Usuarios (
    idUsuario INT PRIMARY KEY AUTO_INCREMENT,
    dni VARCHAR(8) NOT NULL UNIQUE,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    correo VARCHAR(100),
    telefono VARCHAR(12),
    direccion VARCHAR(150),
    contrasena VARCHAR(100) NOT NULL,
    idSexo INT,
    idRol INT,
    FOREIGN KEY (idSexo) REFERENCES Sexos(idSexo),
    FOREIGN KEY (idRol) REFERENCES Roles(idRol),
    INDEX idx_dni (dni),
    INDEX idx_correo (correo),
    INDEX idx_nombre (nombre)
);

-- Tabla Categorias: Almacena categorías de productos
CREATE TABLE Categorias (
    idCategoria INT PRIMARY KEY AUTO_INCREMENT,
    descripcion VARCHAR(50) NOT NULL UNIQUE
);

-- Tabla Productos: Registra información de productos
CREATE TABLE Productos (
    idProducto INT PRIMARY KEY AUTO_INCREMENT,
    idCategoria INT,
    nombre VARCHAR(100) NOT NULL,
    descripción VARCHAR(50),
    precioUnitario DECIMAL(10,2) NOT NULL,
    stockDisponible INT NOT NULL,
    FOREIGN KEY (idCategoria) REFERENCES Categorias(idCategoria),
    INDEX idx_nombre (nombre)
);

-- Tabla Ventas: Registra las ventas realizadas
CREATE TABLE Ventas (
    idVenta INT PRIMARY KEY AUTO_INCREMENT,
    idCliente INT,
    idUsuario INT,
    fechaVenta DATE NOT NULL,
    fechaCreacion DATE NOT NULL, -- Se elimina DEFAULT CURDATE()
    total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (idCliente) REFERENCES Clientes(idCliente),
    FOREIGN KEY (idUsuario) REFERENCES Usuarios(idUsuario)
);

-- Tabla Detalle_Ventas: Detalla los productos de cada venta
CREATE TABLE Detalle_Ventas (
    idDetalle_Venta INT PRIMARY KEY AUTO_INCREMENT,
    idVenta INT,
    idProducto INT,
    Cantidad INT NOT NULL,
    precioUnitario DECIMAL(10,2) NOT NULL,
    Subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (idVenta) REFERENCES Ventas(idVenta),
    FOREIGN KEY (idProducto) REFERENCES Productos(idProducto)
);

-- POBLADO DE DATOS
-- ===========================================================
-- Tabla Sexos
-- ===========================================================
INSERT INTO Sexos (descripcion) VALUES
('Masculino'),
('Femenino');

-- ===========================================================
-- Tabla Roles
-- ===========================================================
INSERT INTO Roles (descripcion) VALUES
('Administrador'),
('Vendedor');

-- ===========================================================
-- Tabla Categorias
-- ===========================================================
INSERT INTO Categorias (descripcion) VALUES
('Bebidas'),
('Snacks'),
('Lácteos'),
('Carnes'),
('Panadería'),
('Frutas'),
('Verduras'),
('Conservas');

-- ===========================================================
-- Tabla Clientes
-- ===========================================================
INSERT INTO Clientes (dni, nombre, apellido, correo, telefono, direccion, idSexo)
VALUES
('12345678', 'Luis', 'Torres', 'luis.torres@mail.com', '987654321', 'Av. Lima 123', 1),
('23456789', 'Ana', 'Ramirez', 'ana.ramirez@mail.com', '912345678', 'Jr. Cusco 456', 2),
('34567890', 'Pedro', 'Soto', 'pedro.soto@mail.com', '954321789', 'Calle Piura 789', 1),
('45678901', 'Maria', 'Perez', 'maria.perez@mail.com', '987123654', 'Av. Arequipa 321', 2),
('56789012', 'Jorge', 'Cruz', 'jorge.cruz@mail.com', '911223344', 'Jr. Libertad 111', 1),
('67890123', 'Sofia', 'Gomez', 'sofia.gomez@mail.com', '913334455', 'Calle Central 888', 2),
('78901234', 'Carlos', 'Mendoza', 'carlos.mendoza@mail.com', '922334455', 'Av. Progreso 999', 1),
('89012345', 'Paula', 'Lopez', 'paula.lopez@mail.com', '944556677', 'Jr. Lima 444', 2),
('90123456', 'Jose', 'Salazar', 'jose.salazar@mail.com', '955667788', 'Av. Grau 222', 1),
('11223344', 'Lucia', 'Aguilar', 'lucia.aguilar@mail.com', '966778899', 'Calle Lince 555', 2);

-- ===========================================================
-- Tabla Usuarios
-- ===========================================================
INSERT INTO Usuarios (dni, nombre, apellido, correo, telefono, direccion, contrasena, idSexo, idRol)
VALUES
('11111111', 'Admin', 'Root', 'admin@bodega.com', '900111111', 'Oficina Principal', 'admin123', 1, 1),
('22222222', 'Pedro', 'Vargas', 'pedro@bodega.com', '900222222', 'Sucursal Sur', 'pedro123', 1, 2),
('33333333', 'Mariana', 'Castro', 'mariana@bodega.com', '900333333', 'Sucursal Norte', 'mariana123', 2, 2);

-- ===========================================================
-- Tabla Productos
-- ===========================================================
INSERT INTO Productos (idCategoria, nombre, descripción, precioUnitario, stockDisponible)
VALUES
(1, 'Coca Cola 500ml', 'Gaseosa', 3.50, 200),
(1, 'Pepsi 500ml', 'Gaseosa', 3.30, 150),
(2, 'Papas Fritas', 'Snacks', 2.00, 300),
(2, 'Galletas Oreo', 'Galleta', 1.50, 250),
(3, 'Leche Gloria', 'Lácteo', 4.50, 100),
(3, 'Yogurt Fresa', 'Lácteo', 2.80, 120),
(4, 'Pollo Entero', 'Carne Blanca', 12.00, 50),
(4, 'Carne Molida', 'Carne Roja', 14.00, 60),
(5, 'Pan Francés', 'Pan', 0.30, 500),
(5, 'Pan Integral', 'Pan', 0.50, 300),
(6, 'Manzana', 'Fruta', 2.20, 200),
(6, 'Plátano', 'Fruta', 1.80, 180),
(7, 'Tomate', 'Verdura', 1.20, 150),
(7, 'Lechuga', 'Verdura', 1.00, 130),
(8, 'Atún Lata', 'Conserva', 4.00, 80),
(8, 'Sardina Lata', 'Conserva', 3.00, 90);

-- ===========================================================
-- Tabla Ventas
-- ===========================================================
INSERT INTO Ventas (idCliente, idUsuario, fechaVenta, fechaCreacion, total)
VALUES
(1, 2, '2024-01-10', '2024-01-10', 35.00),
(2, 2, '2024-01-11', '2024-01-11', 45.00),
(3, 3, '2024-01-12', '2024-01-12', 25.00),
(4, 2, '2024-01-13', '2024-01-13', 55.00),
(5, 2, '2024-01-15', '2024-01-15', 65.00),
(6, 2, '2024-01-16', '2024-01-16', 15.00),
(7, 3, '2024-01-17', '2024-01-17', 28.00),
(8, 2, '2024-01-18', '2024-01-18', 37.00),
(9, 2, '2024-01-19', '2024-01-19', 42.00),
(10, 2, '2024-01-20', '2024-01-20', 52.00);

-- ===========================================================
-- Tabla Detalle_Ventas
-- ===========================================================
INSERT INTO Detalle_Ventas (idVenta, idProducto, Cantidad, precioUnitario, Subtotal)
VALUES
(1, 1, 5, 3.50, 17.50),
(1, 3, 5, 2.00, 10.00),
(2, 2, 8, 3.30, 26.40),
(2, 4, 5, 1.50, 7.50),
(3, 5, 3, 4.50, 13.50),
(3, 6, 4, 2.80, 11.20),
(4, 7, 2, 12.00, 24.00),
(4, 8, 2, 14.00, 28.00),
(5, 9, 20, 0.30, 6.00),
(5, 10, 20, 0.50, 10.00),
(6, 11, 5, 2.20, 11.00),
(6, 12, 3, 1.80, 5.40),
(7, 13, 4, 1.20, 4.80),
(7, 14, 5, 1.00, 5.00),
(8, 15, 4, 4.00, 16.00),
(9, 16, 4, 3.00, 12.00);
