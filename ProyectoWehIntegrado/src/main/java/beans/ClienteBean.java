package beans;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import dao.ClienteDAO;
import modelo.Cliente;

@Named("clienteBean")
@ViewScoped
public class ClienteBean implements Serializable {

    private Cliente cliente = new Cliente();
    private Cliente clienteSeleccionado = new Cliente();
    private List<Cliente> listaClientes;

    public ClienteBean() {
        cargarClientes();
    }

    public void cargarClientes() {
        ClienteDAO dao = new ClienteDAO();
        listaClientes = dao.listarClientes();
    }

    public void agregarCliente() {
        // cuando tengas en el futuro un método en el DAO, puedes llamarlo acá
        listaClientes.add(cliente); // solo simula agregar
        cliente = new Cliente();
    }

    public void prepararEditar(Cliente cli) {
        if (cli != null) {
            this.clienteSeleccionado = new Cliente(
                cli.getIdCliente(),
                cli.getDni(),
                cli.getNombre(),
                cli.getApellido(),
                cli.getCorreo(),
                cli.getTelefono(),
                cli.getDireccion(),
                cli.getIdSexo()
            );
        }
    }

    public void modificarCliente() {
        if (clienteSeleccionado != null) {
            // simular persistencia real
            for (Cliente c : listaClientes) {
                if (c.getIdCliente() == clienteSeleccionado.getIdCliente()) {
                    c.setNombre(clienteSeleccionado.getNombre());
                    c.setApellido(clienteSeleccionado.getApellido());
                    c.setCorreo(clienteSeleccionado.getCorreo());
                    c.setDireccion(clienteSeleccionado.getDireccion());
                    c.setDni(clienteSeleccionado.getDni());
                    c.setIdSexo(clienteSeleccionado.getIdSexo());
                    c.setTelefono(clienteSeleccionado.getTelefono());
                    break;
                }
            }
            clienteSeleccionado = new Cliente();
        }
    }

    public void prepararEliminar(Cliente cli) {
        this.clienteSeleccionado = cli;
    }

    public void eliminarCliente() {
        if (clienteSeleccionado != null) {
            listaClientes.removeIf(c -> c.getIdCliente() == clienteSeleccionado.getIdCliente());
            clienteSeleccionado = new Cliente();
        }
    }

    // getters y setters
    public List<Cliente> getListaClientes() {
        return listaClientes;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Cliente getClienteSeleccionado() {
        return clienteSeleccionado;
    }

    public void setClienteSeleccionado(Cliente clienteSeleccionado) {
        this.clienteSeleccionado = clienteSeleccionado;
    }
}
