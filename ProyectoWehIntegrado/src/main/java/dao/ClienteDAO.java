package dao;

import conf.Conexion;
import modelo.Cliente;
import modelo.Sexo; // Aunque no se usa directamente en este DAO, se mantiene por si es necesario para Cliente
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase DAO (Data Access Object) para la entidad Cliente. Proporciona métodos
 * para interactuar con la tabla 'Clientes' en la base de datos.
 */
public class ClienteDAO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ClienteDAO.class.getName());

    /**
     * Agrega un nuevo cliente a la base de datos.
     *
     * @param cliente El objeto Cliente a agregar. El idCliente será
     * autogenerado por la DB.
     * @return true si el cliente fue agregado exitosamente, false en caso
     * contrario.
     */
    public boolean agregarCliente(Cliente cliente) {
        // Se corrigió 'dirección' a 'direccion' para coincidir con el schema de DB actualizado
        String sql = "INSERT INTO Clientes (dni, nombre, apellido, correo, telefono, direccion, idSexo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getDni());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getApellido());
            ps.setString(4, cliente.getCorreo());
            ps.setString(5, cliente.getTelefono());
            ps.setString(6, cliente.getDireccion()); // Se corrigió el método getter si es necesario, asumiendo que Cliente.getDireccion() devuelve el valor sin acento
            ps.setInt(7, cliente.getIdSexo());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        cliente.setIdCliente(rs.getInt(1));
                    }
                }
                LOGGER.log(Level.INFO, "Cliente agregado exitosamente con ID: {0}", cliente.getIdCliente());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al agregar cliente: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Actualiza la información de un cliente existente en la base de datos.
     *
     * @param cliente El objeto Cliente con la información actualizada (el
     * idCliente debe existir).
     * @return true si el cliente fue actualizado exitosamente, false en caso
     * contrario.
     */
    public boolean actualizarCliente(Cliente cliente) {
        // Se corrigió 'dirección' a 'direccion' para coincidir con el schema de DB actualizado
        String sql = "UPDATE Clientes SET dni=?, nombre=?, apellido=?, correo=?, telefono=?, direccion=?, idSexo=? WHERE idCliente=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getDni());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getApellido());
            ps.setString(4, cliente.getCorreo());
            ps.setString(5, cliente.getTelefono());
            ps.setString(6, cliente.getDireccion()); // Se corrigió el método getter
            ps.setInt(7, cliente.getIdSexo());
            ps.setInt(8, cliente.getIdCliente());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                LOGGER.log(Level.INFO, "Cliente con ID {0} actualizado exitosamente.", cliente.getIdCliente());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar cliente: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Elimina un cliente de la base de datos por su ID.
     *
     * @param idCliente El ID del cliente a eliminar.
     * @return true si el cliente fue eliminado exitosamente, false en caso
     * contrario.
     */
    public boolean eliminarCliente(int idCliente) {
        String sql = "DELETE FROM Clientes WHERE idCliente=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                LOGGER.log(Level.INFO, "Cliente con ID {0} eliminado exitosamente.", idCliente);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar cliente: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Obtiene una lista de todos los clientes registrados en la base de datos.
     * Incluye la descripción del sexo si se realiza un JOIN con la tabla Sexos.
     * Los resultados se ordenan de forma ascendente por idCliente.
     *
     * @return Una lista de objetos Cliente. Retorna una lista vacía si no hay
     * clientes o si ocurre un error.
     */
    public List<Cliente> obtenerTodosClientes() {
        List<Cliente> clientes = new ArrayList<>();
        // Se corrigió 'dirección' a 'direccion' y se añadió ORDER BY c.idCliente ASC
        String sql = "SELECT c.idCliente, c.dni, c.nombre, c.apellido, c.correo, c.telefono, c.direccion, c.idSexo, s.descripcion AS sexoDescripcion FROM Clientes c JOIN Sexos s ON c.idSexo = s.idSexo ORDER BY c.idCliente ASC";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            LOGGER.log(Level.INFO, "Ejecutando consulta: {0}", sql); // Añadido para depuración
            while (rs.next()) {
                clientes.add(mapearResultSetACliente(rs));
            }
            LOGGER.log(Level.INFO, "Se cargaron {0} clientes.", clientes.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los clientes: " + e.getMessage(), e);
        }
        return clientes;
    }

    /**
     * Obtiene una lista de clientes filtrados por un término de búsqueda
     * global. El filtro se aplica a DNI, nombre, apellido, correo, teléfono,
     * dirección y descripción del sexo. Los resultados se ordenan por
     * idCliente.
     *
     * @param filtroGlobal El término de búsqueda global.
     * @return Una lista de objetos Cliente que coinciden con el filtro.
     */
    public List<Cliente> obtenerClientesFiltrados(String filtroGlobal) {
        List<Cliente> clientes = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder();
        // Se corrigió 'dirección' a 'direccion'
        sqlBuilder.append("SELECT c.idCliente, c.dni, c.nombre, c.apellido, c.correo, c.telefono, c.direccion, c.idSexo, s.descripcion AS sexoDescripcion ");
        sqlBuilder.append("FROM Clientes c JOIN Sexos s ON c.idSexo = s.idSexo ");

        if (filtroGlobal != null && !filtroGlobal.trim().isEmpty()) {
            // Se corrigió 'dirección' a 'direccion'
            sqlBuilder.append("WHERE c.dni LIKE ? OR c.nombre LIKE ? OR c.apellido LIKE ? OR c.correo LIKE ? OR c.telefono LIKE ? OR c.direccion LIKE ? OR s.descripcion LIKE ?");
        }
        sqlBuilder.append(" ORDER BY c.idCliente ASC"); // Se añadió ORDER BY ASC para consistencia

        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sqlBuilder.toString())) {
            LOGGER.log(Level.INFO, "Ejecutando consulta: {0}", sqlBuilder.toString()); // Añadido para depuración
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
                    clientes.add(mapearResultSetACliente(rs));
                }
            }
            LOGGER.log(Level.INFO, "Se cargaron {0} clientes filtrados por '{1}'.", new Object[]{clientes.size(), filtroGlobal});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener clientes filtrados: " + e.getMessage(), e);
        }
        return clientes;
    }

    /**
     * Obtiene un cliente de la base de datos por su ID.
     *
     * @param idCliente El ID del cliente a buscar.
     * @return El objeto Cliente si se encuentra, o null si no existe o si
     * ocurre un error.
     */
    public Cliente obtenerClientePorId(int idCliente) {
        // Se corrigió 'dirección' a 'direccion'
        String sql = "SELECT c.idCliente, c.dni, c.nombre, c.apellido, c.correo, c.telefono, c.direccion, c.idSexo, s.descripcion AS sexoDescripcion "
                + "FROM Clientes c JOIN Sexos s ON c.idSexo = s.idSexo WHERE c.idCliente=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            LOGGER.log(Level.INFO, "Ejecutando consulta: {0}", sql); // Añadido para depuración
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LOGGER.log(Level.INFO, "Cliente con ID {0} encontrado.", idCliente);
                    return mapearResultSetACliente(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener cliente por ID: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Obtiene un cliente de la base de datos por su DNI.
     *
     * @param dni El DNI del cliente a buscar.
     * @return El objeto Cliente si se encuentra, o null si no existe o si
     * ocurre un error.
     */
    public Cliente obtenerClientePorDni(String dni) {
        // Se corrigió 'dirección' a 'direccion'
        String sql = "SELECT c.idCliente, c.dni, c.nombre, c.apellido, c.correo, c.telefono, c.direccion, c.idSexo, s.descripcion AS sexoDescripcion "
                + "FROM Clientes c JOIN Sexos s ON c.idSexo = s.idSexo WHERE c.dni=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            LOGGER.log(Level.INFO, "Ejecutando consulta: {0}", sql); // Añadido para depuración
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LOGGER.log(Level.INFO, "Cliente con DNI {0} encontrado.", dni);
                    return mapearResultSetACliente(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener cliente por DNI: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Cliente. Asume que
     * el ResultSet contiene una columna 'sexoDescripcion' si se hizo JOIN con
     * Sexos.
     *
     * @param rs El ResultSet del cual leer los datos.
     * @return Un objeto Cliente con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al acceder a los datos del
     * ResultSet.
     */
    private Cliente mapearResultSetACliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(
                rs.getInt("idCliente"),
                rs.getString("dni"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getString("direccion"), // Se corrigió 'dirección' a 'direccion'
                rs.getInt("idSexo"),
                rs.getString("sexoDescripcion")
        );
        return cliente;
    }
}
