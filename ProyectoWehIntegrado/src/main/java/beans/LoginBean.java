package beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import modelo.Empleado;
import dao.ValidarUsuarioDao;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

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
            session.setAttribute("dni", this.usuario.getDni());
            return "principal?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new jakarta.faces.application.FacesMessage("DNI o contraseña incorrectos."));
            return null;
        }
    }

    public void verificarSesion() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
            .getExternalContext().getSession(false);

        if (session == null || session.getAttribute("dni") == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void redirigirLogueado() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
            .getExternalContext().getSession(false);

        if (session != null && session.getAttribute("dni") != null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("principal.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String cerrarSesion() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
            .getExternalContext().getSession(false);

        if (session != null) {
            session.invalidate(); // Cierra la sesión
        }

        return "index?faces-redirect=true"; // Redirige al login
    }
}