package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import modelo.Empleado;
import java.sql.SQLException;
import conf.Conexion;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValidarUsuarioDao {

    private static final Logger LOGGER = Logger.getLogger(ValidarUsuarioDao.class.getName());

    public boolean validarUsuario(Empleado login) {

        // Se selecciona todos los campos necesarios para el objeto Empleado,
        // incluyendo la descripción del rol y del sexo mediante JOINs.
        // ¡IMPORTANTE: Se asume que la columna de contraseña en la DB es 'contrasena' sin acento!
        String sql = "SELECT u.idUsuario, u.dni, u.nombre, u.apellido, u.correo, u.telefono, u.direccion, u.contrasena, u.idSexo, s.descripcion AS sexoDescripcion, u.idRol, r.descripcion AS rolDescripcion "
                + "FROM Usuarios u "
                + "JOIN Roles r ON u.idRol = r.idRol "
                + "JOIN Sexos s ON u.idSexo = s.idSexo "
                + "WHERE u.dni = ? AND u.contrasena = ?";

        try (Connection conn = Conexion.getConexion(); PreparedStatement stmt = conn.prepareStatement(sql);) {

            stmt.setString(1, login.getDni());
            stmt.setString(2, login.getContrasena());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Actualizar el objeto login con TODOS los datos obtenidos de la base de datos
                    login.setIdUsuario(rs.getInt("idUsuario"));
                    login.setDni(rs.getString("dni"));
                    login.setContrasena(rs.getString("contrasena"));
                    login.setNombre(rs.getString("nombre"));
                    login.setApellido(rs.getString("apellido"));
                    login.setCorreo(rs.getString("correo"));
                    login.setTelefono(rs.getString("telefono"));
                    login.setDireccion(rs.getString("direccion"));
                    login.setIdSexo(rs.getInt("idSexo"));
                    login.setSexoDescripcion(rs.getString("sexoDescripcion")); // ¡Ahora se carga la descripción del sexo!
                    login.setIdRol(rs.getInt("idRol"));
                    login.setRolDescripcion(rs.getString("rolDescripcion"));

                    LOGGER.log(Level.INFO, "Usuario {0} validado exitosamente. Rol: {1}, Sexo: {2}", new Object[]{login.getDni(), login.getRolDescripcion(), login.getSexoDescripcion()});
                    return true; // Se encontró coincidencia
                }
                LOGGER.log(Level.WARNING, "Intento de login fallido para DNI: {0}", login.getDni());
                return false; // No se encontró coincidencia
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en validación de usuario: " + e.getMessage(), e);
            return false;
        }
    }
}
