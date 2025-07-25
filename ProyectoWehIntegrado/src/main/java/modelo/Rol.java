package modelo;

import java.io.Serializable;

/**
 * Clase de modelo para representar un Rol de usuario.
 */
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idRol;
    private String descripcion;

    public Rol() {
    }

    public Rol(int idRol, String descripcion) {
        this.idRol = idRol;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
