package dao;

import conf.Conexion;
import java.io.Serializable; // Importar Serializable
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;

import modelo.Producto;

/**
 * Clase DAO para la entidad Producto. Proporciona métodos para interactuar con
 * la tabla 'Productos' en la base de datos.
 */
public class ProductoDAO implements Serializable { // Implementar Serializable

    private static final long serialVersionUID = 1L; // Añadir serialVersionUID
    private static final Logger LOGGER = Logger.getLogger(ProductoDAO.class.getName());

    /**
     * Obtiene una lista de todos los productos registrados en la base de datos.
     * Incluye la descripción de la categoría. Los resultados se ordenan por
     * idProducto.
     *
     * @return Una lista de objetos Producto. Retorna una lista vacía si no hay
     * productos o si ocurre un error.
     */
    public List<Producto> listarProductos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.idProducto, p.nombre, p.descripción, p.precioUnitario, p.stockDisponible, "
                + "c.idCategoria, c.descripcion "
                + "FROM Productos p "
                + "JOIN Categorias c ON p.idCategoria = c.idCategoria "
                + "ORDER BY p.idProducto ASC";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto producto = new Producto();
                producto.setIdProducto(rs.getInt("idProducto"));
                producto.setIdCategoria(rs.getInt("idCategoria"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripción"));
                producto.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                producto.setStock(rs.getInt("stockDisponible"));
                producto.setCategoriaNombre(rs.getString("descripcion"));
                lista.add(producto);
            }
            LOGGER.log(Level.INFO, "Se cargaron {0} productos.", lista.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar productos: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Busca productos por nombre o descripción que contengan el filtro dado.
     * Incluye la descripción de la categoría.
     *
     * @param filtro El término de búsqueda.
     * @return Una lista de objetos Producto que coinciden con el filtro.
     */
    public List<Producto> buscarPorNombre(String filtro) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.idProducto, p.nombre, p.descripción, p.precioUnitario, p.stockDisponible, "
                + "c.idCategoria, c.descripcion "
                + "FROM Productos p "
                + "JOIN Categorias c ON p.idCategoria = c.idCategoria "
                + "WHERE p.nombre LIKE ? OR p.descripción LIKE ? "
                + "ORDER BY p.idProducto ASC";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            String likeTerm = "%" + filtro + "%";
            ps.setString(1, likeTerm);
            ps.setString(2, likeTerm);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setIdProducto(rs.getInt("idProducto"));
                    producto.setIdCategoria(rs.getInt("idCategoria"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setDescripcion(rs.getString("descripción"));
                    producto.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                    producto.setStock(rs.getInt("stockDisponible"));
                    producto.setCategoriaNombre(rs.getString("descripcion"));
                    lista.add(producto);
                }
            }
            LOGGER.log(Level.INFO, "Se cargaron {0} productos filtrados por '{1}'.", new Object[]{lista.size(), filtro});

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar productos por nombre: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Agrega un nuevo producto a la base de datos.
     *
     * @param producto El objeto Producto a agregar. El idProducto será
     * autogenerado por la DB.
     * @return true si el producto fue agregado exitosamente, false en caso
     * contrario.
     */
    public boolean agregarProducto(Producto producto) {
        String sql = "INSERT INTO Productos (idCategoria, nombre, descripción, precioUnitario, stockDisponible) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, producto.getIdCategoria());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getDescripcion());
            ps.setBigDecimal(4, producto.getPrecioUnitario());
            ps.setInt(5, producto.getStock());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                LOGGER.log(Level.INFO, "Producto agregado exitosamente: {0}", producto.getNombre());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al agregar producto: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Elimina un producto de la base de datos por su ID.
     *
     * @param idProducto El ID del producto a eliminar.
     * @return true si el producto fue eliminado exitosamente, false en caso
     * contrario.
     */
    public boolean eliminarProducto(int idProducto) {
        String sql = "DELETE FROM Productos WHERE idProducto = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                LOGGER.log(Level.INFO, "Producto con ID {0} eliminado exitosamente.", idProducto);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar producto con ID {0}: {1}", new Object[]{idProducto, e.getMessage()});
        }
        return false;
    }

    /**
     * Modifica la información de un producto existente en la base de datos.
     *
     * @param producto El objeto Producto con la información actualizada (el
     * idProducto debe existir).
     * @return true si el producto fue actualizado exitosamente, false en caso
     * contrario.
     */
    public boolean modificarProducto(Producto producto) {
        String sql = "UPDATE Productos SET idCategoria = ?, nombre = ?, descripción = ?, precioUnitario = ?, stockDisponible = ? WHERE idProducto = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, producto.getIdCategoria());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getDescripcion());
            ps.setBigDecimal(4, producto.getPrecioUnitario());
            ps.setInt(5, producto.getStock());
            ps.setInt(6, producto.getIdProducto());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                LOGGER.log(Level.INFO, "Producto con ID {0} modificado exitosamente.", producto.getIdProducto());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al modificar producto con ID {0}: {1}", new Object[]{producto.getIdProducto(), e.getMessage()});
        }
        return false;
    }

    /**
     * Obtiene un producto de la base de datos por su ID. Incluye la descripción
     * de la categoría.
     *
     * @param idProducto El ID del producto a buscar.
     * @return El objeto Producto si se encuentra, o null.
     */
    public Producto obtenerProductoPorId(int idProducto) {
        Producto producto = null;
        String sql = "SELECT p.idProducto, p.nombre, p.descripción, p.precioUnitario, p.stockDisponible, "
                + "c.idCategoria, c.descripcion "
                + "FROM Productos p "
                + "JOIN Categorias c ON p.idCategoria = c.idCategoria "
                + "WHERE p.idProducto = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    producto = new Producto();
                    producto.setIdProducto(rs.getInt("idProducto"));
                    producto.setIdCategoria(rs.getInt("idCategoria"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setDescripcion(rs.getString("descripción"));
                    producto.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                    producto.setStock(rs.getInt("stockDisponible"));
                    producto.setCategoriaNombre(rs.getString("descripcion"));
                    LOGGER.log(Level.INFO, "Producto con ID {0} encontrado.", idProducto);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener producto por ID: " + e.getMessage(), e);
        }
        return producto;
    }
}
