package beans;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import modelo.Empleado;
import dao.EmpleadoDAO; // Se necesita EmpleadoDAO para actualizar en la base de datos
import jakarta.faces.application.FacesMessage;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("perfilEmpleadoBean")
@ViewScoped
public class PerfilEmpleadoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PerfilEmpleadoBean.class.getName());

    private Empleado empleadoActual;

    @Inject
    private EmpleadoDAO empleadoDAO; // Se inyecta EmpleadoDAO para la operación de actualización

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session != null) {
            // Intentamos obtener el objeto Empleado directamente de la sesión
            Empleado usuarioLogueado = (Empleado) session.getAttribute("usuarioLogueado");

            if (usuarioLogueado != null) {
                // Usamos el objeto Empleado directamente de la sesión
                this.empleadoActual = usuarioLogueado;
                LOGGER.log(Level.INFO, "Datos de perfil cargados desde la sesión para el empleado: {0}", empleadoActual.getDni());
            } else {
                LOGGER.log(Level.WARNING, "No se encontró el objeto 'usuarioLogueado' en la sesión. Redirigiendo a index.xhtml");
                // Si no hay objeto Empleado en sesión, redirigir al login
                try {
                    context.getExternalContext().redirect("index.xhtml");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al redirigir a index.xhtml: " + e.getMessage(), e);
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "No se encontró sesión. Redirigiendo a index.xhtml");
            // Si no hay sesión, redirigir al login
            try {
                context.getExternalContext().redirect("index.xhtml");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al redirigir a index.xhtml: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Guarda los cambios realizados en el perfil del empleado en la base de
     * datos. Muestra mensajes de éxito o error al usuario.
     */
    public void actualizarPerfil() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            // Llama al DAO para actualizar el empleado en la base de datos
            boolean exito = empleadoDAO.actualizarEmpleado(empleadoActual);

            if (exito) {
                // Si la actualización fue exitosa, también actualiza el objeto en la sesión
                HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
                if (session != null) {
                    session.setAttribute("usuarioLogueado", empleadoActual);
                }
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Perfil actualizado exitosamente."));
                LOGGER.log(Level.INFO, "Perfil del empleado {0} actualizado exitosamente.", empleadoActual.getDni());
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el perfil."));
                LOGGER.log(Level.WARNING, "Fallo al actualizar perfil para el empleado {0}.", empleadoActual.getDni());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar perfil: " + e.getMessage(), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error Grave", "Ocurrió un error inesperado al actualizar el perfil."));
        }
    }

    // Getter para acceder al empleado actual desde la vista
    public Empleado getEmpleadoActual() {
        return empleadoActual;
    }

    // Setter (necesario para que los inputText actualicen el modelo)
    public void setEmpleadoActual(Empleado empleadoActual) {
        this.empleadoActual = empleadoActual;
    }

    // Método para "refrescar" el perfil si se edita en otra parte
    // Si los datos se editan y la sesión se actualiza, este método puede ser útil.
    // De lo contrario, si solo se usa el objeto de la sesión, no es estrictamente necesario.
    public void recargarPerfil() {
        init(); // Simplemente llama a init para recargar los datos de la sesión
        LOGGER.log(Level.INFO, "Perfil de empleado recargado desde la sesión.");
    }
}
