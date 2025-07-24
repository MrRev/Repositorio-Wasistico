package beans;

import dao.DashboardDAO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named("dashboardBean")
@RequestScoped
public class DashboardBean {

    private int totalProductos;
    private int totalClientes;
    private int totalCategorias;
    private int totalVentas;
    private double totalIngresos;

    private DashboardDAO dao = new DashboardDAO();

    @PostConstruct
    public void init() {
        totalProductos = dao.contarProductos();
        totalClientes = dao.contarClientes();
        totalCategorias = dao.contarCategorias();
        totalVentas = dao.contarVentas();
        totalIngresos = dao.obtenerTotalIngresos();
    }

    public int getTotalProductos() { return totalProductos; }
    public int getTotalClientes() { return totalClientes; }
    public int getTotalCategorias() { return totalCategorias; }
    public int getTotalVentas() { return totalVentas; }
    public double getTotalIngresos() { return totalIngresos; }

}
