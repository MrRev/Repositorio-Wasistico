package modelo;

import java.io.Serializable;

/**
 * Clase para representar un cliente. Implementa Serializable para permitir su
 * uso en ámbitos de sesión/vista en JSF.
 */
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idCliente;
    private String dni;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String direccion;
    private int idSexo;
    private String sexoDescripcion; // Atributo para almacenar la descripción del sexo para la UI

    public Cliente() {
    }

    /**
     * Constructor para crear un nuevo cliente, sin idCliente (ya que será
     * autogenerado por la DB).
     *
     * @param dni El DNI del cliente.
     * @param nombre El nombre del cliente.
     * @param apellido El apellido del cliente.
     * @param correo El correo electrónico del cliente.
     * @param telefono El número de teléfono del cliente.
     * @param direccion La dirección del cliente.
     * @param idSexo El ID del sexo del cliente.
     */
    public Cliente(String dni, String nombre, String apellido,
            String correo, String telefono,
            String direccion, int idSexo) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.idSexo = idSexo;
    }

    /**
     * Constructor completo con idCliente (por ejemplo, al recuperar de la DB o
     * al editar).
     *
     * @param idCliente El ID único del cliente.
     * @param dni El DNI del cliente.
     * @param nombre El nombre del cliente.
     * @param apellido El apellido del cliente.
     * @param correo El correo electrónico del cliente.
     * @param telefono El número de teléfono del cliente.
     * @param direccion La dirección del cliente.
     * @param idSexo El ID del sexo del cliente.
     */
    public Cliente(int idCliente, String dni, String nombre, String apellido,
            String correo, String telefono, String direccion, int idSexo) {
        this(dni, nombre, apellido, correo, telefono, direccion, idSexo); // Llama al constructor sin idCliente
        this.idCliente = idCliente;
    }

    /**
     * Constructor completo con idCliente y sexoDescripcion. Útil para mapear
     * resultados de consultas que hacen JOIN con la tabla de Sexos.
     *
     * @param idCliente El ID único del cliente.
     * @param dni El DNI del cliente.
     * @param nombre El nombre del cliente.
     * @param apellido El apellido del cliente.
     * @param correo El correo electrónico del cliente.
     * @param telefono El número de teléfono del cliente.
     * @param direccion La dirección del cliente.
     * @param idSexo El ID del sexo del cliente.
     * @param sexoDescripcion La descripción textual del sexo del cliente.
     */
    public Cliente(int idCliente, String dni, String nombre, String apellido,
            String correo, String telefono, String direccion, int idSexo, String sexoDescripcion) {
        this(idCliente, dni, nombre, apellido, correo, telefono, direccion, idSexo);
        this.sexoDescripcion = sexoDescripcion;
    }

    /**
     * Constructor de copia. Crea un nuevo objeto Cliente copiando los valores
     * de otro objeto Cliente. Útil para operaciones de edición donde se desea
     * una copia mutable.
     *
     * @param src El objeto Cliente fuente del cual copiar los valores.
     */
    public Cliente(Cliente src) {
        this.idCliente = src.idCliente;
        this.dni = src.dni;
        this.nombre = src.nombre;
        this.apellido = src.apellido;
        this.correo = src.correo;
        this.telefono = src.telefono;
        this.direccion = src.direccion;
        this.idSexo = src.idSexo;
        this.sexoDescripcion = src.sexoDescripcion; // Copia también la descripción del sexo
    }

    // GETTERS y SETTERS
    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getIdSexo() {
        return idSexo;
    }

    public void setIdSexo(int idSexo) {
        this.idSexo = idSexo;
    }

    public String getSexoDescripcion() {
        return sexoDescripcion;
    }

    public void setSexoDescripcion(String sexoDescripcion) {
        this.sexoDescripcion = sexoDescripcion;
    }
}
