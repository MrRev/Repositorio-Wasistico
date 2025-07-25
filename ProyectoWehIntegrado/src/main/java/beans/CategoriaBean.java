package beans;

import dao.CategoriaDAO;
import modelo.Categoria;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.annotation.PostConstruct; // Importar PostConstruct

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList; // Importar ArrayList
import java.util.List;
import java.util.logging.Level; // Importar Level
import java.util.logging.Logger; // Importar Logger

@Named("categoriaBean")
@ViewScoped
public class CategoriaBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CategoriaBean.class.getName());

    private Categoria nuevaCategoria = new Categoria(); // Para el formulario de agregar
    private List<Categoria> listaCategorias; // Para la tabla de listado
    private String filtroGlobal; // Para el filtro global de la tabla

    private Categoria categoriaSeleccionada; // Para editar en el modal
    private Categoria categoriaAEliminar; // Para confirmar eliminación en el modal

    private final CategoriaDAO categoriaDao = new CategoriaDAO();

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Inicializando CategoriaBean...");
        cargarListaCategorias(); // Carga la lista inicial de categorías
        nuevaCategoria = new Categoria(); // Asegura que el formulario de agregar esté limpio
        categoriaSeleccionada = new Categoria(); // Inicializa para evitar NullPointerException
        categoriaAEliminar = new Categoria(); // Inicializa para evitar NullPointerException
    }

    /**
     * Carga la lista de categorías, aplicando el filtro global si existe. Este
     * método es invocado por init() y por limpiarFiltro().
     */
    public void cargarListaCategorias() {
        try {
            if (filtroGlobal != null && !filtroGlobal.trim().isEmpty()) {
                listaCategorias = categoriaDao.obtenerCategoriasFiltradas(filtroGlobal.trim());
                LOGGER.log(Level.INFO, "Categorías filtradas por '{0}' cargadas. Cantidad: {1}", new Object[]{filtroGlobal, listaCategorias.size()});
            } else {
                listaCategorias = categoriaDao.listarCategorias();
                LOGGER.log(Level.INFO, "Todas las categorías cargadas. Cantidad: {0}", listaCategorias.size());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar las categorías: " + e.getMessage(), e);
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "error_carga_categorias", "No se pudo cargar las categorías: " + e.getMessage());
            listaCategorias = new ArrayList<>(); // Asegura que la lista no sea null
        }
    }

    /**
     * Agrega una nueva categoría a la base de datos.
     */
    public void agregarCategoria() {
        try {
            LOGGER.log(Level.INFO, "Intentando agregar categoría: {0}", nuevaCategoria.getDescripcion());
            boolean exito = categoriaDao.agregarCategoria(nuevaCategoria);
            if (exito) {
                cargarListaCategorias(); // Recarga la lista para reflejar el cambio
                limpiarFormulario(); // Limpia el formulario de adición
                mostrarMensaje(FacesMessage.SEVERITY_INFO, "exito_categoria_agregada", "Categoría agregada correctamente.");
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_WARN, "error_categoria_existente", "La descripción de la categoría ya existe o hubo un problema.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al agregar categoría: " + e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                mostrarMensaje(FacesMessage.SEVERITY_WARN, "error_categoria_duplicada", "La descripción de la categoría ya existe.");
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "error_agregar_categoria", "No se pudo agregar la categoría: " + e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al agregar categoría: " + e.getMessage(), e);
            mostrarMensaje(FacesMessage.SEVERITY_FATAL, "error_inesperado", "Ocurrió un error inesperado al agregar la categoría.");
        }
    }

    /**
     * Prepara el modal de edición cargando la categoría seleccionada.
     *
     * @param categoria La categoría a editar.
     */
    public void prepararEditar(Categoria categoria) {
        if (categoria != null) {
            this.categoriaSeleccionada = new Categoria(categoria.getIdCategoria(), categoria.getDescripcion()); // Crea una copia para edición
            LOGGER.log(Level.INFO, "Preparando edición para categoría ID: {0}", categoria.getIdCategoria());
        } else {
            LOGGER.log(Level.WARNING, "Intento de preparar edición con categoría nula.");
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "advertencia", "No se seleccionó ninguna categoría para editar.");
        }
    }

    /**
     * Actualiza la información de una categoría existente.
     */
    public void modificarCategoria() {
        try {
            if (categoriaSeleccionada != null && categoriaSeleccionada.getIdCategoria() > 0) {
                LOGGER.log(Level.INFO, "Intentando modificar categoría ID: {0}", categoriaSeleccionada.getIdCategoria());
                boolean exito = categoriaDao.actualizarCategoria(categoriaSeleccionada);
                if (exito) {
                    cargarListaCategorias(); // Recarga la lista
                    mostrarMensaje(FacesMessage.SEVERITY_INFO, "exito_categoria_actualizada", "Categoría actualizada correctamente.");
                } else {
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "error_actualizar_categoria", "No se pudo actualizar la categoría.");
                }
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_WARN, "advertencia", "No se seleccionó una categoría válida para actualizar.");
                LOGGER.log(Level.WARNING, "Intento de modificar categoría nula o sin ID.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al actualizar categoría: " + e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                mostrarMensaje(FacesMessage.SEVERITY_WARN, "error_categoria_duplicada", "La descripción de la categoría ya existe.");
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "error_actualizar_categoria", "No se pudo actualizar la categoría: " + e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al actualizar categoría: " + e.getMessage(), e);
            mostrarMensaje(FacesMessage.SEVERITY_FATAL, "error_inesperado", "Ocurrió un error inesperado al actualizar la categoría.");
        }
    }

    /**
     * Prepara el modal de confirmación de eliminación.
     *
     * @param categoria La categoría a eliminar.
     */
    public void prepararEliminacion(Categoria categoria) {
        if (categoria != null) {
            this.categoriaAEliminar = categoria;
            LOGGER.log(Level.INFO, "Preparando eliminación para categoría ID: {0}", categoria.getIdCategoria());
        } else {
            LOGGER.log(Level.WARNING, "Intento de preparar eliminación con categoría nula.");
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "advertencia", "No se seleccionó ninguna categoría para eliminar.");
        }
    }

    /**
     * Elimina la categoría seleccionada de la base de datos.
     */
    public void eliminarCategoria() {
        try {
            if (categoriaAEliminar != null && categoriaAEliminar.getIdCategoria() > 0) {
                LOGGER.log(Level.INFO, "Intentando eliminar categoría ID: {0}", categoriaAEliminar.getIdCategoria());
                boolean exito = categoriaDao.eliminarCategoria(categoriaAEliminar.getIdCategoria());
                if (exito) {
                    cargarListaCategorias(); // Recarga la lista
                    mostrarMensaje(FacesMessage.SEVERITY_INFO, "exito_categoria_eliminada", "Categoría eliminada correctamente.");
                    categoriaAEliminar = new Categoria(); // Limpia el objeto después de eliminar
                } else {
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "error_eliminar_categoria", "No se pudo eliminar la categoría.");
                }
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_WARN, "advertencia", "No se seleccionó una categoría válida para eliminar.");
                LOGGER.log(Level.WARNING, "Intento de eliminar categoría nula o sin ID.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al eliminar categoría: " + e.getMessage(), e);
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "error_eliminar_categoria", "No se pudo eliminar la categoría: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al eliminar categoría: " + e.getMessage(), e);
            mostrarMensaje(FacesMessage.SEVERITY_FATAL, "error_inesperado", "Ocurrió un error inesperado al eliminar la categoría.");
        }
    }

    /**
     * Busca categorías en la base de datos utilizando el filtro global. Este
     * método se invoca desde la UI cuando se aplica el filtro.
     */
    public void buscarCategorias() {
        cargarListaCategorias(); // Reutiliza el método de carga que ya aplica el filtro
    }

    /**
     * Limpia el filtro de búsqueda y recarga la lista completa de categorías.
     */
    public void limpiarFiltro() {
        this.filtroGlobal = null; // O this.filtroGlobal = "";
        cargarListaCategorias(); // Recarga la lista completa sin filtro
        mostrarMensaje(FacesMessage.SEVERITY_INFO, "filtro_limpiado", "Filtro de categorías limpiado.");
    }

    /**
     * Reinicia el formulario de agregar categoría.
     */
    public void limpiarFormulario() {
        this.nuevaCategoria = new Categoria();
        mostrarMensaje(FacesMessage.SEVERITY_INFO, "formulario_limpiado", "Formulario de categoría limpiado.");
    }

    /**
     * Método auxiliar para mostrar mensajes en la UI.
     *
     * @param severity La severidad del mensaje (INFO, WARN, ERROR, FATAL).
     * @param summaryKey La clave del mensaje para el archivo de recursos (si
     * aplica).
     * @param detail La descripción detallada del mensaje.
     */
    private void mostrarMensaje(FacesMessage.Severity severity, String summaryKey, String detail) {
        // En una aplicación real, 'summaryKey' podría usarse para buscar mensajes en un archivo .properties (Resource Bundle)
        // Por simplicidad, aquí usamos el 'detail' como el mensaje principal.
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summaryKey, detail));
    }

    // --- Getters y Setters ---
    public Categoria getNuevaCategoria() {
        return nuevaCategoria;
    }

    public void setNuevaCategoria(Categoria nuevaCategoria) {
        this.nuevaCategoria = nuevaCategoria;
    }

    public List<Categoria> getListaCategorias() {
        return listaCategorias;
    }

    public void setListaCategorias(List<Categoria> listaCategorias) {
        this.listaCategorias = listaCategorias;
    }

    public String getFiltroGlobal() {
        return filtroGlobal;
    }

    public void setFiltroGlobal(String filtroGlobal) {
        this.filtroGlobal = filtroGlobal;
    }

    public Categoria getCategoriaSeleccionada() {
        return categoriaSeleccionada;
    }

    public void setCategoriaSeleccionada(Categoria categoriaSeleccionada) {
        this.categoriaSeleccionada = categoriaSeleccionada;
    }

    public Categoria getCategoriaAEliminar() {
        return categoriaAEliminar;
    }

    public void setCategoriaAEliminar(Categoria categoriaAEliminar) {
        this.categoriaAEliminar = categoriaAEliminar;
    }
}
