package beans;

import dao.DashboardDAO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.title.Title;

@Named("dashboardBean")
@RequestScoped
public class DashboardBean {

    // Logger para registrar eventos y errores
    private static final Logger LOGGER = Logger.getLogger(DashboardBean.class.getName());

    // Inyección del DAO para acceso a datos
    @Inject
    private DashboardDAO dashboardDAO;

    // Modelos de gráficos para PrimeFaces Charts
    private BarChartModel barModelVentasCategoria;
    private LineChartModel lineModelVentasMensuales;
    private BarChartModel barModelProductosMasVendidos;

    // Variables para los contadores y totales del dashboard
    private int totalProductos, totalClientes, totalCategorias, totalVentas, totalUsuarios;
    private double totalIngresos;

    // Colores predefinidos para los gráficos para consistencia
    private static final List<String> BAR_COLORS_VENTAS_CATEGORIA = Arrays.asList(
            "rgba(54, 162, 235, 0.6)", // Azul
            "rgba(75, 192, 192, 0.6)", // Verde Azulado
            "rgba(153, 102, 255, 0.6)", // Púrpura
            "rgba(255, 159, 64, 0.6)", // Naranja
            "rgba(255, 99, 132, 0.6)" // Rojo
    );
    private static final String LINE_COLOR_VENTAS_MENSUALES = "rgba(255, 99, 132, 1)"; // Rojo
    private static final List<String> BAR_COLORS_PRODUCTOS_VENDIDOS = Arrays.asList(
            "rgba(255, 206, 86, 0.6)", // Amarillo
            "rgba(75, 192, 192, 0.6)", // Verde Azulado
            "rgba(153, 102, 255, 0.6)", // Púrpura
            "rgba(54, 162, 235, 0.6)", // Azul
            "rgba(255, 99, 132, 0.6)" // Rojo
    );


    /**
     * Método que se ejecuta después de la construcción del bean.
     * Carga todos los datos del dashboard y prepara los modelos de gráficos.
     */
    @PostConstruct
    public void init() {
        // Carga los contadores y totales
        cargarContadoresYTotales();

        // Crea los modelos de gráficos
        crearGraficoVentasPorCategoria();
        crearGraficoVentasMensuales();
        crearGraficoProductosMasVendidos();
    }

    /**
     * Carga los contadores y totales del dashboard utilizando el DAO.
     * Incluye manejo de errores básico para cada llamada al DAO.
     */
    private void cargarContadoresYTotales() {
        try {
            totalProductos = dashboardDAO.contarProductos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar productos: " + e.getMessage(), e);
            totalProductos = 0; // Valor por defecto en caso de error
        }

        try {
            totalClientes = dashboardDAO.contarClientes();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar clientes: " + e.getMessage(), e);
            totalClientes = 0;
        }

        try {
            totalCategorias = dashboardDAO.contarCategorias();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar categorías: " + e.getMessage(), e);
            totalCategorias = 0;
        }

        try {
            totalVentas = dashboardDAO.contarVentas();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar ventas: " + e.getMessage(), e);
            totalVentas = 0;
        }

        try {
            totalIngresos = dashboardDAO.obtenerTotalIngresos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener total de ingresos: " + e.getMessage(), e);
            totalIngresos = 0.0;
        }

        try {
            totalUsuarios = dashboardDAO.contarUsuarios();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar usuarios: " + e.getMessage(), e);
            totalUsuarios = 0;
        }
    }

    /**
     * Crea el modelo de gráfico de barras para "Ventas por Categoría".
     */
    private void crearGraficoVentasPorCategoria() {
        barModelVentasCategoria = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet ds = new BarChartDataSet();
        Map<String, Integer> datos = dashboardDAO.obtenerVentasPorCategoria();
        List<Number> valores = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();

        // Itera sobre el mapa de datos para poblar las listas de valores y etiquetas
        datos.forEach((k, v) -> {
            etiquetas.add(k);
            valores.add(v);
        });

        // FIX: Crear una nueva ArrayList<Number> para forzar la inferencia de tipo
        ds.setData(new ArrayList<>(valores));
        ds.setLabel("Cantidad de Ítems Vendidos por Categoría"); // Etiqueta más descriptiva
        // Asigna colores dinámicamente o usa una lista predefinida
        List<String> backgroundColors = new ArrayList<>();
        for (int i = 0; i < valores.size(); i++) {
            backgroundColors.add(BAR_COLORS_VENTAS_CATEGORIA.get(i % BAR_COLORS_VENTAS_CATEGORIA.size()));
        }
        ds.setBackgroundColor(backgroundColors); // Asigna una lista de colores

        data.addChartDataSet(ds);
        data.setLabels(etiquetas);
        barModelVentasCategoria.setData(data);

        // Configuración de opciones del gráfico
        BarChartOptions opts = new BarChartOptions();
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes y = new CartesianLinearAxes();
        y.setBeginAtZero(true); // El eje Y siempre empieza en cero
        scales.addYAxesData(y);
        opts.setScales(scales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Ventas por Categoría");
        opts.setTitle(title);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        opts.setLegend(legend);

        barModelVentasCategoria.setOptions(opts);
    }

    /**
     * Crea el modelo de gráfico de líneas para "Ventas Mensuales".
     */
    private void crearGraficoVentasMensuales() {
        lineModelVentasMensuales = new LineChartModel();
        ChartData data = new ChartData();

        LineChartDataSet ds = new LineChartDataSet();
        Map<String, Double> datos = dashboardDAO.obtenerVentasMensuales();
        List<Number> valores = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        datos.forEach((k, v) -> {
            etiquetas.add(k);
            valores.add(v);
        });
        // FIX: Crear una nueva ArrayList<Number> para forzar la inferencia de tipo
        ds.setData(new ArrayList<>(valores));
        ds.setLabel("Total de Ventas Mensuales");
        ds.setBorderColor(LINE_COLOR_VENTAS_MENSUALES);
        ds.setFill(false); // Para una línea sin área sombreada

        data.addChartDataSet(ds);
        data.setLabels(etiquetas);
        lineModelVentasMensuales.setData(data);

        // Configuración de opciones del gráfico
        LineChartOptions opts = new LineChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Tendencia de Ventas Mensuales");
        opts.setTitle(title);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        opts.setLegend(legend);

        lineModelVentasMensuales.setOptions(opts);
    }

    /**
     * Crea el modelo de gráfico de barras para "Productos Más Vendidos".
     */
    private void crearGraficoProductosMasVendidos() {
        barModelProductosMasVendidos = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet ds = new BarChartDataSet();
        Map<String, Integer> datos = dashboardDAO.obtenerProductosMasVendidos();
        List<Number> valores = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        datos.forEach((k, v) -> {
            etiquetas.add(k);
            valores.add(v);
        });

        // FIX: Crear una nueva ArrayList<Number> para forzar la inferencia de tipo
        ds.setData(new ArrayList<>(valores));
        ds.setLabel("Cantidad Vendida");
        // Asigna colores dinámicamente o usa una lista predefinida
        List<String> backgroundColors = new ArrayList<>();
        for (int i = 0; i < valores.size(); i++) {
            backgroundColors.add(BAR_COLORS_PRODUCTOS_VENDIDOS.get(i % BAR_COLORS_PRODUCTOS_VENDIDOS.size()));
        }
        ds.setBackgroundColor(backgroundColors);

        data.addChartDataSet(ds);
        data.setLabels(etiquetas);
        barModelProductosMasVendidos.setData(data);

        // Configuración de opciones del gráfico
        BarChartOptions opts = new BarChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Top 5 Productos Más Vendidos");
        opts.setTitle(title);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        opts.setLegend(legend);

        barModelProductosMasVendidos.setOptions(opts);
    }

    // --- Getters para acceder a los datos desde la vista JSF ---
    public BarChartModel getBarModelVentasCategoria() {
        return barModelVentasCategoria;
    }

    public LineChartModel getLineModelVentasMensuales() {
        return lineModelVentasMensuales;
    }

    public BarChartModel getBarModelProductosMasVendidos() {
        return barModelProductosMasVendidos;
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public int getTotalClientes() {
        return totalClientes;
    }

    public int getTotalCategorias() {
        return totalCategorias;
    }

    public int getTotalVentas() {
        return totalVentas;
    }

    public double getTotalIngresos() {
        return totalIngresos;
    }

    public int getTotalUsuarios() {
        return totalUsuarios;
    }
}
