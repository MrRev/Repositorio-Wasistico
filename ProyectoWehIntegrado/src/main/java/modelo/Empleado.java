package modelo;

import java.io.Serializable;

/**
 * Clase para representar un empleado (usuario del sistema). Implementa
 * Serializable para ser compatible con JSF y CDI en diferentes ámbitos.
 */
public class Empleado implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idUsuario;
    private String dni;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String direccion;
    private String contrasena; // NOTA IMPORTANTE: En una aplicación real, esto DEBERÍA ser un hash (ej. BCrypt) por seguridad.
    private int idSexo;
    private int idRol;
    private String rolDescripcion; // Campo para almacenar la descripción del rol (obtenido de la tabla Roles)

    /**
     * Constructor vacío necesario para JSF/CDI.
     */
    public Empleado() {
    }

    /**
     * Constructor para crear un nuevo empleado, sin idUsuario (ya que será
     * autogenerado por la DB).
     *
     * @param dni El DNI del empleado.
     * @param nombre El nombre del empleado.
     * @param apellido El apellido del empleado.
     * @param correo El correo electrónico del empleado.
     * @param telefono El número de teléfono del empleado.
     * @param direccion La dirección del empleado.
     * @param contrasena La contraseña del empleado (considerar hashing en
     * producción).
     * @param idSexo El ID del sexo del empleado.
     * @param idRol El ID del rol del empleado.
     */
    public Empleado(String dni, String nombre, String apellido,
            String correo, String telefono,
            String direccion, String contrasena,
            int idSexo, int idRol) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.contrasena = contrasena;
        this.idSexo = idSexo;
        this.idRol = idRol;
    }

    /**
     * Constructor cuando ya tienes idUsuario (por ejemplo, al recuperar de la
     * DB o al editar). Utiliza el constructor anterior para inicializar los
     * campos comunes.
     *
     * @param idUsuario El ID único del empleado.
     * @param dni El DNI del empleado.
     * @param nombre El nombre del empleado.
     * @param apellido El apellido del empleado.
     * @param correo El correo electrónico del empleado.
     * @param telefono El número de teléfono del empleado.
     * @param direccion La dirección del empleado.
     * @param contrasena La contraseña del empleado (considerar hashing en
     * producción).
     * @param idSexo El ID del sexo del empleado.
     * @param idRol El ID del rol del empleado.
     */
    public Empleado(int idUsuario, String dni, String nombre,
            String apellido, String correo, String telefono,
            String direccion, String contrasena,
            int idSexo, int idRol) {
        this(dni, nombre, apellido, correo, telefono, direccion, contrasena, idSexo, idRol);
        this.idUsuario = idUsuario;
    }

    /**
     * Constructor completo con descripción del rol incluida. Útil para mapear
     * resultados de consultas que hacen JOIN con la tabla de Roles.
     *
     * @param idUsuario El ID único del empleado.
     * @param dni El DNI del empleado.
     * @param nombre El nombre del empleado.
     * @param apellido El apellido del empleado.
     * @param correo El correo electrónico del empleado.
     * @param telefono El número de teléfono del empleado.
     * @param direccion La dirección del empleado.
     * @param contrasena La contraseña del empleado (considerar hashing en
     * producción).
     * @param idSexo El ID del sexo del empleado.
     * @param idRol El ID del rol del empleado.
     * @param rolDescripcion La descripción textual del rol del empleado.
     */
    public Empleado(int idUsuario, String dni, String nombre,
            String apellido, String correo, String telefono,
            String direccion, String contrasena,
            int idSexo, int idRol, String rolDescripcion) {
        this(idUsuario, dni, nombre, apellido, correo, telefono, direccion, contrasena, idSexo, idRol);
        this.rolDescripcion = rolDescripcion;
    }

    /**
     * Crea un nuevo objeto Empleado copiando los valores de otro objeto
     * Empleado. Útil para operaciones de edición donde se desea una copia
     * mutable.
     *
     * @param src El objeto Empleado fuente del cual copiar los valores.
     */
    public Empleado(Empleado src) {
        this.idUsuario = src.idUsuario;
        this.dni = src.dni;
        this.nombre = src.nombre;
        this.apellido = src.apellido;
        this.correo = src.correo;
        this.telefono = src.telefono;
        this.direccion = src.direccion;
        this.contrasena = src.contrasena;
        this.idSexo = src.idSexo;
        this.idRol = src.idRol;
        this.rolDescripcion = src.rolDescripcion;
    }

    // GETTERS y SETTERS — permiten obtener o fijar cada campo
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public int getIdSexo() {
        return idSexo;
    }

    public void setIdSexo(int idSexo) {
        this.idSexo = idSexo;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getRolDescripcion() {
        return rolDescripcion;
    }

    public void setRolDescripcion(String rolDescripcion) {
        this.rolDescripcion = rolDescripcion;
    }
}
