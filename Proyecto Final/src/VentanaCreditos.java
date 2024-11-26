import javax.swing.*;
import java.awt.*;

public class VentanaCreditos extends JDialog {
    public VentanaCreditos(JFrame parent) {
        super(parent, "Créditos", true);
        setSize(1200, 800);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Agregar imagen al panel superior
        JLabel imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/Panamerican_University_color_logo.jpg"));

        // Redimensionar la imagen
        Image scaledImage = imageIcon.getImage().getScaledInstance(200, 100, Image.SCALE_SMOOTH); // Ajustar tamaño
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        imageLabel.setIcon(scaledIcon);

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(imageLabel, BorderLayout.NORTH);

        // Añadir el panel principal al diálogo
        add(mainPanel);
   

        // Panel de información
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel[] labels = {
                createLabel("Universidad Panamericana", true),
                createLabel("Fundamentos de Programación en Paralelo", true),
                createLabel("Dr. Juan Carlos López Pimentel", false),
                createLabel("Fecha: " + java.time.LocalDate.now().toString(), false),
                createLabel(" ", false),
                createLabel("Estudiantes:", true),
                createLabel("• Gabriel Torres Zacarias", false),
                createLabel("• Gabriel Zaid Gutiérrez", false),
                createLabel("• Sebastian Avilez Hernandez", false)
        };

        for (JLabel label : labels) {
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(label);
            infoPanel.add(Box.createVerticalStrut(10));
        }

        mainPanel.add(infoPanel, BorderLayout.CENTER);

        // Botón de aceptar
        JButton okButton = new JButton("Aceptar");
        okButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JLabel createLabel(String text, boolean isTitle) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", isTitle ? Font.BOLD : Font.PLAIN, isTitle ? 16 : 14));
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaCreditos(null));
    }
}
