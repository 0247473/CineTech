import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaEstadosAcomodadores {
    private JTable table;
    private DefaultTableModel tableModel;

    public VentanaEstadosAcomodadores(List<Acomodador> acomodadores) {
        JFrame frame = new JFrame("Estados de Acomodadores");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = { "Acomodador", "Estado" };
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        new Timer(1000, e -> actualizar(acomodadores)).start();
        frame.setVisible(true);
    }

    private void actualizar(List<Acomodador> acomodadores) {
        tableModel.setRowCount(0);
        for (Acomodador acomodador : acomodadores) {
            tableModel.addRow(new Object[] {
                    "Acomodador",
                    acomodador.getEstado()
            });
        }
    }
}