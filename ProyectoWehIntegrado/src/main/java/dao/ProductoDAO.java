package dao;

import conf.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Producto;

public class ProductoDAO {
    
    public List<Producto> listarProductos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.idProducto, p.nombre, p.descripción, p.precioUnitario, p.stockDisponible, "
                   + "c.idCategoria, c.descripcion "
                   + "FROM Productos p "
                   + "JOIN Categorias c ON p.idCategoria = c.idCategoria "
                   + "ORDER BY p.idProducto ASC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto producto = new Producto();
                producto.setIdProducto(rs.getInt("idProducto"));
                producto.setIdCategoria(rs.getInt("idCategoria"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripción"));
                producto.setPrecioUnitario(rs.getDouble("precioUnitario"));
                producto.setStock(rs.getInt("stockDisponible"));
                producto.setCategoriaNombre(rs.getString("descripcion"));  // << nuevo
                lista.add(producto);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Producto> buscarPorNombre(String filtro) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.idProducto, p.nombre, p.descripción, p.precioUnitario, p.stockDisponible, "
                   + "c.idCategoria, c.descripcion "
                   + "FROM Productos p "
                   + "JOIN Categorias c ON p.idCategoria = c.idCategoria "
                   + "WHERE p.nombre LIKE ? OR p.descripción LIKE ?";
        try (Connection con = Conexion.getConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + filtro + "%");
            ps.setString(2, "%" + filtro + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Producto producto = new Producto();
                producto.setIdProducto(rs.getInt("idProducto"));
                producto.setIdCategoria(rs.getInt("idCategoria"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripción"));
                producto.setPrecioUnitario(rs.getDouble("precioUnitario"));
                producto.setStock(rs.getInt("stockDisponible"));
                producto.setCategoriaNombre(rs.getString("descripcion"));  // << nuevo
                lista.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean agregarProducto(Producto producto) {
        String sql = "INSERT INTO Productos (idCategoria, nombre, descripción, precioUnitario, stockDisponible) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, producto.getIdCategoria());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getDescripcion());
            ps.setDouble(4, producto.getPrecioUnitario());
            ps.setInt(5, producto.getStock());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarProducto(int idProducto) {
        String sql = "DELETE FROM Productos WHERE idProducto = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modificarProducto(Producto producto) {
        String sql = "UPDATE Productos SET idCategoria = ?, nombre = ?, descripción = ?, precioUnitario = ?, stockDisponible = ? WHERE idProducto = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, producto.getIdCategoria());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getDescripcion());
            ps.setDouble(4, producto.getPrecioUnitario());
            ps.setInt(5, producto.getStock());
            ps.setInt(6, producto.getIdProducto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
