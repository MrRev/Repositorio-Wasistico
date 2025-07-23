package conf;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Conexion {
    private static final String URL = "jdbc:mysql://localhost:3306/bodegadonpablo";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConexion() throws SQLException{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }catch (ClassNotFoundException e){
            throw new SQLException("Error al cargar el driver MySQL: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        try (Connection conn = Conexion.getConexion()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexion exitosa a la base de datos.");
            } else {
                System.out.println("No se pudo establecer la conexión.");
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }
}