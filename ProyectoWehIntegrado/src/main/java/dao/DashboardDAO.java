package dao;

import conf.Conexion;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAO {

    public int contarProductos() {
        String sql = "SELECT COUNT(*) FROM Productos";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int contarClientes() {
        String sql = "SELECT COUNT(*) FROM Clientes";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int contarCategorias() {
        String sql = "SELECT COUNT(*) FROM Categorias";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int contarVentas() {
        String sql = "SELECT COUNT(*) FROM Ventas";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double obtenerTotalIngresos() {
        String sql = "SELECT SUM(total) FROM Ventas";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int contarUsuarios() {
        String sql = "SELECT COUNT(*) FROM Usuarios";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Double> obtenerVentasMensuales() {
        Map<String, Double> datos = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(v.fechaVenta, '%Y-%m') AS mes, SUM(v.total) AS total "
                   + "FROM Ventas v "
                   + "GROUP BY mes "
                   + "ORDER BY mes";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                datos.put(rs.getString("mes"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerVentasMensuales: " + e.getMessage());
        }
        return datos;
    }

    public Map<String, Integer> obtenerVentasPorCategoria() {
        Map<String, Integer> datos = new LinkedHashMap<>();
        String sql = "SELECT c.descripcion AS categoria, COUNT(dv.idDetalle_Venta) AS cantidad "
                + "FROM Detalle_Ventas dv "
                + "JOIN Productos p ON dv.idProducto = p.idProducto "
                + "JOIN Categorias c ON p.idCategoria = c.idCategoria "
                + "GROUP BY c.descripcion "
                + "ORDER BY cantidad DESC";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                datos.put(rs.getString("categoria"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerVentasPorCategoria: " + e.getMessage());
        }
        return datos;
    }

    public Map<String, Integer> obtenerProductosMasVendidos() {
        Map<String, Integer> datos = new LinkedHashMap<>();
        String sql = "SELECT p.nombre AS producto, SUM(dv.Cantidad) AS cantidad "
                + "FROM Detalle_Ventas dv "
                + "JOIN Productos p ON dv.idProducto = p.idProducto "
                + "GROUP BY p.nombre "
                + "ORDER BY cantidad DESC "
                + "LIMIT 5";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                datos.put(rs.getString("producto"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerProductosMasVendidos: " + e.getMessage());
        }
        return datos;
    }
}
