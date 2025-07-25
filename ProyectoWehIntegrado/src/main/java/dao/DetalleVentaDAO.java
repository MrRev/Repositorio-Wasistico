package dao;

import conf.Conexion;
import modelo.DetalleVenta;
import modelo.Producto;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal; // Importar BigDecimal
import modelo.DetalleVenta;

/**
 * Clase DAO para la entidad DetalleVenta. Proporciona métodos para interactuar
 * con la tabla 'Detalle_Ventas'.
 */
public class DetalleVentaDAO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DetalleVentaDAO.class.getName());

    /**
     * Inserta un nuevo detalle de venta en la base de datos. Este método es
     * principalmente auxiliar y se usa dentro de una transacción de venta
     * completa.
     *
     * @param detalle El objeto DetalleVenta a insertar.
     * @return true si el detalle fue agregado exitosamente, false en caso
     * contrario.
     */
    public boolean agregarDetalleVenta(DetalleVenta detalle) {
        String sql = "INSERT INTO Detalle_Ventas (idVenta, idProducto, Cantidad, precioUnitario, Subtotal) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, detalle.getIdVenta());
            ps.setInt(2, detalle.getIdProducto());
            ps.setInt(3, detalle.getCantidad());
            ps.setBigDecimal(4, detalle.getPrecioUnitario());
            ps.setBigDecimal(5, detalle.getSubtotal());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        detalle.setIdDetalleVenta(rs.getInt(1));
                    }
                }
                LOGGER.log(Level.INFO, "Detalle de venta agregado exitosamente con ID: {0}", detalle.getIdDetalleVenta());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al agregar detalle de venta: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Obtiene todos los detalles para una venta específica. Incluye información
     * del producto (nombre, descripción, categoría).
     *
     * @param idVenta El ID de la venta cuyos detalles se desean obtener.
     * @return Una lista de objetos DetalleVenta con información del producto.
     */
    public List<DetalleVenta> obtenerDetallesPorIdVenta(int idVenta) {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT dv.idDetalle_Venta, dv.idVenta, dv.idProducto, dv.Cantidad, dv.precioUnitario, dv.Subtotal, "
                + "p.nombre AS productoNombre, p.descripción AS productoDescripcion, p.precioUnitario AS productoPrecioUnitario, p.stockDisponible AS productoStock, "
                + "c.descripcion AS categoriaNombre "
                + "FROM Detalle_Ventas dv "
                + "JOIN Productos p ON dv.idProducto = p.idProducto "
                + "JOIN Categorias c ON p.idCategoria = c.idCategoria "
                + "WHERE dv.idVenta = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setIdDetalleVenta(rs.getInt("idDetalle_Venta"));
                    detalle.setIdVenta(rs.getInt("idVenta"));
                    detalle.setIdProducto(rs.getInt("idProducto"));
                    detalle.setCantidad(rs.getInt("Cantidad"));
                    detalle.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                    detalle.setSubtotal(rs.getBigDecimal("Subtotal"));

                    // Si tu modelo DetalleVenta tuviera un campo para el objeto Producto,
                    // podrías poblarlo aquí. Por ahora, solo se obtiene la descripción.
                    // Producto producto = new Producto();
                    // producto.setNombre(rs.getString("productoNombre"));
                    // producto.setDescripcion(rs.getString("productoDescripcion"));
                    // detalle.setProducto(producto); // Asumiendo que DetalleVenta tiene setProducto(Producto)
                    detalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener detalles de venta por ID de venta: " + e.getMessage(), e);
        }
        return detalles;
    }

    // Puedes añadir más métodos si necesitas, por ejemplo, para eliminar o actualizar
    // detalles de venta individuales, aunque esto es menos común en un flujo de ventas.
}
