package modelo;
/**
 *
 * @author Muaro
 */
public class Sexo {
    private int idSexo;
    private String descripcion;

    public Sexo(){}
    
    public Sexo(int idSexo, String descripcion) {
        this.idSexo = idSexo;
        this.descripcion = descripcion;
    }

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
}