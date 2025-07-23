package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import modelo.Usuario;
import conf.Conexion;

public class ValidarUsuarioDao {

    public boolean validarUsuario(Usuario login) {
        
        String sql = "SELECT dni, contraseña, nombre, apellido FROM Usuarios WHERE dni = ? AND contraseña = ?";
        
        try ( Connection conn = Conexion.getConexion(); 
            PreparedStatement stmt = conn.prepareStatement(sql);) {
            //comparamos con los datos obtenidos del constructor
            stmt.setString(1, login.getDni());
            stmt.setString(2, login.getContraseña());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Actualizar el objeto login con los datos obtenidos en el modelo
                    login.setDni(rs.getString("dni"));
                    login.setContraseña(rs.getString("contraseña"));
                    login.setNombre(rs.getString("nombre"));
                    login.setApellido(rs.getString("apellido"));
                    return true; // Se encontró coincidencia
                }
                return false; // No se encontró coincidencia
            }
        } catch (Exception e) {
            System.err.println("Error en validación de usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}