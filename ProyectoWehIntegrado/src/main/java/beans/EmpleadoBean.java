package beans;

import dao.EmpleadoDAO;
import dao.CatalogoDAO;
import modelo.Empleado;
import modelo.Rol;
import modelo.Sexo;
import util.DniApiService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped; // Manteniendo ViewScoped
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("empleadoBean")
@ViewScoped // Se mantiene ViewScoped
public class EmpleadoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EmpleadoBean.class.getName());

    @Inject
    private EmpleadoDAO empleadoDAO;
    @Inject
    private CatalogoDAO catalogoDAO;
    @Inject
    private DniApiService dniApiService; // Servicio para consultar la API de DNI

    private Empleado nuevoEmpleado;
    private Empleado empleadoAEliminar; // Campo para el empleado a eliminar
    private boolean datosCargadosDesdeApi; // Controla si nombre/apellido deben estar bloqueados
    private List<Empleado> listaEmpleados;
    private String filtroGlobal;
    private List<SelectItem> sexosItems;
    private List<SelectItem> rolesItems;

    @PostConstruct
    public void init() {
        nuevoEmpleado = new Empleado();
        datosCargadosDesdeApi = false; // Inicialmente, los campos no están bloqueados
        cargarCatalogos();
        cargarEmpleados();
    }

    /**
     * Carga las listas de sexos y roles desde el CatalogoDAO y las prepara como
     * SelectItem. Se añaden mensajes de error si la carga falla.
     */
    public void cargarCatalogos() {
        sexosItems = new ArrayList<>();
        sexosItems.add(new SelectItem("", "Seleccione un sexo")); // Opción por defecto
        try {
            List<Sexo> sexos = catalogoDAO.obtenerSexos();
            if (sexos == null || sexos.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("formEmpleado:sexo",
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay sexos disponibles en el sistema."));
                FacesContext.getCurrentInstance().addMessage("formEditarEmpleado:editSexo",
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay sexos disponibles en el sistema."));
                LOGGER.log(Level.WARNING, "No se encontraron sexos en la base de datos.");
            } else {
                for (Sexo sexo : sexos) {
                    sexosItems.add(new SelectItem(sexo.getIdSexo(), sexo.getDescripcion()));
                }
                LOGGER.log(Level.INFO, "Se cargaron {0} sexos.", sexos.size());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar sexos: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage("formEmpleado:sexo",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los sexos: " + e.getMessage()));
            FacesContext.getCurrentInstance().addMessage("formEditarEmpleado:editSexo",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los sexos: " + e.getMessage()));
        }

        rolesItems = new ArrayList<>();
        rolesItems.add(new SelectItem("", "Seleccione un rol")); // Opción por defecto
        try {
            List<Rol> roles = catalogoDAO.obtenerRoles();
            if (roles == null || roles.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("formEmpleado:rol",
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay roles disponibles en el sistema."));
                FacesContext.getCurrentInstance().addMessage("formEditarEmpleado:editRol",
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay roles disponibles en el sistema."));
                LOGGER.log(Level.WARNING, "No se encontraron roles en la base de datos.");
            } else {
                for (Rol rol : roles) {
                    rolesItems.add(new SelectItem(rol.getIdRol(), rol.getDescripcion()));
                }
                LOGGER.log(Level.INFO, "Se cargaron {0} roles.", roles.size());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar roles: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage("formEmpleado:rol",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los roles: " + e.getMessage()));
            FacesContext.getCurrentInstance().addMessage("formEditarEmpleado:editRol",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los roles: " + e.getMessage()));
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
                LOGGER.log(Level.INFO, "Empleados filtrados por '{0}' cargados. Cantidad: {1}", new Object[]{filtroGlobal, listaEmpleados.size()});
            } else {
                listaEmpleados = empleadoDAO.obtenerTodosEmpleados();
                LOGGER.log(Level.INFO, "Todos los empleados cargados. Cantidad: {0}", listaEmpleados.size());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar empleados: " + e.getMessage(), e);
            listaEmpleados = new ArrayList<>(); // Asegura que la lista no sea null
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los empleados."));
        }
    }

    /**
     * Método para consultar el DNI y autocompletar campos del nuevoEmpleado.
     * Bloquea los campos de nombre y apellido si la consulta es exitosa.
     */
    public void consultarDniParaEmpleado() {
        FacesContext context = FacesContext.getCurrentInstance();
        String dni = nuevoEmpleado.getDni();

        // Limpiar campos y desbloquear por defecto antes de la consulta
        nuevoEmpleado.setNombre("");
        nuevoEmpleado.setApellido("");
        datosCargadosDesdeApi = false; // Desbloquear campos para permitir edición manual o nueva carga

        if (dni == null || dni.trim().isEmpty() || dni.trim().length() != 8) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El DNI debe tener 8 dígitos."));
            LOGGER.log(Level.WARNING, "Intento de consulta de DNI con formato inválido: {0}", dni);
            return;
        }

        try {
            Map<String, String> datosDni = dniApiService.consultarDni(dni.trim());

            if (datosDni != null && !datosDni.isEmpty()) {
                nuevoEmpleado.setNombre(datosDni.get("nombres"));
                String apellidoPaterno = datosDni.get("apellidoPaterno");
                String apellidoMaterno = datosDni.get("apellidoMaterno");

                // Concatena los apellidos, manejando posibles nulos o vacíos
                String apellidos = "";
                if (apellidoPaterno != null && !apellidoPaterno.isEmpty()) {
                    apellidos += apellidoPaterno;
                }
                if (apellidoMaterno != null && !apellidoMaterno.isEmpty()) {
                    if (!apellidos.isEmpty()) {
                        apellidos += " ";
                    }
                    apellidos += apellidoMaterno;
                }
                nuevoEmpleado.setApellido(apellidos.trim());

                datosCargadosDesdeApi = true; // Bloquear campos si la carga fue exitosa
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Datos de DNI cargados exitosamente."));
                LOGGER.log(Level.INFO, "Datos de DNI cargados para {0}: Nombre={1}, Apellido={2}", new Object[]{dni, nuevoEmpleado.getNombre(), nuevoEmpleado.getApellido()});
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "DNI no encontrado o no se pudieron obtener los datos. Verifique el número."));
                LOGGER.log(Level.WARNING, "DNI {0} no encontrado o datos no obtenidos de la API.", dni);
                datosCargadosDesdeApi = false; // Asegurarse de que los campos no estén bloqueados
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error al consultar DNI en EmpleadoBean para %s: %s", dni, e.getMessage()), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", "Ocurrió un error inesperado al validar el DNI."));
            datosCargadosDesdeApi = false; // Asegurarse de que los campos no estén bloqueados
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
                LOGGER.log(Level.INFO, mensajeExito + " ID: {0}", nuevoEmpleado.getIdUsuario());
                nuevoEmpleado = new Empleado(); // Limpiar el formulario para un nuevo ingreso
                datosCargadosDesdeApi = false; // Desbloquear campos para el siguiente ingreso
                cargarEmpleados(); // Recargar la lista de empleados en la tabla
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensajeExito));
            } else {
                LOGGER.log(Level.WARNING, mensajeError + " DNI: {0}", nuevoEmpleado.getDni());
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
     * @param empleado El objeto Empleado a ser eliminado.
     */
    public void prepararEliminacion(Empleado empleado) {
        this.empleadoAEliminar = empleado;
        LOGGER.log(Level.INFO, "Empleado {0} preparado para eliminación.", empleado.getDni());
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
                    LOGGER.log(Level.INFO, "Empleado con ID {0} eliminado exitosamente.", empleadoAEliminar.getIdUsuario());
                    cargarEmpleados(); // Recargar la lista de empleados en la tabla
                    empleadoAEliminar = null; // Limpiar el empleado a eliminar
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Empleado eliminado exitosamente."));
                } else {
                    LOGGER.log(Level.WARNING, "No se pudo eliminar el empleado con ID {0}.", empleadoAEliminar.getIdUsuario());
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el empleado."));
                }
            } else {
                LOGGER.log(Level.WARNING, "Intento de eliminar empleado nulo.");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se seleccionó ningún empleado para eliminar."));
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
        this.nuevoEmpleado = new Empleado(empleado); // Usando el constructor de copia
        datosCargadosDesdeApi = false; // Al editar, permitir la re-validación o edición manual de DNI/nombres/apellidos
        LOGGER.log(Level.INFO, "Preparando edición para empleado con DNI: {0}", nuevoEmpleado.getDni());
    }

    /**
     * Limpia el formulario de nuevo empleado y desbloquea los campos.
     */
    public void limpiarFormulario() {
        nuevoEmpleado = new Empleado();
        datosCargadosDesdeApi = false; // Desbloquear campos al limpiar
        LOGGER.log(Level.INFO, "Formulario de empleado limpiado.");
    }

    // --- Getters y Setters ---
    public Empleado getNuevoEmpleado() {
        return nuevoEmpleado;
    }

    public void setNuevoEmpleado(Empleado nuevoEmpleado) {
        this.nuevoEmpleado = nuevoEmpleado;
    }

    public Empleado getEmpleadoAEliminar() {
        return empleadoAEliminar;
    }

    public void setEmpleadoAEliminar(Empleado empleadoAEliminar) {
        this.empleadoAEliminar = empleadoAEliminar;
    }

    // Este getter es el que usará la vista para el atributo 'disabled'
    public boolean isDatosCargadosDesdeApi() {
        return datosCargadosDesdeApi;
    }

    // El setter no es estrictamente necesario si solo se controla internamente
    public void setDatosCargadosDesdeApi(boolean datosCargadosDesdeApi) {
        this.datosCargadosDesdeApi = datosCargadosDesdeApi;
    }

    public List<Empleado> getListaEmpleados() {
        // En ViewScoped, la lista se carga en @PostConstruct y en cargarEmpleados(),
        // por lo que no es necesario recargar aquí a menos que se quiera una carga perezosa.
        return listaEmpleados;
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