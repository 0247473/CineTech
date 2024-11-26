import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VentanaEstadoHilos {
    private JTextArea threadStateArea;

    public VentanaEstadoHilos(List<Thread> allThreads) {
        JFrame frame = new JFrame("Estado de los Hilos");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        threadStateArea = new JTextArea();
        threadStateArea.setEditable(false);
        frame.add(new JScrollPane(threadStateArea), BorderLayout.CENTER);

        // Actualización periódica
        new Timer(1000, e -> actualizarEstadoHilos(allThreads)).start();

        frame.setVisible(true);
    }

    private void actualizarEstadoHilos(List<Thread> allThreads) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Estados de los Hilos ===\n");

        // Recorrer todos los hilos y mostrar su estado
        for (Thread thread : allThreads) {
            sb.append("Hilo: ").append(thread.getName())
              .append(" - Estado: ").append(thread.getState()).append("\n");
        }

        threadStateArea.setText(sb.toString());
    }
}
