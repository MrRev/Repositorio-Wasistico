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
    descripcion VARCHAR(50),
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

-- ===========================================================
-- Tabla Usuarios
-- ===========================================================
INSERT INTO Usuarios (dni, nombre, apellido, correo, telefono, direccion, contrasena, idSexo, idRol)
VALUES
('72189592', 'Mauro Estefano', 'Lazaro Pinco', 'lazarooswaldo948@gmail.com', '938132318', 'CASA', '12345', 1, 1);

-- ===========================================================
-- Tabla Productos
-- ===========================================================
INSERT INTO Productos (idCategoria, nombre, descripcion, precioUnitario, stockDisponible)
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
-- Usar la base de datos BodegaDonPablo
USE BodegaDonPablo;

-- 1. Obtener la lista completa de clientes con la descripción de su sexo
--    Muestra el DNI, nombre, apellido, correo y la descripción del sexo de cada cliente.
SELECT
    c.dni,
    c.nombre,
    c.apellido,
    c.correo,
    s.descripcion AS sexo_descripcion
FROM
    Clientes c
INNER JOIN
    Sexos s ON c.idSexo = s.idSexo;

-- 2. Obtener la lista completa de usuarios con la descripción de su rol y sexo
--    Muestra el DNI, nombre, apellido, correo, la descripción del rol y la descripción del sexo de cada usuario.
SELECT
    u.dni,
    u.nombre,
    u.apellido,
    u.correo,
    r.descripcion AS rol_descripcion,
    s.descripcion AS sexo_descripcion
FROM
    Usuarios u
INNER JOIN
    Roles r ON u.idRol = r.idRol
INNER JOIN
    Sexos s ON u.idSexo = s.idSexo;

-- 3. Obtener la lista de productos con la descripción de su categoría
--    Muestra el nombre del producto, su precio unitario, stock disponible y la descripción de su categoría.
SELECT
    p.nombre AS producto_nombre,
    c.descripcion AS categoria_descripcion,
    p.precioUnitario,
    p.stockDisponible
FROM
    Productos p
INNER JOIN
    Categorias c ON p.idCategoria = c.idCategoria;

-- 4. Obtener el detalle de todas las ventas, incluyendo información del cliente y el usuario que realizó la venta
--    Muestra el ID de la venta, la fecha, el total, y los nombres completos del cliente y el usuario.
SELECT
    v.idVenta,
    v.fechaVenta,
    v.total,
    cl.nombre AS nombre_cliente,
    cl.apellido AS apellido_cliente,
    us.nombre AS nombre_usuario,
    us.apellido AS apellido_usuario
FROM
    Ventas v
INNER JOIN
    Clientes cl ON v.idCliente = cl.idCliente
INNER JOIN
    Usuarios us ON v.idUsuario = us.idUsuario;

-- 5. Obtener el detalle de los productos vendidos en cada venta
--    Muestra el ID de la venta, el nombre del producto, la cantidad vendida, el precio unitario en el momento de la venta y el subtotal.
SELECT
    dv.idVenta,
    p.nombre AS producto_nombre,
    dv.Cantidad,
    dv.precioUnitario AS precio_venta_unitario,
    dv.Subtotal
FROM
    Detalle_Ventas dv
INNER JOIN
    Productos p ON dv.idProducto = p.idProducto;

-- 6. Obtener el total de ventas por cliente
--    Muestra el nombre completo del cliente y la suma total de todas sus compras.
SELECT
    c.nombre,
    c.apellido,
    SUM(v.total) AS total_comprado
FROM
    Clientes c
INNER JOIN
    Ventas v ON c.idCliente = v.idCliente
GROUP BY
    c.idCliente, c.nombre, c.apellido
ORDER BY
    total_comprado DESC;

-- 7. Obtener la cantidad total vendida de cada producto
--    Muestra el nombre del producto y la suma de las cantidades vendidas de ese producto.
SELECT
    p.nombre AS producto_nombre,
    SUM(dv.Cantidad) AS cantidad_total_vendida
FROM
    Productos p
INNER JOIN
    Detalle_Ventas dv ON p.idProducto = dv.idProducto
GROUP BY
    p.idProducto, p.nombre
ORDER BY
    cantidad_total_vendida DESC;

-- 8. Obtener las ventas realizadas por un usuario específico (ej. 'Mauro Estefano Lazaro Pinco')
--    Muestra los detalles de la venta (ID, fecha, total) y el nombre del cliente.
SELECT
    v.idVenta,
    v.fechaVenta,
    v.total,
    cl.nombre AS nombre_cliente,
    cl.apellido AS apellido_cliente
FROM
    Ventas v
INNER JOIN
    Usuarios u ON v.idUsuario = u.idUsuario
INNER JOIN
    Clientes cl ON v.idCliente = cl.idCliente
WHERE
    u.nombre = 'Mauro Estefano' AND u.apellido = 'Lazaro Pinco';

-- 9. Obtener los productos más vendidos por categoría
--    Muestra la categoría, el nombre del producto y la cantidad total vendida.
SELECT
    cat.descripcion AS categoria,
    p.nombre AS producto_nombre,
    SUM(dv.Cantidad) AS cantidad_total_vendida
FROM
    Detalle_Ventas dv
INNER JOIN
    Productos p ON dv.idProducto = p.idProducto
INNER JOIN
    Categorias cat ON p.idCategoria = cat.idCategoria
GROUP BY
    cat.descripcion, p.nombre
ORDER BY
    categoria, cantidad_total_vendida DESC;

-- 10. Obtener el valor total de ventas por categoría de producto
--     Muestra la descripción de la categoría y la suma de los subtotales de los productos de esa categoría.
SELECT
    c.descripcion AS categoria_nombre,
    SUM(dv.Subtotal) AS total_ventas_categoria
FROM
    Detalle_Ventas dv
INNER JOIN
    Productos p ON dv.idProducto = p.idProducto
INNER JOIN
    Categorias c ON p.idCategoria = c.idCategoria
GROUP BY
    c.descripcion
ORDER BY
    total_ventas_categoria DESC;

-- 11. Obtener clientes que no han realizado ninguna compra (usando LEFT JOIN)
--     Muestra el nombre y apellido de los clientes que no tienen registros en la tabla Ventas.
SELECT
    c.nombre,
    c.apellido
FROM
    Clientes c
LEFT JOIN
    Ventas v ON c.idCliente = v.idCliente
WHERE
    v.idVenta IS NULL;

-- 12. Obtener productos que nunca han sido vendidos (usando LEFT JOIN)
--     Muestra el nombre del producto y su categoría.
SELECT
    p.nombre AS producto_nombre,
    cat.descripcion AS categoria
FROM
    Productos p
LEFT JOIN
    Detalle_Ventas dv ON p.idProducto = dv.idProducto
INNER JOIN
    Categorias cat ON p.idCategoria = cat.idCategoria
WHERE
    dv.idDetalle_Venta IS NULL;