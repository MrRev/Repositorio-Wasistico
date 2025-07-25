package beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import modelo.Empleado;
import dao.ValidarUsuarioDao;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(LoginBean.class.getName());

    private Empleado usuario = new Empleado();
    private ValidarUsuarioDao dao = new ValidarUsuarioDao();

    public Empleado getUsuario() {
        return usuario;
    }

    public void setUsuario(Empleado usuario) {
        this.usuario = usuario;
    }

    public String validarLogin() {
        if (dao.validarUsuario(usuario)) {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                    .getExternalContext().getSession(true);
            // Almacenamos el objeto Empleado completo en la sesión
            session.setAttribute("usuarioLogueado", this.usuario);
            LOGGER.log(Level.INFO, "Login exitoso para DNI: {0}", this.usuario.getDni());
            return "principal?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new jakarta.faces.application.FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Login", "DNI o contraseña incorrectos."));
            LOGGER.log(Level.WARNING, "Login fallido.");
            return null;
        }
    }

    public void verificarSesion() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);

        // Si no hay sesión o el atributo 'usuarioLogueado' no existe, redirigir a index.xhtml
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            try {
                LOGGER.log(Level.INFO, "Sesión no encontrada o usuario no logueado. Redirigiendo a index.xhtml");
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al redirigir a index.xhtml: " + e.getMessage(), e);
                // No es ideal imprimir stack trace en producción, usar logger
            }
        }
    }

    public void redirigirLogueado() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);

        // Si hay sesión y el atributo 'usuarioLogueado' existe, redirigir a principal.xhtml
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            try {
                LOGGER.log(Level.INFO, "Usuario ya logueado. Redirigiendo a principal.xhtml");
                FacesContext.getCurrentInstance().getExternalContext().redirect("principal.xhtml");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al redirigir a principal.xhtml: " + e.getMessage(), e);
                // No es ideal imprimir stack trace en producción, usar logger
            }
        }
    }

    public String cerrarSesion() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);

        if (session != null) {
            session.invalidate(); // Cierra la sesión
            LOGGER.log(Level.INFO, "Sesión cerrada.");
        }

        return "index?faces-redirect=true"; // Redirige al login
    }
}
