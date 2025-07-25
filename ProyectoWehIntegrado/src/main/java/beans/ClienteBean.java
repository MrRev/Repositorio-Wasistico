package beans;

import dao.ClienteDAO; // Importa la clase DAO para interactuar con la base de datos de clientes
import dao.CatalogoDAO; // Importa la clase DAO para obtener catálogos (como sexos)
import modelo.Cliente; // Importa el modelo Cliente
import modelo.Sexo; // Importa el modelo Sexo (para el select de sexos)
import util.DniApiService; // Importa el servicio para consultar la API de DNI

import jakarta.annotation.PostConstruct; // Para el método que se ejecuta al iniciar el bean
import jakarta.faces.application.FacesMessage; // Para mostrar mensajes al usuario en la UI
import jakarta.faces.context.FacesContext; // Para acceder al contexto de JSF (mensajes, etc.)
import jakarta.faces.model.SelectItem; // Para crear opciones para los selectOneMenu
import jakarta.faces.view.ViewScoped; // Define el ámbito del bean: vive mientras el usuario esté en la misma vista
import jakarta.inject.Inject; // Para inyectar otras clases (dependencias)
import jakarta.inject.Named; // Para que JSF reconozca este bean por su nombre (clienteBean)

import java.io.Serializable; // Para que el bean pueda ser serializado (importante para @ViewScoped)
import java.util.ArrayList; // Para crear listas dinámicas
import java.util.List; // Para usar el tipo List
import java.util.Map; // Para manejar la respuesta de la API de DNI
import java.util.logging.Level; // Para los niveles de mensajes de log (INFO, WARNING, SEVERE)
import java.util.logging.Logger; // Para registrar mensajes en la consola del servidor

@Named("clienteBean") // Nombre con el que se accederá a este bean desde la vista (ej. #{clienteBean.nuevoCliente})
@ViewScoped // El bean vive mientras el usuario esté en la misma página (vista)
public class ClienteBean implements Serializable {

    private static final long serialVersionUID = 1L; // Identificador para la serialización
    private static final Logger LOGGER = Logger.getLogger(ClienteBean.class.getName()); // Objeto para registrar mensajes

    @Inject // Inyecta una instancia de ClienteDAO
    private ClienteDAO clienteDAO;
    @Inject // Inyecta una instancia de CatalogoDAO
    private CatalogoDAO catalogoDAO;
    @Inject // Inyecta una instancia de DniApiService
    private DniApiService dniApiService;

    private Cliente nuevoCliente; // Objeto Cliente que se usa para el formulario de agregar/editar
    private Cliente clienteAEliminar; // Objeto Cliente temporal para la confirmación de eliminación
    private boolean datosCargadosDesdeApi; // Bandera para controlar si nombre/apellido están bloqueados en la UI
    private List<Cliente> listaClientes; // Lista de clientes que se muestra en la tabla
    private String filtroGlobal; // Texto para el filtro global de la tabla
    private List<SelectItem> sexosItems; // Lista de opciones para el select de sexo

    @PostConstruct // Este método se ejecuta justo después de que el bean es construido e inyectado
    public void init() {
        nuevoCliente = new Cliente(); // Inicializa un nuevo objeto Cliente para el formulario
        datosCargadosDesdeApi = false; // Por defecto, los campos de nombre/apellido no están bloqueados
        cargarCatalogos(); // Carga las opciones para los select (sexos)
        cargarClientes(); // Carga la lista inicial de clientes para la tabla
    }

    /**
     * Carga las listas de sexos desde el CatalogoDAO y las prepara como
     * SelectItem. Muestra mensajes de advertencia si no hay datos o de error si
     * falla la carga.
     */
    public void cargarCatalogos() {
        sexosItems = new ArrayList<>();
        sexosItems.add(new SelectItem("", "Seleccione un sexo")); // Añade una opción por defecto al inicio
        try {
            List<Sexo> sexos = catalogoDAO.obtenerSexos(); // Obtiene la lista de sexos de la base de datos
            if (sexos == null || sexos.isEmpty()) {
                // Si no hay sexos, muestra una advertencia en los mensajes de JSF
                FacesContext.getCurrentInstance().addMessage("formCliente:sexo",
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay sexos disponibles en el sistema."));
                FacesContext.getCurrentInstance().addMessage("formEditarCliente:editSexo",
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay sexos disponibles en el sistema."));
                LOGGER.log(Level.WARNING, "No se encontraron sexos en la base de datos.");
            } else {
                // Si hay sexos, los convierte en objetos SelectItem para el dropdown
                for (Sexo sexo : sexos) {
                    sexosItems.add(new SelectItem(sexo.getIdSexo(), sexo.getDescripcion()));
                }
                LOGGER.log(Level.INFO, "Se cargaron {0} sexos.", sexos.size());
            }
        } catch (Exception e) {
            // Si ocurre un error, lo registra y muestra un mensaje de error en la UI
            LOGGER.log(Level.SEVERE, "Error al cargar sexos: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage("formCliente:sexo",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los sexos: " + e.getMessage()));
            FacesContext.getCurrentInstance().addMessage("formEditarCliente:editSexo",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los sexos: " + e.getMessage()));
        }
    }

    /**
     * Carga la lista de clientes desde el ClienteDAO. Aplica un filtro global
     * si está definido. Muestra mensajes de error si la carga falla.
     */
    public void cargarClientes() {
        try {
            if (filtroGlobal != null && !filtroGlobal.trim().isEmpty()) {
                // Si hay un filtro, obtiene los clientes filtrados
                listaClientes = clienteDAO.obtenerClientesFiltrados(filtroGlobal);
                LOGGER.log(Level.INFO, "Clientes filtrados por '{0}' cargados. Cantidad: {1}", new Object[]{filtroGlobal, listaClientes.size()});
            } else {
                // Si no hay filtro, obtiene todos los clientes
                listaClientes = clienteDAO.obtenerTodosClientes();
                LOGGER.log(Level.INFO, "Todos los clientes cargados. Cantidad: {0}", listaClientes.size());
            }
        } catch (Exception e) {
            // Si ocurre un error, lo registra y muestra un mensaje de error en la UI
            LOGGER.log(Level.SEVERE, "Error al cargar clientes: " + e.getMessage(), e);
            listaClientes = new ArrayList<>(); // Asegura que la lista no sea null para evitar NullPointerException en la UI
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los clientes."));
        }
    }

    /**
     * Consulta la API de DNI para autocompletar los campos de nombre y apellido
     * del nuevoCliente. Si la consulta es exitosa, bloquea esos campos en la
     * UI.
     */
    public void consultarDniParaCliente() {
        FacesContext context = FacesContext.getCurrentInstance();
        String dni = nuevoCliente.getDni(); // Obtiene el DNI ingresado en el formulario

        // Limpiar campos de nombre y apellido y desbloquearlos antes de cada nueva consulta
        nuevoCliente.setNombre("");
        nuevoCliente.setApellido("");
        datosCargadosDesdeApi = false; // Resetea la bandera de bloqueo

        // Valida que el DNI tenga 8 dígitos
        if (dni == null || dni.trim().isEmpty() || dni.trim().length() != 8) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El DNI debe tener 8 dígitos."));
            LOGGER.log(Level.WARNING, "Intento de consulta de DNI con formato inválido: {0}", dni);
            return; // Sale del método si el DNI es inválido
        }

        try {
            // Llama al servicio de la API de DNI para obtener los datos
            Map<String, String> datosDni = dniApiService.consultarDni(dni.trim());

            if (datosDni != null && !datosDni.isEmpty()) {
                // Si se obtuvieron datos, los asigna al objeto nuevoCliente
                nuevoCliente.setNombre(datosDni.get("nombres"));
                String apellidoPaterno = datosDni.get("apellidoPaterno");
                String apellidoMaterno = datosDni.get("apellidoMaterno");

                // Concatena los apellidos, manejando posibles valores nulos o vacíos
                String apellidos = "";
                if (apellidoPaterno != null && !apellidoPaterno.isEmpty()) {
                    apellidos += apellidoPaterno;
                }
                if (apellidoMaterno != null && !apellidoMaterno.isEmpty()) {
                    if (!apellidos.isEmpty()) { // Si ya hay un apellido paterno, añade un espacio
                        apellidos += " ";
                    }
                    apellidos += apellidoMaterno;
                }
                nuevoCliente.setApellido(apellidos.trim()); // Asigna el apellido completo al cliente

                datosCargadosDesdeApi = true; // Establece la bandera a true para bloquear los campos en la UI
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Datos de DNI cargados exitosamente."));
                LOGGER.log(Level.INFO, "Datos de DNI cargados para {0}: Nombre={1}, Apellido={2}", new Object[]{dni, nuevoCliente.getNombre(), nuevoCliente.getApellido()});
            } else {
                // Si no se obtuvieron datos, muestra una advertencia y los campos permanecen desbloqueados
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "DNI no encontrado o no se pudieron obtener los datos. Verifique el número."));
                LOGGER.log(Level.WARNING, "DNI {0} no encontrado o datos no obtenidos de la API.", dni);
                datosCargadosDesdeApi = false;
            }
        } catch (Exception e) {
            // Captura cualquier excepción durante la consulta a la API, la registra y muestra un error
            LOGGER.log(Level.SEVERE, String.format("Error al consultar DNI en ClienteBean para %s: %s", dni, e.getMessage()), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", "Ocurrió un error inesperado al validar el DNI."));
            datosCargadosDesdeApi = false; // Asegura que los campos no estén bloqueados si hay un error
        }
    }

    /**
     * Guarda un nuevo cliente en la base de datos o actualiza uno existente.
     * Muestra mensajes de éxito o error al usuario.
     */
    public void guardarCliente() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            boolean exito;
            String mensajeExito;
            String mensajeError;

            if (nuevoCliente.getIdCliente() == 0) { // Si el ID es 0, es un cliente nuevo (ID autogenerado por DB)
                exito = clienteDAO.agregarCliente(nuevoCliente);
                mensajeExito = "Cliente agregado exitosamente.";
                mensajeError = "No se pudo agregar el cliente.";
            } else { // Si el ID es diferente de 0, es una actualización de un cliente existente
                exito = clienteDAO.actualizarCliente(nuevoCliente);
                mensajeExito = "Cliente actualizado exitosamente.";
                mensajeError = "No se pudo actualizar el cliente.";
            }

            if (exito) {
                LOGGER.log(Level.INFO, mensajeExito + " ID: {0}", nuevoCliente.getIdCliente());
                nuevoCliente = new Cliente(); // Limpia el formulario creando un nuevo objeto Cliente
                datosCargadosDesdeApi = false; // Desbloquea los campos para el siguiente ingreso
                cargarClientes(); // Recarga la lista de clientes en la tabla para mostrar los cambios
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensajeExito));
            } else {
                LOGGER.log(Level.WARNING, mensajeError + " DNI: {0}", nuevoCliente.getDni());
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", mensajeError));
            }
        } catch (Exception e) {
            // Captura cualquier excepción durante el guardado/actualización
            LOGGER.log(Level.SEVERE, "Error al guardar/actualizar cliente: " + e.getMessage(), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error Grave", "Ocurrió un error inesperado al guardar el cliente."));
        }
    }

    /**
     * Prepara el cliente para ser eliminado, almacenándolo temporalmente en
     * 'clienteAEliminar'.
     *
     * @param cliente El objeto Cliente a ser eliminado.
     */
    public void prepararEliminacion(Cliente cliente) {
        this.clienteAEliminar = cliente;
        LOGGER.log(Level.INFO, "Cliente {0} preparado para eliminación.", cliente.getDni());
    }

    /**
     * Elimina el cliente seleccionado (almacenado en 'clienteAEliminar') de la
     * base de datos. Muestra mensajes de éxito o error al usuario.
     */
    public void eliminarCliente() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            if (clienteAEliminar != null) { // Verifica que haya un cliente seleccionado para eliminar
                boolean exito = clienteDAO.eliminarCliente(clienteAEliminar.getIdCliente());
                if (exito) {
                    LOGGER.log(Level.INFO, "Cliente con ID {0} eliminado exitosamente.", clienteAEliminar.getIdCliente());
                    cargarClientes(); // Recarga la lista de clientes en la tabla
                    clienteAEliminar = null; // Limpia el objeto temporal
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Cliente eliminado exitosamente."));
                } else {
                    LOGGER.log(Level.WARNING, "No se pudo eliminar el cliente con ID {0}.", clienteAEliminar.getIdCliente());
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el cliente."));
                }
            } else {
                LOGGER.log(Level.WARNING, "Intento de eliminar cliente nulo.");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se seleccionó ningún cliente para eliminar."));
            }
        } catch (Exception e) {
            // Captura cualquier excepción durante la eliminación
            LOGGER.log(Level.SEVERE, "Error al eliminar cliente: " + e.getMessage(), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error Grave", "Ocurrió un error inesperado al eliminar el cliente."));
        }
    }

    /**
     * Prepara el formulario para la edición de un cliente existente. Clona el
     * objeto para evitar modificar directamente la lista hasta que se guarde.
     *
     * @param cliente El cliente a editar.
     */
    public void editarCliente(Cliente cliente) {
        // Crea una copia del cliente para editar, para no modificar directamente el objeto de la lista
        this.nuevoCliente = new Cliente(cliente); // Usa el constructor de copia de Cliente
        datosCargadosDesdeApi = false; // Al editar, los campos de nombre/apellido se desbloquean para permitir cambios o re-validación
        LOGGER.log(Level.INFO, "Preparando edición para cliente con DNI: {0}", nuevoCliente.getDni());
    }

    /**
     * Limpia el formulario de nuevo cliente y desbloquea los campos de
     * nombre/apellido.
     */
    public void limpiarFormulario() {
        nuevoCliente = new Cliente(); // Crea una nueva instancia de Cliente para resetear el formulario
        datosCargadosDesdeApi = false; // Desbloquea los campos de nombre/apellido
        LOGGER.log(Level.INFO, "Formulario de cliente limpiado.");
    }

    // --- Getters y Setters ---
    // Estos métodos son necesarios para que JSF pueda acceder a las propiedades del bean
    public Cliente getNuevoCliente() {
        return nuevoCliente;
    }

    public void setNuevoCliente(Cliente nuevoCliente) {
        this.nuevoCliente = nuevoCliente;
    }

    public Cliente getClienteAEliminar() {
        return clienteAEliminar;
    }

    public void setClienteAEliminar(Cliente clienteAEliminar) {
        this.clienteAEliminar = clienteAEliminar;
    }

    // Este getter es el que la vista usa para el atributo 'disabled' en los campos de nombre/apellido
    public boolean isDatosCargadosDesdeApi() {
        return datosCargadosDesdeApi;
    }

    // El setter para datosCargadosDesdeApi puede no ser estrictamente necesario si solo se controla internamente
    public void setDatosCargadosDesdeApi(boolean datosCargadosDesdeApi) {
        this.datosCargadosDesdeApi = datosCargadosDesdeApi;
    }

    public List<Cliente> getListaClientes() {
        return listaClientes;
    }

    // El setter para listaClientes no es estrictamente necesario si solo se carga internamente
    public void setListaClientes(List<Cliente> listaClientes) {
        this.listaClientes = listaClientes;
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

    // El setter para sexosItems no es estrictamente necesario si solo se carga internamente
    public void setSexosItems(List<SelectItem> sexosItems) {
        this.sexosItems = sexosItems;
    }
}
