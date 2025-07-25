package modelo;

import java.io.Serializable;

/**
 * Clase de modelo para representar un Empleado (Usuario del sistema).
 * Implementa Serializable para ser compatible con JSF y CDI en diferentes ámbitos.
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
    private String contrasena; // En una aplicación real, esto debería ser un hash
    private int idSexo;
    private int idRol;
    private String rolDescripcion; // Nuevo campo para almacenar la descripción del rol

    // Constructor por defecto (necesario para JSF/CDI)
    public Empleado() {
    }

    // Constructor completo (sin idUsuario para nuevas inserciones)
    public Empleado(String dni, String nombre, String apellido, String correo, String telefono, String direccion, String contrasena, int idSexo, int idRol) {
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

    // Constructor completo (con idUsuario para recuperación o actualización)
    public Empleado(int idUsuario, String dni, String nombre, String apellido, String correo, String telefono, String direccion, String contrasena, int idSexo, int idRol) {
        this.idUsuario = idUsuario;
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

    // Constructor completo con rolDescripcion (para mapeo desde la DB)
    public Empleado(int idUsuario, String dni, String nombre, String apellido, String correo, String telefono, String direccion, String contrasena, int idSexo, int idRol, String rolDescripcion) {
        this.idUsuario = idUsuario;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.contrasena = contrasena;
        this.idSexo = idSexo;
        this.idRol = idRol;
        this.rolDescripcion = rolDescripcion;
    }

    // --- Getters y Setters ---
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
