package dao;

import conf.Conexion;
import modelo.Venta;
import modelo.DetalleVenta; // Necesario para la operación de guardar venta completa
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date; // Para java.util.Date
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal; // Importar BigDecimal

/**
 * Clase DAO para la entidad Venta. Proporciona métodos para interactuar con la
 * tabla 'Ventas' y coordinar operaciones con 'Detalle_Ventas'.
 */
public class VentaDAO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VentaDAO.class.getName());

    /**
     * Registra una nueva venta en la base de datos, incluyendo sus detalles.
     * Esta operación se realiza dentro de una transacción para asegurar la
     * atomicidad (si falla alguna parte, se revierte todo).
     * También actualiza el stock de los productos vendidos.
     *
     * @param venta El objeto Venta con los datos principales (sin idVenta).
     * @param detalles Los detalles de la venta (productos, cantidades, etc.).
     * @return true si la venta y sus detalles fueron registrados exitosamente,
     * false en caso contrario.
     */
    public boolean registrarVentaCompleta(Venta venta, List<DetalleVenta> detalles) {
        Connection con = null;
        PreparedStatement psVenta = null;
        PreparedStatement psDetalle = null;
        PreparedStatement psProductoUpdate = null; // Para actualizar el stock

        String sqlVenta = "INSERT INTO Ventas (idCliente, idUsuario, fechaVenta, fechaCreacion, total) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO Detalle_Ventas (idVenta, idProducto, Cantidad, precioUnitario, Subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlProductoUpdate = "UPDATE Productos SET stockDisponible = stockDisponible - ? WHERE idProducto = ?";

        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar la Venta principal
            psVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            psVenta.setInt(1, venta.getIdCliente());
            psVenta.setInt(2, venta.getIdUsuario());
            psVenta.setDate(3, new java.sql.Date(venta.getFechaVenta().getTime()));
            psVenta.setDate(4, new java.sql.Date(venta.getFechaCreacion().getTime()));
            psVenta.setBigDecimal(5, venta.getTotal());

            int filasAfectadasVenta = psVenta.executeUpdate();
            if (filasAfectadasVenta == 0) {
                con.rollback(); // Revertir si la venta no se insertó
                LOGGER.log(Level.WARNING, "No se pudo insertar la venta principal.");
                return false;
            }

            try (ResultSet rs = psVenta.getGeneratedKeys()) {
                if (rs.next()) {
                    venta.setIdVenta(rs.getInt(1)); // Obtener el ID autogenerado
                } else {
                    con.rollback(); // Revertir si no se obtuvo el ID
                    LOGGER.log(Level.WARNING, "No se pudo obtener el ID autogenerado para la venta.");
                    return false;
                }
            }

            // 2. Insertar los detalles de la venta y actualizar el stock
            psDetalle = con.prepareStatement(sqlDetalle);
            psProductoUpdate = con.prepareStatement(sqlProductoUpdate);

            for (DetalleVenta detalle : detalles) {
                // Insertar detalle
                psDetalle.setInt(1, venta.getIdVenta());
                psDetalle.setInt(2, detalle.getIdProducto());
                psDetalle.setInt(3, detalle.getCantidad());
                psDetalle.setBigDecimal(4, detalle.getPrecioUnitario());
                psDetalle.setBigDecimal(5, detalle.getSubtotal());
                psDetalle.addBatch(); // Añadir al lote para ejecución eficiente

                // Actualizar stock
                psProductoUpdate.setInt(1, detalle.getCantidad());
                psProductoUpdate.setInt(2, detalle.getIdProducto());
                psProductoUpdate.addBatch(); // Añadir al lote
            }

            int[] filasDetalleAfectadas = psDetalle.executeBatch();
            int[] filasStockAfectadas = psProductoUpdate.executeBatch();

            // Verificar que todos los detalles y actualizaciones de stock se realizaron correctamente
            for (int count : filasDetalleAfectadas) {
                if (count == 0) {
                    con.rollback();
                    LOGGER.log(Level.WARNING, "Fallo al insertar un detalle de venta.");
                    return false;
                }
            }
            for (int count : filasStockAfectadas) {
                if (count == 0) {
                    con.rollback();
                    LOGGER.log(Level.WARNING, "Fallo al actualizar el stock de un producto.");
                    return false;
                }
            }

            con.commit(); // Confirmar la transacción
            LOGGER.log(Level.INFO, "Venta {0} y sus detalles registrados exitosamente.", venta.getIdVenta());
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); // Revertir en caso de error
                    LOGGER.log(Level.WARNING, "Transacción de venta revertida debido a un error.");
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error al intentar revertir la transacción: " + ex.getMessage(), ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Error al registrar la venta completa: " + e.getMessage(), e);
            return false;
        } finally {
            // Cerrar recursos
            try {
                if (psVenta != null) psVenta.close();
                if (psDetalle != null) psDetalle.close();
                if (psProductoUpdate != null) psProductoUpdate.close();
                if (con != null) con.setAutoCommit(true); // Restaurar auto-commit
                if (con != null) con.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error al cerrar recursos en VentaDAO: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Obtiene una venta por su ID, incluyendo los detalles de cliente y usuario.
     *
     * @param idVenta El ID de la venta a buscar.
     * @return El objeto Venta si se encuentra, o null.
     */
    public Venta obtenerVentaPorId(int idVenta) {
        Venta venta = null;
        String sql = "SELECT v.idVenta, v.idCliente, v.idUsuario, v.fechaVenta, v.fechaCreacion, v.total, " +
                     "c.nombre AS clienteNombre, c.apellido AS clienteApellido, " +
                     "u.nombre AS usuarioNombre, u.apellido AS usuarioApellido " +
                     "FROM Ventas v " +
                     "JOIN Clientes c ON v.idCliente = c.idCliente " +
                     "JOIN Usuarios u ON v.idUsuario = u.idUsuario " +
                     "WHERE v.idVenta = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    venta = new Venta();
                    venta.setIdVenta(rs.getInt("idVenta"));
                    venta.setIdCliente(rs.getInt("idCliente"));
                    venta.setIdUsuario(rs.getInt("idUsuario"));
                    venta.setFechaVenta(rs.getDate("fechaVenta"));
                    venta.setFechaCreacion(rs.getDate("fechaCreacion"));
                    venta.setTotal(rs.getBigDecimal("total"));
                    // Podrías añadir campos al modelo Venta para clienteNombre, usuarioNombre, etc.
                    // o recuperarlos por separado si no quieres sobrecargar el modelo Venta.
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener venta por ID: " + e.getMessage(), e);
        }
        return venta;
    }

    /**
     * Obtiene una lista de todas las ventas.
     *
     * @return Lista de objetos Venta.
     */
    public List<Venta> listarTodasLasVentas() {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.idVenta, v.idCliente, v.idUsuario, v.fechaVenta, v.fechaCreacion, v.total, " +
                     "c.nombre AS clienteNombre, c.apellido AS clienteApellido, " +
                     "u.nombre AS usuarioNombre, u.apellido AS usuarioApellido " +
                     "FROM Ventas v " +
                     "JOIN Clientes c ON v.idCliente = c.idCliente " +
                     "JOIN Usuarios u ON v.idUsuario = u.idUsuario " +
                     "ORDER BY v.fechaVenta DESC, v.idVenta DESC"; // Ordenar por fecha y luego por ID
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Venta venta = new Venta();
                venta.setIdVenta(rs.getInt("idVenta"));
                venta.setIdCliente(rs.getInt("idCliente"));
                venta.setIdUsuario(rs.getInt("idUsuario"));
                venta.setFechaVenta(rs.getDate("fechaVenta"));
                venta.setFechaCreacion(rs.getDate("fechaCreacion"));
                venta.setTotal(rs.getBigDecimal("total"));
                // Aquí también, si el modelo Venta tuviera campos para nombres de cliente/usuario, se podrían setear.
                ventas.add(venta);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar todas las ventas: " + e.getMessage(), e);
        }
        return ventas;
    }

    // Puedes añadir más métodos según sea necesario, como:
    // - listarVentasPorFecha(Date fechaInicio, Date fechaFin)
    // - listarVentasPorCliente(int idCliente)
    // - listarVentasPorUsuario(int idUsuario)
}
