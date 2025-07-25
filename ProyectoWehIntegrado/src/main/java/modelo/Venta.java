package modelo;

import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal; // Importar BigDecimal

/**
 * Modelo que representa una Venta en la base de datos.
 *
 * @author Muaro
 */
public class Venta implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idVenta;
    private int idCliente;
    private int idUsuario;
    private Date fechaVenta;
    private Date fechaCreacion;
    private BigDecimal total; // Cambiado a BigDecimal

    public Venta() {
    }

    public Venta(int idVenta, int idCliente, int idUsuario, Date fechaVenta, Date fechaCreacion, BigDecimal total) {
        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.fechaVenta = fechaVenta;
        this.fechaCreacion = fechaCreacion;
        this.total = total;
    }

    // Getters y Setters
    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Date getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(Date fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Venta{"
                + "idVenta=" + idVenta
                + ", idCliente=" + idCliente
                + ", idUsuario=" + idUsuario
                + ", fechaVenta=" + fechaVenta
                + ", fechaCreacion=" + fechaCreacion
                + ", total=" + total
                + '}';
    }
}
