package modelo;

import java.io.Serializable;
import java.math.BigDecimal; // Importar BigDecimal

public class Producto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idProducto;
    private int idCategoria;
    private String nombre;
    private String descripcion;
    private BigDecimal precioUnitario; // CAMBIO AQUÍ: BigDecimal
    private int stockDisponible;
    private String categoriaNombre; // Para mostrar la descripción de la categoría

    // Constructores
    public Producto() {
    }

    public Producto(int idProducto, int idCategoria, String nombre, String descripcion, BigDecimal precioUnitario, int stockDisponible) {
        this.idProducto = idProducto;
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.stockDisponible = stockDisponible;
    }

    public Producto(int idProducto, int idCategoria, String nombre, String descripcion, BigDecimal precioUnitario, int stockDisponible, String categoriaNombre) {
        this.idProducto = idProducto;
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.stockDisponible = stockDisponible;
        this.categoriaNombre = categoriaNombre;
    }

    // Getters y Setters
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // CAMBIO CLAVE: Getter y Setter para BigDecimal
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getStock() {
        return stockDisponible;
    }

    public void setStock(int stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    @Override
    public String toString() {
        return "Producto{"
                + "idProducto=" + idProducto
                + ", idCategoria=" + idCategoria
                + ", nombre='" + nombre + '\''
                + ", descripcion='" + descripcion + '\''
                + ", precioUnitario=" + precioUnitario
                + ", stockDisponible=" + stockDisponible
                + ", categoriaNombre='" + categoriaNombre + '\''
                + '}';
    }
}
