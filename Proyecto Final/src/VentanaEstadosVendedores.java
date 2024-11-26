import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaEstadosVendedores {
    private JTable table;
    private DefaultTableModel tableModel;

    public VentanaEstadosVendedores(List<VendedorTaquilla> taquilleros, List<VendedorDulceria> dulceros) {
        JFrame frame = new JFrame("Estados de Vendedores");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = { "Vendedor", "Estado" };
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        new Timer(1000, e -> actualizar(taquilleros, dulceros)).start();
        frame.setVisible(true);
    }

    private void actualizar(List<VendedorTaquilla> taquilleros, List<VendedorDulceria> dulceros) {
        tableModel.setRowCount(0);
        for (VendedorTaquilla taquillero : taquilleros) {
            tableModel.addRow(new Object[] {
                    "Taquillero",
                    taquillero.getEstado()
            });
        }
        for (VendedorDulceria dulcero : dulceros) {
            tableModel.addRow(new Object[] {
                    "Dulcero",
                    dulcero.getEstado()
            });
        }
    }
}
