package beans;

import dao.VentaDAO;
import dao.ProductoDAO;
import dao.ClienteDAO;
import modelo.Venta;
import modelo.DetalleVenta;
import modelo.Producto;
import modelo.Cliente;
import modelo.Empleado; // Para el empleado logueado

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped; // Usamos ViewScoped para el flujo de una venta específica
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date; // Para java.util.Date
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Managed Bean para gestionar el proceso de ventas. Orquesta la selección de
 * clientes y productos, la gestión del carrito, el cálculo del total y el
 * registro final de la venta.
 */
@Named("ventaBean")
@ViewScoped // El bean vive mientras el usuario esté en la misma página de venta
public class VentaBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VentaBean.class.getName());

    @Inject
    private VentaDAO ventaDAO;
    @Inject
    private ProductoDAO productoDAO;
    @Inject
    private ClienteDAO clienteDAO;

    // Propiedades para la venta actual
    private Venta ventaActual;
    private List<DetalleVenta> carrito;
    private BigDecimal totalVenta;

    // Propiedades para la selección de cliente
    private Cliente clienteSeleccionado;
    private String filtroClienteDni; // Usado para el filtro de clientes en la UI
    private List<Cliente> clientesDisponibles;

    // Propiedades para la selección de producto y adición al carrito
    private Producto productoSeleccionadoParaCarrito;
    private int cantidadProductoParaCarrito;
    private String filtroProductoNombre; // Usado para el filtro de productos en la UI
    private List<Producto> productosDisponibles;

    // Empleado logueado que realiza la venta
    private Empleado empleadoLogueado;

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Inicializando VentaBean...");
        limpiarFormularioVenta(); // Inicializa una nueva venta y carrito
        // Las siguientes llamadas se realizan dentro de limpiarFormularioVenta()
        // cargarClientesDisponibles(); 
        // cargarProductosDisponibles(); 
        empleadoLogueado = obtenerEmpleadoLogueado(); // Obtiene el empleado de la sesión
        if (empleadoLogueado == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error de Sesión", "No se pudo obtener el empleado logueado. Por favor, inicie sesión nuevamente."));
            LOGGER.log(Level.SEVERE, "No se pudo obtener el empleado logueado al iniciar VentaBean.");
            // Opcional: Redirigir a la página de login si no hay empleado logueado
            // try { FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml"); } catch (Exception e) {}
        }
    }

    /**
     * Carga la lista de clientes disponibles, aplicando un filtro si existe.
     * Utiliza el filtroClienteDni para buscar por DNI o nombre/apellido.
     */
    public void cargarClientesDisponibles() {
        try {
            if (filtroClienteDni != null && !filtroClienteDni.trim().isEmpty()) {
                // Asume que clienteDAO.obtenerClientesFiltrados busca por DNI o nombre/apellido
                clientesDisponibles = clienteDAO.obtenerClientesFiltrados(filtroClienteDni.trim());
                LOGGER.log(Level.INFO, "Clientes filtrados por DNI/nombre '{0}' cargados. Cantidad: {1}", new Object[]{filtroClienteDni, clientesDisponibles.size()});
            } else {
                clientesDisponibles = clienteDAO.obtenerTodosClientes();
                LOGGER.log(Level.INFO, "Todos los clientes cargados. Cantidad: {0}", clientesDisponibles.size());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar clientes disponibles: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los clientes."));
            clientesDisponibles = new ArrayList<>(); // Asegura que no sea null
        }
    }

    /**
     * Carga la lista de productos disponibles, aplicando un filtro si existe.
     * Utiliza el filtroProductoNombre para buscar por nombre o descripción.
     */
    public void cargarProductosDisponibles() {
        try {
            if (filtroProductoNombre != null && !filtroProductoNombre.trim().isEmpty()) {
                // Utiliza el método obtenerProductosFiltrados del ProductoDAO
                productosDisponibles = productoDAO.obtenerProductosFiltrados(filtroProductoNombre.trim());
                LOGGER.log(Level.INFO, "Productos filtrados por nombre/descripción '{0}' cargados. Cantidad: {1}", new Object[]{filtroProductoNombre, productosDisponibles.size()});
            } else {
                productosDisponibles = productoDAO.listarProductos();
                LOGGER.log(Level.INFO, "Todos los productos cargados. Cantidad: {0}", productosDisponibles.size());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar productos disponibles: " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los productos."));
            productosDisponibles = new ArrayList<>(); // Asegura que no sea null
        }
    }

    /**
     * Selecciona un cliente para la venta actual.
     *
     * @param cliente El cliente seleccionado de la lista.
     */
    public void seleccionarCliente(Cliente cliente) {
        if (cliente != null) {
            this.clienteSeleccionado = cliente;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Cliente Seleccionado", "Cliente: " + cliente.getNombre() + " " + cliente.getApellido()));
            LOGGER.log(Level.INFO, "Cliente seleccionado para la venta: {0}", cliente.getDni());
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se seleccionó ningún cliente."));
            LOGGER.log(Level.WARNING, "Intento de seleccionar cliente nulo.");
        }
    }

    /**
     * Añade el producto seleccionado con la cantidad especificada al carrito de
     * compras. Realiza validaciones de stock y cantidad.
     */
    public void agregarProductoAlCarrito() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (productoSeleccionadoParaCarrito == null || productoSeleccionadoParaCarrito.getIdProducto() == 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe seleccionar un producto."));
            LOGGER.log(Level.WARNING, "Intento de agregar producto nulo al carrito.");
            return;
        }
        if (cantidadProductoParaCarrito <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La cantidad debe ser mayor a cero."));
            LOGGER.log(Level.WARNING, "Intento de agregar producto con cantidad inválida: {0}", cantidadProductoParaCarrito);
            return;
        }

        // Recuperar el producto más reciente de la base de datos para verificar el stock
        Producto productoActualizado = productoDAO.obtenerProductoPorId(productoSeleccionadoParaCarrito.getIdProducto());
        if (productoActualizado == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El producto seleccionado ya no está disponible."));
            LOGGER.log(Level.SEVERE, "Producto con ID {0} no encontrado al intentar agregar al carrito.", productoSeleccionadoParaCarrito.getIdProducto());
            return;
        }
        
        // Actualizar el objeto productoSeleccionadoParaCarrito con el stock más reciente
        productoSeleccionadoParaCarrito.setStock(productoActualizado.getStock());

        // Verificar si el producto ya está en el carrito
        DetalleVenta detalleExistente = null;
        for (DetalleVenta dv : carrito) {
            if (dv.getIdProducto() == productoSeleccionadoParaCarrito.getIdProducto()) {
                detalleExistente = dv;
                break;
            }
        }

        int cantidadEnCarritoActual = (detalleExistente != null) ? detalleExistente.getCantidad() : 0;
        int nuevaCantidadTotal = cantidadEnCarritoActual + cantidadProductoParaCarrito;

        if (nuevaCantidadTotal > productoSeleccionadoParaCarrito.getStock()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Stock", "La cantidad total para " + productoSeleccionadoParaCarrito.getNombre() + " (" + nuevaCantidadTotal + ") excede el stock disponible (" + productoSeleccionadoParaCarrito.getStock() + ")."));
            LOGGER.log(Level.WARNING, "Stock insuficiente para producto {0}. Solicitado: {1}, Disponible: {2}", new Object[]{productoSeleccionadoParaCarrito.getNombre(), nuevaCantidadTotal, productoSeleccionadoParaCarrito.getStock()});
            return;
        }

        if (detalleExistente != null) {
            // Si el producto ya está, actualiza la cantidad
            detalleExistente.setCantidad(nuevaCantidadTotal);
            detalleExistente.setSubtotal(productoSeleccionadoParaCarrito.getPrecioUnitario().multiply(new BigDecimal(nuevaCantidadTotal)));
            LOGGER.log(Level.INFO, "Cantidad de producto {0} actualizada en el carrito a {1}.", new Object[]{productoSeleccionadoParaCarrito.getNombre(), nuevaCantidadTotal});
        } else {
            // Si no está, añade un nuevo detalle
            DetalleVenta nuevoDetalle = new DetalleVenta();
            nuevoDetalle.setIdProducto(productoSeleccionadoParaCarrito.getIdProducto());
            nuevoDetalle.setCantidad(cantidadProductoParaCarrito);
            nuevoDetalle.setPrecioUnitario(productoSeleccionadoParaCarrito.getPrecioUnitario()); // Usar el precio unitario del producto
            nuevoDetalle.setSubtotal(productoSeleccionadoParaCarrito.getPrecioUnitario().multiply(new BigDecimal(cantidadProductoParaCarrito)));
            carrito.add(nuevoDetalle);
            LOGGER.log(Level.INFO, "Producto {0} añadido al carrito con cantidad {1}.", new Object[]{productoSeleccionadoParaCarrito.getNombre(), cantidadProductoParaCarrito});
        }

        calcularTotalVenta(); // Recalcula el total de la venta
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Producto Añadido", productoSeleccionadoParaCarrito.getNombre() + " añadido al carrito."));

        // Limpiar campos de selección de producto para el siguiente
        productoSeleccionadoParaCarrito = null;
        cantidadProductoParaCarrito = 0;
        // No limpiar filtroProductoNombre aquí para mantener el filtro activo si el usuario lo desea
        // cargarProductosDisponibles(); // Recargar productos para reflejar stock si se desea (aunque el stock real se actualiza en DB al finalizar venta)
    }

    /**
     * Elimina un detalle de venta del carrito.
     *
     * @param detalle El detalle de venta a eliminar.
     */
    public void eliminarProductoDelCarrito(DetalleVenta detalle) {
        if (detalle != null && carrito.remove(detalle)) {
            calcularTotalVenta();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Producto Eliminado", "Producto eliminado del carrito."));
            LOGGER.log(Level.INFO, "Producto con ID {0} eliminado del carrito.", detalle.getIdProducto());
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se pudo eliminar el producto del carrito."));
            LOGGER.log(Level.WARNING, "Intento de eliminar producto nulo o no encontrado del carrito.");
        }
    }

    /**
     * Actualiza la cantidad de un producto en el carrito.
     *
     * @param detalle El detalle de venta a actualizar.
     */
    public void actualizarCantidadEnCarrito(DetalleVenta detalle) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (detalle == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Detalle de venta nulo."));
            LOGGER.log(Level.WARNING, "Intento de actualizar detalle de venta nulo.");
            return;
        }
        if (detalle.getCantidad() <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La cantidad debe ser mayor a cero."));
            LOGGER.log(Level.WARNING, "Cantidad inválida para detalle de venta con ID producto: {0}, Cantidad: {1}", new Object[]{detalle.getIdProducto(), detalle.getCantidad()});
            // Opcional: Eliminar el detalle si la cantidad es 0 o negativa
            // carrito.remove(detalle);
            // calcularTotalVenta();
            return;
        }

        // Recuperar el producto original de la base de datos para verificar el stock
        Producto productoOriginal = productoDAO.obtenerProductoPorId(detalle.getIdProducto());
        if (productoOriginal == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Producto no encontrado para actualizar cantidad."));
            LOGGER.log(Level.SEVERE, "Producto con ID {0} no encontrado al actualizar cantidad en carrito.", detalle.getIdProducto());
            return;
        }

        if (detalle.getCantidad() > productoOriginal.getStock()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Stock", "La cantidad para " + productoOriginal.getNombre() + " (" + detalle.getCantidad() + ") excede el stock disponible (" + productoOriginal.getStock() + ")."));
            LOGGER.log(Level.WARNING, "Stock insuficiente al actualizar cantidad para producto {0}. Solicitado: {1}, Disponible: {2}", new Object[]{productoOriginal.getNombre(), detalle.getCantidad(), productoOriginal.getStock()});
            // Revertir la cantidad a la última válida o al stock máximo si se prefiere
            // detalle.setCantidad(productoOriginal.getStock());
            return;
        }

        detalle.setSubtotal(detalle.getPrecioUnitario().multiply(new BigDecimal(detalle.getCantidad())));
        calcularTotalVenta();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Cantidad Actualizada", "Cantidad de producto actualizada."));
        LOGGER.log(Level.INFO, "Cantidad actualizada para producto con ID {0} a {1}.", new Object[]{detalle.getIdProducto(), detalle.getCantidad()});
    }

    /**
     * Calcula el total de la venta sumando los subtotales de todos los ítems en
     * el carrito.
     */
    public void calcularTotalVenta() {
        totalVenta = BigDecimal.ZERO;
        for (DetalleVenta detalle : carrito) {
            totalVenta = totalVenta.add(detalle.getSubtotal());
        }
        LOGGER.log(Level.INFO, "Total de la venta recalculado: {0}", totalVenta);
    }

    /**
     * Finaliza el proceso de venta, registrando la venta principal y sus
     * detalles en la base de datos, y actualizando el stock de productos.
     *
     * @return String de navegación (puede ser null para quedarse en la misma
     * página con mensajes).
     */
    public String finalizarVenta() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (clienteSeleccionado == null || clienteSeleccionado.getIdCliente() == 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe seleccionar un cliente para la venta."));
            LOGGER.log(Level.WARNING, "Intento de finalizar venta sin cliente seleccionado.");
            return null;
        }
        if (carrito.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El carrito de compras está vacío. Añada productos para finalizar la venta."));
            LOGGER.log(Level.WARNING, "Intento de finalizar venta con carrito vacío.");
            return null;
        }
        if (empleadoLogueado == null || empleadoLogueado.getIdUsuario() == 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error de Sesión", "No se pudo identificar al empleado que realiza la venta. Por favor, reinicie la sesión."));
            LOGGER.log(Level.SEVERE, "Empleado logueado no válido al intentar finalizar venta.");
            return null;
        }

        // Asignar datos a la venta actual
        ventaActual.setIdCliente(clienteSeleccionado.getIdCliente());
        ventaActual.setIdUsuario(empleadoLogueado.getIdUsuario());
        ventaActual.setFechaVenta(new Date()); // Fecha actual de la venta
        ventaActual.setFechaCreacion(new Date()); // Fecha de creación (podría ser la misma que fechaVenta)
        ventaActual.setTotal(totalVenta);

        try {
            // Este método (registrarVentaCompleta) en VentaDAO DEBE manejar la transacción
            // de guardar la venta, sus detalles y la reducción del stock de cada producto.
            // Es CRÍTICO que VentaDAO.registrarVentaCompleta() llame a productoDAO.reducirStock()
            // por cada item del carrito y que todo esto esté dentro de una transacción de BD.
            boolean exito = ventaDAO.registrarVentaCompleta(ventaActual, carrito);
            if (exito) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Venta registrada exitosamente con ID: " + ventaActual.getIdVenta()));
                LOGGER.log(Level.INFO, "Venta {0} registrada exitosamente.", ventaActual.getIdVenta());
                limpiarFormularioVenta(); // Limpiar el formulario para una nueva venta
                // Recargar los productos disponibles para reflejar la reducción de stock en la tabla de selección
                cargarProductosDisponibles(); 
                return "ventas?faces-redirect=true"; // O redirigir a una página de confirmación/listado
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo registrar la venta. Verifique los datos e intente de nuevo."));
                LOGGER.log(Level.WARNING, "Fallo al registrar la venta.");
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Excepción al intentar finalizar la venta: " + e.getMessage(), e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error Grave", "Ocurrió un error inesperado al procesar la venta."));
            return null;
        }
    }

    /**
     * Reinicia el formulario de venta, limpiando el carrito y seleccionando un
     * nuevo cliente.
     */
    public void limpiarFormularioVenta() {
        ventaActual = new Venta();
        carrito = new ArrayList<>();
        totalVenta = BigDecimal.ZERO;
        clienteSeleccionado = null;
        productoSeleccionadoParaCarrito = null;
        cantidadProductoParaCarrito = 0;
        filtroClienteDni = null;
        filtroProductoNombre = null;
        cargarClientesDisponibles(); // Recargar clientes
        cargarProductosDisponibles(); // Recargar productos
        LOGGER.log(Level.INFO, "Formulario de venta limpiado.");
    }

    /**
     * Obtiene el objeto Empleado logueado de la sesión HTTP.
     *
     * @return El objeto Empleado logueado, o null si no hay sesión o no está
     * logueado.
     */
    private Empleado obtenerEmpleadoLogueado() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (session != null) {
            return (Empleado) session.getAttribute("usuarioLogueado");
        }
        return null;
    }

    // --- Getters y Setters ---
    public Venta getVentaActual() {
        return ventaActual;
    }

    public void setVentaActual(Venta ventaActual) {
        this.ventaActual = ventaActual;
    }

    public List<DetalleVenta> getCarrito() {
        return carrito;
    }

    public void setCarrito(List<DetalleVenta> carrito) {
        this.carrito = carrito;
    }

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }

    public Cliente getClienteSeleccionado() {
        return clienteSeleccionado;
    }

    public void setClienteSeleccionado(Cliente clienteSeleccionado) {
        this.clienteSeleccionado = clienteSeleccionado;
    }

    public String getFiltroClienteDni() {
        return filtroClienteDni;
    }

    public void setFiltroClienteDni(String filtroClienteDni) {
        this.filtroClienteDni = filtroClienteDni;
    }

    public List<Cliente> getClientesDisponibles() {
        return clientesDisponibles;
    }

    public void setClientesDisponibles(List<Cliente> clientesDisponibles) {
        this.clientesDisponibles = clientesDisponibles;
    }

    public Producto getProductoSeleccionadoParaCarrito() {
        return productoSeleccionadoParaCarrito;
    }

    public void setProductoSeleccionadoParaCarrito(Producto productoSeleccionadoParaCarrito) {
        this.productoSeleccionadoParaCarrito = productoSeleccionadoParaCarrito;
    }

    public int getCantidadProductoParaCarrito() {
        return cantidadProductoParaCarrito;
    }

    public void setCantidadProductoParaCarrito(int cantidadProductoParaCarrito) {
        this.cantidadProductoParaCarrito = cantidadProductoParaCarrito;
    }

    public String getFiltroProductoNombre() {
        return filtroProductoNombre;
    }

    public void setFiltroProductoNombre(String filtroProductoNombre) {
        this.filtroProductoNombre = filtroProductoNombre;
    }

    public List<Producto> getProductosDisponibles() {
        return productosDisponibles;
    }

    public void setProductosDisponibles(List<Producto> productosDisponibles) {
        this.productosDisponibles = productosDisponibles;
    }

    public Empleado getEmpleadoLogueado() {
        return empleadoLogueado;
    }

    public void setEmpleadoLogueado(Empleado empleadoLogueado) {
        this.empleadoLogueado = empleadoLogueado;
    }
}
