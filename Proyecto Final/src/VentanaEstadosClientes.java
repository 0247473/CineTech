import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaEstadosClientes {
    private final JTable table;
    private final DefaultTableModel tableModel;

    public VentanaEstadosClientes(List<Cliente> clientes) {
        JFrame frame = new JFrame("Estados de Clientes");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = { "Cliente", "Estado" };
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        new Timer(1000, e -> actualizar(clientes)).start();
        frame.setVisible(true);
    }

    private void actualizar(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        for (Cliente cliente : clientes) {
            tableModel.addRow(new Object[] {
                    cliente.getClienteId(),
                    cliente.getEstado()
            });
        }
    }
}