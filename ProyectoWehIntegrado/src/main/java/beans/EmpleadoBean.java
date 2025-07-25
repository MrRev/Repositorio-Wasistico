package beans;

import dao.EmpleadoDAO;
import dao.CatalogoDAO; // Nuevo import para el DAO de catálogos
import modelo.Empleado;
import modelo.Rol;     // Nuevo import para la clase Rol
import modelo.Sexo;    // Nuevo import para la clase Sexo
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage; // Import para mensajes de JSF
import jakarta.faces.context.FacesContext;     // Import para el contexto de JSF
import jakarta.faces.model.SelectItem;         // Import para SelectItem, usado en selectOneMenu
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("empleadoBean")
@SessionScoped // Cambiado a SessionScoped para mantener el filtro y las listas de catálogos
public class EmpleadoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EmpleadoBean.class.getName());

    @Inject
    private EmpleadoDAO empleadoDAO;
    @Inject
    private CatalogoDAO catalogoDAO; // Inyección del DAO para catálogos

    private Empleado nuevoEmpleado;
    private List<Empleado> listaEmpleados;
    private Empleado empleadoAEliminar; // Para el modal de confirmación de eliminación

    private String filtroGlobal; // Atributo para el término de búsqueda global

    private List<SelectItem> sexosItems; // Lista de opciones para el selectOneMenu de sexo
    private List<SelectItem> rolesItems; // Lista de opciones para el selectOneMenu de rol

    @PostConstruct
    public void init() {
        nuevoEmpleado = new Empleado();
        cargarEmpleados();    // Carga inicial de la tabla de empleados
        cargarCatalogos();    // Carga inicial de las listas de sexos y roles
    }

    /**
     * Carga las listas de sexos y roles desde el CatalogoDAO y las prepara como
     * SelectItem. Se añaden mensajes de error si la carga falla.
     */
    public void cargarCatalogos() {
        sexosItems = new ArrayList<>();
        sexosItems.add(new SelectItem("", "Seleccione un sexo", "Seleccione un sexo", false, false, true));
        try {
            List<Sexo> sexos = catalogoDAO.obtenerSexos();
            if (sexos == null || sexos.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("formEmpleado:sexo", new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay sexos disponibles en el sistema."));
                FacesContext.getCurrentInstance().addMessage("formEditarEmpleado:editSexo", new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay sexos disponibles en el sistema."));
                LOGGER.log(Level.WARNING, "No se encontraron sexos en la base de datos.");
            } else {
                for (Sexo sexo : sexos) {
                    sexosItems.add(new SelectItem(sexo.getIdSexo(), sexo.getDescripcion()));
                }
                LOGGER.log(Level.INFO, "Se cargaron {0} sexos.", sexos.size());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar sexos: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage("formEmpleado:sexo", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los sexos: " + e.getMessage()));
            FacesContext.getCurrentInstance().addMessage("formEditarEmpleado:editSexo", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los sexos: " + e.getMessage()));
        }

        rolesItems = new ArrayList<>();
        rolesItems.add(new SelectItem("", "Seleccione un rol", "Seleccione un rol", false, false, true));
        try {
            List<Rol> roles = catalogoDAO.obtenerRoles();
            if (roles == null || roles.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("formEmpleado:rol", new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay roles disponibles en el sistema."));
                FacesContext.getCurrentInstance().addMessage("formEditarEmpleado:editRol", new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay roles disponibles en el sistema."));
                LOGGER.log(Level.WARNING, "No se encontraron roles en la base de datos.");
            } else {
                for (Rol rol : roles) {
                    rolesItems.add(new SelectItem(rol.getIdRol(), rol.getDescripcion()));
                }
                LOGGER.log(Level.INFO, "Se cargaron {0} roles.", roles.size());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar roles: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage("formEmpleado:rol", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los roles: " + e.getMessage()));
            FacesContext.getCurrentInstance().addMessage("formEditarEmpleado:editRol", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los roles: " + e.getMessage()));
        }
    }

    /**
     * Carga la lista de empleados, aplicando el filtro global si existe. Se
     * añaden mensajes de error si la carga falla.
     */
    public void cargarEmpleados() {
        try {
            if (filtroGlobal != null && !filtroGlobal.trim().isEmpty()) {
                listaEmpleados = empleadoDAO.obtenerEmpleadosFiltrados(filtroGlobal);
            } else {
                listaEmpleados = empleadoDAO.obtenerTodosEmpleados();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar empleados: " + e.getMessage(), e);
            listaEmpleados = new ArrayList<>(); // Asegura que la lista no sea null
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los empleados."));
        }
    }

    /**
     * Guarda un nuevo empleado o actualiza uno existente. Muestra mensajes de
     * éxito o error al usuario.
     */
    public void guardarEmpleado() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            boolean exito;
            String mensajeExito;
            String mensajeError;

            if (nuevoEmpleado.getIdUsuario() == 0) { // Es un nuevo empleado (ID 0 indica nuevo)
                exito = empleadoDAO.agregarEmpleado(nuevoEmpleado);
                mensajeExito = "Empleado agregado exitosamente.";
                mensajeError = "No se pudo agregar el empleado.";
            } else { // Es una actualización de un empleado existente
                exito = empleadoDAO.actualizarEmpleado(nuevoEmpleado);
                mensajeExito = "Empleado actualizado exitosamente.";
                mensajeError = "No se pudo actualizar el empleado.";
            }

            if (exito) {
                LOGGER.log(Level.INFO, mensajeExito);
                nuevoEmpleado = new Empleado(); // Limpiar el formulario para un nuevo ingreso
                cargarEmpleados(); // Recargar la lista de empleados en la tabla
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensajeExito));
            } else {
                LOGGER.log(Level.WARNING, mensajeError);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", mensajeError));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar/actualizar empleado: " + e.getMessage(), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error Grave", "Ocurrió un error inesperado al guardar el empleado."));
        }
    }

    /**
     * Prepara el empleado para ser eliminado, almacenándolo temporalmente.
     *
     * @param empleado El empleado a ser eliminado.
     */
    public void prepararEliminacion(Empleado empleado) {
        this.empleadoAEliminar = empleado;
    }

    /**
     * Elimina el empleado seleccionado de la base de datos. Muestra mensajes de
     * éxito o error al usuario.
     */
    public void eliminarEmpleado() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            if (empleadoAEliminar != null) {
                boolean exito = empleadoDAO.eliminarEmpleado(empleadoAEliminar.getIdUsuario());
                if (exito) {
                    LOGGER.log(Level.INFO, "Empleado eliminado exitosamente.");
                    cargarEmpleados(); // Recargar la lista de empleados en la tabla
                    empleadoAEliminar = null; // Limpiar el empleado a eliminar
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Empleado eliminado exitosamente."));
                } else {
                    LOGGER.log(Level.WARNING, "No se pudo eliminar el empleado.");
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el empleado."));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar empleado: " + e.getMessage(), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error Grave", "Ocurrió un error inesperado al eliminar el empleado."));
        }
    }

    /**
     * Prepara el formulario para la edición de un empleado existente. Clona el
     * objeto para evitar modificaciones directas en la lista hasta que se
     * guarde.
     *
     * @param empleado El empleado a editar.
     */
    public void editarEmpleado(Empleado empleado) {
        // Clonar el empleado para evitar modificar directamente la lista hasta que se guarde
        // Asegúrate de que el constructor o setter maneje rolDescripcion si es necesario
        this.nuevoEmpleado = new Empleado(empleado.getIdUsuario(), empleado.getDni(), empleado.getNombre(),
                empleado.getApellido(), empleado.getCorreo(), empleado.getTelefono(),
                empleado.getDireccion(), empleado.getContrasena(), empleado.getIdSexo(),
                empleado.getIdRol(), empleado.getRolDescripcion());
    }

    /**
     * Limpia el formulario de nuevo empleado.
     */
    public void limpiarFormulario() {
        nuevoEmpleado = new Empleado();
    }

    // --- Getters y Setters ---
    public Empleado getNuevoEmpleado() {
        return nuevoEmpleado;
    }

    public void setNuevoEmpleado(Empleado nuevoEmpleado) {
        this.nuevoEmpleado = nuevoEmpleado;
    }

    public List<Empleado> getListaEmpleados() {
        // Asegúrate de que la lista se cargue si aún no lo ha hecho (aunque PostConstruct ya lo hace)
        if (listaEmpleados == null) {
            cargarEmpleados();
        }
        return listaEmpleados;
    }

    public Empleado getEmpleadoAEliminar() {
        return empleadoAEliminar;
    }

    public void setEmpleadoAEliminar(Empleado empleadoAEliminar) {
        this.empleadoAEliminar = empleadoAEliminar;
    }

    public String getFiltroGlobal() {
        return filtroGlobal;
    }

    public void setFiltroGlobal(String filtroGlobal) {
        this.filtroGlobal = filtroGlobal;
    }

    public List<SelectItem> getSexosItems() {
        return sexosItems;
    }

    public List<SelectItem> getRolesItems() {
        return rolesItems;
    }
}
