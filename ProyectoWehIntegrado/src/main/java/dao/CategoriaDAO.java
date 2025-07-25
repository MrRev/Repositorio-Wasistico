package dao;

import conf.Conexion; // Asume que tienes una clase para manejar la conexión a la BD
import modelo.Categoria;
import jakarta.inject.Named; // Importar Named para hacer el DAO un Managed Bean

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase DAO para la entidad Categoria. Proporciona métodos para interactuar con
 * la tabla 'Categorias' en la base de datos.
 */
@Named // Hace que este DAO sea un Managed Bean de CDI, accesible en EL
public class CategoriaDAO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CategoriaDAO.class.getName());

    /**
     * Agrega una nueva categoría a la base de datos.
     *
     * @param categoria El objeto Categoria a agregar. El idCategoria será
     * autogenerado por la DB.
     * @return true si la categoría fue agregada exitosamente, false en caso
     * contrario.
     */
    public boolean agregarCategoria(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO Categorias (descripcion) VALUES (?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Permite obtener el ID generado
            ps.setString(1, categoria.getDescripcion());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        categoria.setIdCategoria(rs.getInt(1)); // Asigna el ID generado a la categoría
                    }
                }
                LOGGER.log(Level.INFO, "Categoría agregada exitosamente: {0} con ID: {1}", new Object[]{categoria.getDescripcion(), categoria.getIdCategoria()});
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al agregar categoría: " + e.getMessage(), e);
            throw e; // Relanza la excepción para que el bean pueda manejarla (ej. duplicados)
        }
        return false;
    }

    /**
     * Actualiza la información de una categoría existente en la base de datos.
     *
     * @param categoria El objeto Categoria con la información actualizada (el
     * idCategoria debe existir).
     * @return true si la categoría fue actualizada exitosamente, false en caso
     * contrario.
     */
    public boolean actualizarCategoria(Categoria categoria) throws SQLException {
        String sql = "UPDATE Categorias SET descripcion = ? WHERE idCategoria = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria.getDescripcion());
            ps.setInt(2, categoria.getIdCategoria());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                LOGGER.log(Level.INFO, "Categoría con ID {0} actualizada exitosamente.", categoria.getIdCategoria());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar categoría con ID {0}: {1}", new Object[]{categoria.getIdCategoria(), e.getMessage()});
            throw e; // Relanza la excepción para que el bean pueda manejarla (ej. duplicados)
        }
        return false;
    }

    /**
     * Elimina una categoría de la base de datos por su ID.
     *
     * @param idCategoria El ID de la categoría a eliminar.
     * @return true si la categoría fue eliminada exitosamente, false en caso
     * contrario.
     */
    public boolean eliminarCategoria(int idCategoria) throws SQLException {
        String sql = "DELETE FROM Categorias WHERE idCategoria = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCategoria);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                LOGGER.log(Level.INFO, "Categoría con ID {0} eliminada exitosamente.", idCategoria);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar categoría con ID {0}: {1}", new Object[]{idCategoria, e.getMessage()});
            throw e; // Relanza la excepción
        }
        return false;
    }

    /**
     * Obtiene una lista de todas las categorías registradas en la base de
     * datos.
     *
     * @return Una lista de objetos Categoria. Retorna una lista vacía si no hay
     * categorías o si ocurre un error.
     */
    public List<Categoria> listarCategorias() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT idCategoria, descripcion FROM Categorias ORDER BY idCategoria ASC";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("idCategoria"), rs.getString("descripcion")));
            }
            LOGGER.log(Level.INFO, "Se cargaron {0} categorías.", lista.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar categorías: " + e.getMessage(), e);
            throw e; // Relanza la excepción
        }
        return lista;
    }

    /**
     * Obtiene categorías de la base de datos que coincidan con el filtro dado,
     * buscando en la descripción.
     *
     * @param filtro El término de búsqueda.
     * @return Una lista de objetos Categoria que coinciden con el filtro.
     */
    public List<Categoria> obtenerCategoriasFiltradas(String filtro) throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        if (filtro == null || filtro.trim().isEmpty()) {
            return listarCategorias(); // Si no hay filtro, devuelve todas las categorías
        }

        String sql = "SELECT idCategoria, descripcion FROM Categorias WHERE descripcion LIKE ? ORDER BY idCategoria ASC";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + filtro.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Categoria(rs.getInt("idCategoria"), rs.getString("descripcion")));
                }
            }
            LOGGER.log(Level.INFO, "Se cargaron {0} categorías filtradas por '{1}'.", new Object[]{lista.size(), filtro});

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener categorías filtradas: " + e.getMessage(), e);
            throw e; // Relanza la excepción
        }
        return lista;
    }

    /**
     * Obtiene una categoría de la base de datos por su ID.
     *
     * @param idCategoria El ID de la categoría a buscar.
     * @return El objeto Categoria si se encuentra, o null.
     */
    public Categoria obtenerCategoriaPorId(int idCategoria) throws SQLException {
        Categoria categoria = null;
        String sql = "SELECT idCategoria, descripcion FROM Categorias WHERE idCategoria = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    categoria = new Categoria(rs.getInt("idCategoria"), rs.getString("descripcion"));
                    LOGGER.log(Level.INFO, "Categoría con ID {0} encontrada.", idCategoria);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener categoría por ID: " + e.getMessage(), e);
            throw e; // Relanza la excepción
        }
        return categoria;
    }
}
