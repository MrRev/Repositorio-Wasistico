package modelo;

/**
 * 
 * @author Muaro
 */
public class Categoria {
    private int idCategoria;
    private String descripcion;

    // Constructor vacío
    public Categoria() {}

    // Constructor con parámetros
    public Categoria(int idCategoria, String descripcion) {
        this.idCategoria = idCategoria;
        this.descripcion = descripcion;
    }

    // Getter para idCategoria
    public int getIdCategoria() {
        return idCategoria;
    }

    // Setter para idCategoria
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    // Getter para descripcion
    public String getDescripcion() {
        return descripcion;
    }

    // Setter para descripcion
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}