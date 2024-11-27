import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class VentanaMonitoreoUnificada {
    private JFrame frame;
    private DefaultTableModel clientesModel;
    private DefaultTableModel vendedoresModel;
    private DefaultTableModel acomodadoresModel;
    private DefaultTableModel recursosModel;
    private DefaultTableModel hilosModel;

    public VentanaMonitoreoUnificada(RecursosCine recursos, List<Cliente> clientes,
            List<VendedorTaquilla> taquilleros, List<VendedorDulceria> dulceros,
            List<Acomodador> acomodadores, List<Thread> allThreads) {
        frame = new JFrame("Monitoreo del Cine");
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Clientes", createClientesPanel());
        tabbedPane.addTab("Vendedores", createVendedoresPanel());
        tabbedPane.addTab("Acomodadores", createAcomodadoresPanel());
        tabbedPane.addTab("Recursos", createRecursosPanel());
        tabbedPane.addTab("Hilos", createHilosPanel());

        frame.add(tabbedPane);

        Timer timer = new Timer(1000,
                e -> actualizarTodo(recursos, clientes, taquilleros, dulceros, acomodadores, allThreads));
        timer.start();

        frame.setVisible(true);
    }

    private JPanel createClientesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = { "ID Cliente", "Estado" };
        clientesModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(clientesModel);
        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createVendedoresPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = { "Tipo", "ID", "Estado" };
        vendedoresModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(vendedoresModel);
        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createAcomodadoresPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = { "ID", "Estado" };
        acomodadoresModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(acomodadoresModel);
        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createRecursosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = { "Recurso", "Ocupación", "Clientes en Espera" };
        recursosModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(recursosModel);
        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createHilosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = { "Nombre", "Estado" };
        hilosModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(hilosModel);
        panel.add(new JScrollPane(table));
        return panel;
    }

    private void actualizarClientes(List<Cliente> clientes) {
        clientesModel.setRowCount(0);

        for (Cliente cliente : clientes) {
            clientesModel.addRow(new Object[] {
                    cliente.getClienteId(),
                    cliente.getEstado()
            });
        }
    }

    private void actualizarVendedores(List<VendedorTaquilla> taquilleros, List<VendedorDulceria> dulceros) {
        vendedoresModel.setRowCount(0);

        for (VendedorTaquilla taquillero : taquilleros) {
            vendedoresModel.addRow(new Object[] {
                    "Taquillero",
                    taquillero.getId(),
                    taquillero.getEstado()
            });
        }

        for (VendedorDulceria dulcero : dulceros) {
            vendedoresModel.addRow(new Object[] {
                    "Dulcero",
                    dulcero.getId(),
                    dulcero.getEstado()
            });
        }
    }

    private void actualizarAcomodadores(List<Acomodador> acomodadores) {
        acomodadoresModel.setRowCount(0);

        for (Acomodador acomodador : acomodadores) {
            acomodadoresModel.addRow(new Object[] {
                    acomodador.getId(),
                    acomodador.getEstado()
            });
        }
    }

    private void actualizarRecursos(RecursosCine recursos) {
        recursosModel.setRowCount(0);
        recursosModel.addRow(new Object[] {
                "Cola Taquilla",
                String.format("%d/%d", recursos.getBufferOccupancyTaquilla(), recursos.getBufferSizeTaquilla()),
                getClientesEnEspera(recursos.getColaTaquilla())
        });
        recursosModel.addRow(new Object[] {
                "Cola Dulcería",
                String.format("%d/%d", recursos.getBufferOccupancyDulceria(), recursos.getBufferSizeDulceria()),
                getClientesEnEspera(recursos.getColaDulceria())
        });
        recursosModel.addRow(new Object[] {
                "Cola Acomodador",
                String.format("%d/%d", recursos.getBufferOccupancyAcomodador(), recursos.getBufferSizeAcomodador()),
                getClientesEnEspera(recursos.getColaAcomodador())
        });
    }

    private String getClientesEnEspera(BlockingQueue<Cliente> cola) {
        StringBuilder sb = new StringBuilder();
        for (Cliente cliente : cola) {
            sb.append(cliente.getClienteId()).append(" ");
        }
        return sb.toString().trim();
    }

    private void actualizarHilos(List<Thread> allThreads) {
        hilosModel.setRowCount(0);
        for (Thread thread : allThreads) {
            hilosModel.addRow(new Object[] {
                    thread.getName(),
                    thread.getState()
            });
        }
    }

    private void actualizarTodo(RecursosCine recursos, List<Cliente> clientes,
            List<VendedorTaquilla> taquilleros, List<VendedorDulceria> dulceros,
            List<Acomodador> acomodadores, List<Thread> allThreads) {
        SwingUtilities.invokeLater(() -> {
            actualizarClientes(clientes);
            actualizarVendedores(taquilleros, dulceros);
            actualizarAcomodadores(acomodadores);
            actualizarRecursos(recursos);
            actualizarHilos(allThreads);
        });
    }
}