package dao;

import conf.Conexion;
import modelo.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO{
    
    public void agregarCategoria(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO Categorias (descripcion) VALUES (?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            System.out.println("Ejecutando INSERT con descripción: " + categoria.getDescripcion());
            stmt.setString(1, categoria.getDescripcion());
            int rows = stmt.executeUpdate();
            System.out.println("Filas afectadas: " + rows);
        }
    }
    
    public List<Categoria> listarCategorias() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM Categorias ORDER BY idCategoria ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("idCategoria"));
                categoria.setDescripcion(rs.getString("descripcion"));
                categorias.add(categoria);
            }
            System.out.println("Categorías recuperadas: " + categorias.size());
        }
        return categorias;
    }
    
    public void actualizarCategoria(Categoria categoria) throws SQLException {
        String sql = "UPDATE Categorias SET descripcion = ? WHERE idCategoria = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria.getDescripcion());
            stmt.setInt(2, categoria.getIdCategoria());
            int rows = stmt.executeUpdate();
            System.out.println("Filas actualizadas: " + rows);
        }
    }
    
    public void eliminarCategoria(int idCategoria) throws SQLException {
        String sql = "DELETE FROM Categorias WHERE idCategoria = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCategoria);
            int rows = stmt.executeUpdate();
            System.out.println("Filas eliminadas: " + rows);
        }
    }
}
