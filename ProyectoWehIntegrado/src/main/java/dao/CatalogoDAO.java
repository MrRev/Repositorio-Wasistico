package dao;

import conf.Conexion;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import modelo.Rol;
import modelo.Sexo;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase DAO para obtener datos de catálogos como Roles y Sexos.
 */
public class CatalogoDAO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CatalogoDAO.class.getName());

    /**
     * Obtiene una lista de todos los roles disponibles en la base de datos.
     *
     * @return Una lista de objetos Rol. Retorna una lista vacía si no hay roles
     * o si ocurre un error.
     */
    public List<Sexo> obtenerSexos() {
        List<Sexo> sexos = new ArrayList<>();
        String sql = "SELECT idSexo, descripcion FROM Sexos ORDER BY descripcion";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int count = 0;
            while (rs.next()) {
                sexos.add(new Sexo(rs.getInt("idSexo"), rs.getString("descripcion")));
                count++;
            }
            LOGGER.log(Level.INFO, "Se encontraron {0} sexos en la base de datos.", count);
            if (count == 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay sexos disponibles en el sistema."));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener sexos: {0} [SQL: {1}]", new Object[]{e.getMessage(), sql});
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los sexos: " + e.getMessage()));
        }
        return sexos;
    }

    public List<Rol> obtenerRoles() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT idRol, descripcion FROM Roles ORDER BY descripcion";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int count = 0;
            while (rs.next()) {
                roles.add(new Rol(rs.getInt("idRol"), rs.getString("descripcion")));
                count++;
            }
            LOGGER.log(Level.INFO, "Se encontraron {0} roles en la base de datos.", count);
            if (count == 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay roles disponibles en el sistema."));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener roles: {0} [SQL: {1}]", new Object[]{e.getMessage(), sql});
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los roles: " + e.getMessage()));
        }
        return roles;
    }
}
