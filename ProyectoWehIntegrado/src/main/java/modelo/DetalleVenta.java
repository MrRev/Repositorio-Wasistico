package modelo;

import java.io.Serializable;
import java.math.BigDecimal; // Importar BigDecimal

/**
 * Modelo que representa un Detalle de Venta en la base de datos.
 *
 * @author Muaro
 */
public class DetalleVenta implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idDetalleVenta;
    private int idVenta;
    private int idProducto;
    private int cantidad;
    private BigDecimal precioUnitario; // Cambiado a BigDecimal
    private BigDecimal subtotal;       // Cambiado a BigDecimal

    public DetalleVenta() {
    }

    public DetalleVenta(int idDetalleVenta, int idVenta, int idProducto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.idDetalleVenta = idDetalleVenta;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    // Getters y Setters
    public int getIdDetalleVenta() {
        return idDetalleVenta;
    }

    public void setIdDetalleVenta(int idDetalleVenta) {
        this.idDetalleVenta = idDetalleVenta;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "DetalleVenta{"
                + "idDetalleVenta=" + idDetalleVenta
                + ", idVenta=" + idVenta
                + ", idProducto=" + idProducto
                + ", cantidad=" + cantidad
                + ", precioUnitario=" + precioUnitario
                + ", subtotal=" + subtotal
                + '}';
    }
}
