package beans;

import dao.ProductoDAO;
import dao.CategoriaDAO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import modelo.Producto;
import modelo.Categoria;
import jakarta.faces.application.FacesMessage; // Importar FacesMessage
import jakarta.faces.context.FacesContext;     // Importar FacesContext

@Named("productoBean")
@SessionScoped
public class ProductoBean implements Serializable {

    private Producto producto = new Producto(); // Usado para agregar nuevos productos
    private List<Producto> listaProductos;
    private List<Categoria> listaCategorias;
    private String filtro;

    private Producto productoSeleccionado; // Usado para editar/eliminar

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO;

    public ProductoBean() throws SQLException {
        this.categoriaDAO = new CategoriaDAO();
    }

    @PostConstruct
    public void init() {
        try {
            listarProductos();
            cargarCategorias(); // Llama al nuevo método para cargar categorías
            productoSeleccionado = new Producto(); // Inicializa para evitar NullPointerException
        } catch (SQLException e) {
            e.printStackTrace();
            // Considerar añadir un FacesMessage para notificar al usuario sobre el error
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Inicialización", "No se pudieron cargar los datos iniciales: " + e.getMessage()));
        }
    }

    /**
     * Carga la lista completa de productos desde la base de datos. Este método
     * se usa para la carga inicial y después de operaciones de CUD.
     */
    public void listarProductos() {
        listaProductos = productoDAO.listarProductos();
    }

    /**
     * Agrega un nuevo producto a la base de datos. Después de agregar, recarga
     * la lista de productos y reinicia el objeto 'producto'.
     */
    public void agregarProducto() {
        if (productoDAO.agregarProducto(producto)) {
            listarProductos(); // Recargar la tabla
            limpiarFormulario(); // Limpiar el formulario de agregar
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Producto agregado correctamente."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo agregar el producto. Verifique los datos y la conexión a la base de datos."));
        }
    }

    /**
     * Busca productos en la base de datos utilizando el filtro global. Este
     * método se invoca desde la UI cuando se aplica el filtro.
     */
    public void buscarProductos() {
        // Llama al método obtenerProductosFiltrados del DAO, que maneja si el filtro está vacío o no.
        listaProductos = productoDAO.obtenerProductosFiltrados(filtro);
    }

    /**
     * Limpia el filtro de búsqueda y recarga la lista completa de productos.
     */
    public void limpiarFiltro() {
        this.filtro = null; // O this.filtro = ""; para asegurar que el inputText se vacíe
        listarProductos(); // Recarga la lista completa sin filtro
    }

    /**
     * Prepara el producto seleccionado para su eliminación.
     *
     * @param producto El producto a eliminar.
     */
    public void prepararEliminar(Producto producto) {
        if (producto != null) {
            this.productoSeleccionado = producto;
        }
    }

    /**
     * Elimina el producto seleccionado de la base de datos. Después de
     * eliminar, recarga la lista de productos y reinicia el objeto
     * 'productoSeleccionado'.
     */
    public void eliminarProducto() {
        if (productoSeleccionado != null && productoSeleccionado.getIdProducto() > 0) {
            if (productoDAO.eliminarProducto(productoSeleccionado.getIdProducto())) {
                listarProductos(); // Recargar la tabla
                productoSeleccionado = new Producto(); // Limpiar el producto seleccionado
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Producto eliminado correctamente."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el producto."));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se seleccionó ningún producto para eliminar."));
        }
    }

    /**
     * Prepara el producto seleccionado para su edición. Se crea una nueva
     * instancia de Producto para evitar problemas de referencia directa.
     *
     * @param producto El producto a editar.
     */
    public void prepararEditar(Producto producto) {
        if (producto != null) {
            this.productoSeleccionado = new Producto(); // Nueva instancia
            // Asignación de valores
            this.productoSeleccionado.setIdProducto(producto.getIdProducto());
            this.productoSeleccionado.setIdCategoria(producto.getIdCategoria());
            this.productoSeleccionado.setNombre(producto.getNombre());
            this.productoSeleccionado.setDescripcion(producto.getDescripcion());
            this.productoSeleccionado.setPrecioUnitario(producto.getPrecioUnitario());
            this.productoSeleccionado.setStock(producto.getStock());
            this.productoSeleccionado.setCategoriaNombre(producto.getCategoriaNombre()); // Asegúrate de copiar también el nombre de la categoría
        }
    }

    /**
     * Modifica el producto seleccionado en la base de datos. Después de
     * modificar, recarga la lista de productos y reinicia el objeto
     * 'productoSeleccionado'.
     */
    public void modificarProducto() {
        if (productoSeleccionado != null && productoSeleccionado.getIdProducto() > 0) {
            if (productoDAO.modificarProducto(productoSeleccionado)) {
                listarProductos(); // Recargar la tabla
                productoSeleccionado = new Producto(); // Limpiar el producto seleccionado
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Producto modificado correctamente."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo modificar el producto."));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se seleccionó ningún producto para modificar."));
        }
    }

    /**
     * Carga la lista de categorías desde la base de datos. Este método es útil
     * para poblar los selectOneMenu de categorías.
     */
    public void cargarCategorias() throws SQLException {
        listaCategorias = categoriaDAO.listarCategorias();
    }

    /**
     * Limpia los campos del formulario de agregar producto.
     */
    public void limpiarFormulario() {
        this.producto = new Producto(); // Reinicia el objeto 'producto' para un nuevo ingreso
    }

    // GETTERS & SETTERS
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public List<Categoria> getListaCategorias() {
        return listaCategorias;
    }

    public void setListaCategorias(List<Categoria> listaCategorias) {
        this.listaCategorias = listaCategorias;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }
}
