package dao;

import conf.Conexion;
import modelo.Categoria;
import java.io.Serializable; // Importar Serializable
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger; // Importar Logger

public class CategoriaDAO implements Serializable { // Implementar Serializable

    private static final long serialVersionUID = 1L; // Añadir serialVersionUID
    private static final Logger LOGGER = Logger.getLogger(CategoriaDAO.class.getName()); // Usar Logger

    public void agregarCategoria(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO Categorias (descripcion) VALUES (?)";
        try (Connection conn = Conexion.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            LOGGER.log(Level.INFO, "Ejecutando INSERT con descripción: {0}", categoria.getDescripcion());
            stmt.setString(1, categoria.getDescripcion());
            int rows = stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Filas afectadas: {0}", rows);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al agregar categoría: " + e.getMessage(), e);
            throw e; // Relanzar la excepción para que el bean pueda manejarla
        }
    }

    public List<Categoria> listarCategorias() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM Categorias ORDER BY idCategoria ASC";
        try (Connection conn = Conexion.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("idCategoria"));
                categoria.setDescripcion(rs.getString("descripcion"));
                categorias.add(categoria);
            }
            LOGGER.log(Level.INFO, "Categorías recuperadas: {0}", categorias.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar categorías: " + e.getMessage(), e);
            throw e; // Relanzar la excepción
        }
        return categorias;
    }

    public void actualizarCategoria(Categoria categoria) throws SQLException {
        String sql = "UPDATE Categorias SET descripcion = ? WHERE idCategoria = ?";
        try (Connection conn = Conexion.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria.getDescripcion());
            stmt.setInt(2, categoria.getIdCategoria());
            int rows = stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Filas actualizadas: {0}", rows);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar categoría: " + e.getMessage(), e);
            throw e; // Relanzar la excepción
        }
    }

    public void eliminarCategoria(int idCategoria) throws SQLException {
        String sql = "DELETE FROM Categorias WHERE idCategoria = ?";
        try (Connection conn = Conexion.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCategoria);
            int rows = stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Filas eliminadas: {0}", rows);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar categoría: " + e.getMessage(), e);
            throw e; // Relanzar la excepción
        }
    }
}
