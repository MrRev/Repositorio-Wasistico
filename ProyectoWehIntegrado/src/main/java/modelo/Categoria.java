package modelo;

import java.io.Serializable;

/**
 * Modelo que representa una Categoría de producto en la base de datos.
 *
 * @author Muaro
 */
public class Categoria implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idCategoria;
    private String descripcion;

    public Categoria() {
    }

    public Categoria(int idCategoria, String descripcion) {
        this.idCategoria = idCategoria;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Categoria{"
                + "idCategoria=" + idCategoria
                + ", descripcion='" + descripcion + '\''
                + '}';
    }
}
