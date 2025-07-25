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

@Named("productoBean")
@SessionScoped
public class ProductoBean implements Serializable {

    private Producto producto = new Producto();
    private List<Producto> listaProductos;
    private List<Categoria> listaCategorias;
    private String filtro;

    private Producto productoSeleccionado; // para editar/eliminar

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO;

    public ProductoBean() throws SQLException {
        this.categoriaDAO = new CategoriaDAO();
    }

    @PostConstruct
    public void init() {
        try {
            listarProductos();
            listaCategorias = categoriaDAO.listarCategorias();
            productoSeleccionado = new Producto();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void listarProductos() {
        // Este método ahora puede usar el filtro si está presente, o listar todos si no.
        // Sin embargo, para la tabla principal, mantenemos listarTodos por claridad.
        // El método buscarProductos se encargará de aplicar el filtro.
        listaProductos = productoDAO.listarProductos();
    }

    public void agregarProducto() {
        if (productoDAO.agregarProducto(producto)) {
            listarProductos();
            producto = new Producto();
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
        this.filtro = null; // O this.filtro = "";
        listarProductos(); // Recarga la lista completa sin filtro
    }

    public void prepararEliminar(Producto producto) {
        if (producto != null) {
            this.productoSeleccionado = producto;
        }
    }

    public void eliminarProducto() {
        if (productoSeleccionado != null && productoSeleccionado.getIdProducto() > 0) {
            productoDAO.eliminarProducto(productoSeleccionado.getIdProducto());
            listarProductos();
            productoSeleccionado = new Producto();
        }
    }

    public void prepararEditar(Producto producto) {
        if (producto != null) {
            this.productoSeleccionado = new Producto();
            this.productoSeleccionado.setIdProducto(producto.getIdProducto());
            this.productoSeleccionado.setIdCategoria(producto.getIdCategoria());
            this.productoSeleccionado.setNombre(producto.getNombre());
            this.productoSeleccionado.setDescripcion(producto.getDescripcion());
            this.productoSeleccionado.setPrecioUnitario(producto.getPrecioUnitario());
            this.productoSeleccionado.setStock(producto.getStock());
        }
    }

    public void modificarProducto() {
        if (productoSeleccionado != null && productoSeleccionado.getIdProducto() > 0) {
            productoDAO.modificarProducto(productoSeleccionado);
            listarProductos();
            productoSeleccionado = new Producto();
        }
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
