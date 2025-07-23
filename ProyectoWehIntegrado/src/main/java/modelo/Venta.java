package modelo;

import java.util.Date;

/**
 *
 * @author Muaro
 */
public class Venta {
    private int idVenta;
    private int idCliente;
    private int idUsuario;
    private Date fechaVenta;
    private Date fechaCreacion;
    private double total;

    public Venta() {}

    public Venta(int idVenta, int idCliente, int idUsuario, Date fechaVenta, Date fechaCreacion, double total) {
        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.fechaVenta = fechaVenta;
        this.fechaCreacion = fechaCreacion;
        this.total = total;
    }

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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}