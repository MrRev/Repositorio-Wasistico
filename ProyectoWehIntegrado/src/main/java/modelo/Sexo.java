package modelo;

import java.io.Serializable;

/**
 * Clase de modelo para representar el Sexo.
 */
public class Sexo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idSexo;
    private String descripcion;

    public Sexo() {
    }

    public Sexo(int idSexo, String descripcion) {
        this.idSexo = idSexo;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdSexo() {
        return idSexo;
    }

    public void setIdSexo(int idSexo) {
        this.idSexo = idSexo;
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
