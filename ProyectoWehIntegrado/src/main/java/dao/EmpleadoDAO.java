package dao;

import conf.Conexion;
import modelo.Empleado;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase DAO (Data Access Object) para la entidad Empleado.
 * Proporciona métodos para interactuar con la tabla 'Usuarios' en la base de datos.
 */
public class EmpleadoDAO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EmpleadoDAO.class.getName());

    /**
     * Agrega un nuevo empleado a la base de datos.
     *
     * @param empleado El objeto Empleado a agregar. El idUsuario será autogenerado por la DB.
     * @return true si el empleado fue agregado exitosamente, false en caso contrario.
     */
    public boolean agregarEmpleado(Empleado empleado) {
        String sql = "INSERT INTO Usuarios (dni, nombre, apellido, correo, telefono, dirección, contraseña, idSexo, idRol) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empleado.getDni());
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getApellido());
            ps.setString(4, empleado.getCorreo());
            ps.setString(5, empleado.getTelefono());
            ps.setString(6, empleado.getDireccion());
            ps.setString(7, empleado.getContrasena());
            ps.setInt(8, empleado.getIdSexo());
            ps.setInt(9, empleado.getIdRol());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        empleado.setIdUsuario(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al agregar empleado: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Actualiza la información de un empleado existente en la base de datos.
     *
     * @param empleado El objeto Empleado con la información actualizada (el idUsuario debe existir).
     * @return true si el empleado fue actualizado exitosamente, false en caso contrario.
     */
    public boolean actualizarEmpleado(Empleado empleado) {
        String sql = "UPDATE Usuarios SET dni=?, nombre=?, apellido=?, correo=?, telefono=?, dirección=?, contraseña=?, idSexo=?, idRol=? WHERE idUsuario=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, empleado.getDni());
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getApellido());
            ps.setString(4, empleado.getCorreo());
            ps.setString(5, empleado.getTelefono());
            ps.setString(6, empleado.getDireccion());
            ps.setString(7, empleado.getContrasena());
            ps.setInt(8, empleado.getIdSexo());
            ps.setInt(9, empleado.getIdRol());
            ps.setInt(10, empleado.getIdUsuario());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar empleado: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Elimina un empleado de la base de datos por su ID.
     *
     * @param idUsuario El ID del empleado a eliminar.
     * @return true si el empleado fue eliminado exitosamente, false en caso contrario.
     */
    public boolean eliminarEmpleado(int idUsuario) {
        String sql = "DELETE FROM Usuarios WHERE idUsuario=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar empleado: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Obtiene una lista de todos los empleados registrados en la base de datos.
     *
     * @return Una lista de objetos Empleado. Retorna una lista vacía si no hay empleados o si ocurre un error.
     */
    public List<Empleado> obtenerTodosEmpleados() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT u.idUsuario, u.dni, u.nombre, u.apellido, u.correo, u.telefono, u.dirección, u.contraseña, u.idSexo, u.idRol, r.descripcion AS rolDescripcion " +
                     "FROM Usuarios u JOIN Roles r ON u.idRol = r.idRol";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empleados.add(mapearResultSetAEmpleado(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los empleados: " + e.getMessage(), e);
        }
        return empleados;
    }

    /**
     * Obtiene una lista de empleados filtrados por un término de búsqueda global.
     * El filtro se aplica a DNI, nombre, apellido, correo, teléfono, dirección y descripción del rol.
     *
     * @param filtroGlobal El término de búsqueda global.
     * @return Una lista de objetos Empleado que coinciden con el filtro.
     */
    public List<Empleado> obtenerEmpleadosFiltrados(String filtroGlobal) {
        List<Empleado> empleados = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT u.idUsuario, u.dni, u.nombre, u.apellido, u.correo, u.telefono, u.dirección, u.contraseña, u.idSexo, u.idRol, r.descripcion AS rolDescripcion ");
        sqlBuilder.append("FROM Usuarios u JOIN Roles r ON u.idRol = r.idRol ");

        if (filtroGlobal != null && !filtroGlobal.trim().isEmpty()) {
            sqlBuilder.append("WHERE u.dni LIKE ? OR u.nombre LIKE ? OR u.apellido LIKE ? OR u.correo LIKE ? OR u.telefono LIKE ? OR u.dirección LIKE ? OR r.descripcion LIKE ?");
        }
        sqlBuilder.append(" ORDER BY u.idUsuario"); // Opcional: ordenar resultados

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlBuilder.toString())) {

            if (filtroGlobal != null && !filtroGlobal.trim().isEmpty()) {
                String likeTerm = "%" + filtroGlobal.trim() + "%";
                ps.setString(1, likeTerm);
                ps.setString(2, likeTerm);
                ps.setString(3, likeTerm);
                ps.setString(4, likeTerm);
                ps.setString(5, likeTerm);
                ps.setString(6, likeTerm);
                ps.setString(7, likeTerm);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    empleados.add(mapearResultSetAEmpleado(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener empleados filtrados: " + e.getMessage(), e);
        }
        return empleados;
    }

    /**
     * Obtiene un empleado de la base de datos por su ID.
     *
     * @param idUsuario El ID del empleado a buscar.
     * @return El objeto Empleado si se encuentra, o null si no existe o si ocurre un error.
     */
    public Empleado obtenerEmpleadoPorId(int idUsuario) {
        String sql = "SELECT u.idUsuario, u.dni, u.nombre, u.apellido, u.correo, u.telefono, u.dirección, u.contraseña, u.idSexo, u.idRol, r.descripcion AS rolDescripcion " +
                     "FROM Usuarios u JOIN Roles r ON u.idRol = r.idRol WHERE u.idUsuario=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetAEmpleado(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener empleado por ID: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Obtiene un empleado de la base de datos por su DNI.
     * Útil para la autenticación.
     *
     * @param dni El DNI del empleado a buscar.
     * @return El objeto Empleado si se encuentra, o null si no existe o si ocurre un error.
     */
    public Empleado obtenerEmpleadoPorDni(String dni) {
        String sql = "SELECT u.idUsuario, u.dni, u.nombre, u.apellido, u.correo, u.telefono, u.dirección, u.contraseña, u.idSexo, u.idRol, r.descripcion AS rolDescripcion " +
                     "FROM Usuarios u JOIN Roles r ON u.idRol = r.idRol WHERE u.dni=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetAEmpleado(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener empleado por DNI: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Empleado.
     *
     * @param rs El ResultSet del cual leer los datos.
     * @return Un objeto Empleado con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private Empleado mapearResultSetAEmpleado(ResultSet rs) throws SQLException {
        Empleado empleado = new Empleado(
                rs.getInt("idUsuario"),
                rs.getString("dni"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getString("dirección"),
                rs.getString("contraseña"),
                rs.getInt("idSexo"),
                rs.getInt("idRol")
        );
        // Asegúrate de que tu clase Empleado tenga un setter para rolDescripcion o un constructor que lo reciba
        empleado.setRolDescripcion(rs.getString("rolDescripcion"));
        return empleado;
    }
}
