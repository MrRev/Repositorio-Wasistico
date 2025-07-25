package modelo;

import java.io.Serializable;

/**
 * Clase de modelo para representar el Sexo. Implementa Serializable para
 * permitir su uso en ámbitos de sesión/vista en JSF.
 */
public class Sexo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idSexo;
    private String descripcion;

    /**
     * Constructor vacío necesario para JSF/CDI.
     */
    public Sexo() {
    }

    /**
     * Constructor para inicializar un objeto Sexo con su ID y descripción.
     *
     * @param idSexo El ID único del sexo.
     * @param descripcion La descripción textual del sexo (ej. "Masculino",
     * "Femenino").
     */
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

    /**
     * Sobrescribe el método toString() para devolver la descripción del sexo.
     * Esto es útil para mostrar la descripción directamente en componentes de
     * UI.
     *
     * @return La descripción del sexo.
     */
    @Override
    public String toString() {
        return descripcion;
    }
}
