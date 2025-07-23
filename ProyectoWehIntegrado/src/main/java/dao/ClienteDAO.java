package dao;

import modelo.Cliente;
import conf.Conexion;
import java.sql.*;
import java.util.*;

public class ClienteDAO {

    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Clientes";
        
        try (
            Connection con = Conexion.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                Cliente cli = new Cliente();
                cli.setIdCliente(rs.getInt("idCliente"));
                cli.setDni(rs.getString("dni"));
                cli.setNombre(rs.getString("nombre"));
                cli.setApellido(rs.getString("apellido"));
                cli.setCorreo(rs.getString("correo"));
                cli.setTelefono(rs.getString("telefono"));
                cli.setDireccion(rs.getString("dirección"));
                cli.setIdSexo(rs.getInt("idSexo"));
                lista.add(cli);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // (en el futuro agregar métodos agregar, modificar, eliminar)
}
