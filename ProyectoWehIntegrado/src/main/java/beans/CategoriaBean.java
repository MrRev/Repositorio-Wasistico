package beans;

import dao.CategoriaDAO;
import modelo.Categoria;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

@Named("categoriaBean")
@ViewScoped
public class CategoriaBean implements Serializable {

    private Categoria categoria = new Categoria();
    private List<Categoria> categorias;
    private final CategoriaDAO categoriaDao = new CategoriaDAO();

    private Categoria categoriaSeleccionada; // Para editar o eliminar

    public CategoriaBean() {
        cargarCategorias();
    }

    private void cargarCategorias() {
        try {
            categorias = categoriaDao.listarCategorias();
            System.out.println("Categorías cargadas: " + categorias.size());
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "mensaje_error", "No se pudo cargar las categorías: " + e.getMessage());
        }
    }

    public void agregarCategoria() {
        try {
            System.out.println("Agregando categoría: " + categoria.getDescripcion());
            categoriaDao.agregarCategoria(categoria);
            cargarCategorias(); // recarga la lista
            categoria = new Categoria(); // limpia el formulario
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "mensaje_exito", "Categoría agregada correctamente");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                mostrarMensaje(FacesMessage.SEVERITY_WARN, "mensaje_duplicado", "La descripción ya existe.");
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "mensaje_error", "No se pudo agregar la categoría: " + e.getMessage());
            }
            System.err.println("Error al agregar categoría: " + e.getMessage());
        }
    }

    public void prepararEdicion(Categoria categoria) {
        this.categoriaSeleccionada = categoria;
    }

    public void actualizarCategoria() {
        try {
            categoriaDao.actualizarCategoria(categoriaSeleccionada);
            cargarCategorias();
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "mensaje_exito", "Categoría actualizada correctamente");
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "mensaje_error", "No se pudo actualizar la categoría: " + e.getMessage());
            System.err.println("Error al actualizar categoría: " + e.getMessage());
        }
    }

    public void prepararEliminacion(Categoria categoria) {
        this.categoriaSeleccionada = categoria;
    }

    public void eliminarCategoria() {
        try {
            categoriaDao.eliminarCategoria(categoriaSeleccionada.getIdCategoria());
            cargarCategorias();
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "mensaje_exito", "Categoría eliminada correctamente");
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "mensaje_error", "No se pudo eliminar la categoría: " + e.getMessage());
            System.err.println("Error al eliminar categoría: " + e.getMessage());
        }
    }

    private void mostrarMensaje(FacesMessage.Severity severidad, String key, String defaultMsg) {
        String mensaje = obtenerMensaje(key, defaultMsg);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, "", mensaje));
    }

    private String obtenerMensaje(String key, String defaultMsg) {
        try {
            return FacesContext.getCurrentInstance().getApplication()
                    .evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{msg['" + key + "']}", String.class);
        } catch (Exception e) {
            System.err.println("No se pudo resolver el mensaje para la clave: " + key);
            return defaultMsg;
        }
    }

    // Getters y Setters
    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public Categoria getCategoriaSeleccionada() {
        return categoriaSeleccionada;
    }

    public void setCategoriaSeleccionada(Categoria categoriaSeleccionada) {
        this.categoriaSeleccionada = categoriaSeleccionada;
    }
}