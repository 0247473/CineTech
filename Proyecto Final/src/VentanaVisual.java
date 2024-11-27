import java.awt.*;
import java.util.List;
import javax.swing.*;

public class VentanaVisual extends JFrame {
    private final RecursosCine recursos;
    private final List<Cliente> clientes;
    private final List<VendedorTaquilla> taquilleros;
    private final List<VendedorDulceria> dulceros;
    private final List<Acomodador> acomodadores;
    private final CinePanel cinePanel;
    private Timer timer;

    public VentanaVisual(RecursosCine recursos, List<Cliente> clientes,
            List<VendedorTaquilla> taquilleros, List<VendedorDulceria> dulceros,
            List<Acomodador> acomodadores) {
        this.recursos = recursos;
        this.clientes = clientes;
        this.taquilleros = taquilleros;
        this.dulceros = dulceros;
        this.acomodadores = acomodadores;

        setTitle("Visualización del Cine");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cinePanel = new CinePanel();
        add(cinePanel);

        timer = new Timer(100, e -> {
            cinePanel.repaint();
        });
        timer.start();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class CinePanel extends JPanel {
        private final Color COLOR_FONDO = new Color(240, 240, 240);
        private final Color COLOR_TAQUILLA = new Color(255, 200, 200);
        private final Color COLOR_DULCERIA = new Color(200, 255, 200);
        private final Color COLOR_SALA = new Color(200, 200, 255);
        private final int TAMANO_PERSONA = 20;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            dibujarFondo(g2d);
            dibujarAreas(g2d);
            dibujarTrabajadores(g2d);
            dibujarClientes(g2d);
            dibujarAsientos(g2d);
            dibujarInformacion(g2d);
        }

        private void dibujarFondo(Graphics2D g2d) {
            g2d.setColor(COLOR_FONDO);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        private void dibujarAreas(Graphics2D g2d) {
            g2d.setColor(COLOR_TAQUILLA);
            g2d.fillRect(50, 50, 200, 100);
            g2d.setColor(Color.BLACK);
            g2d.drawString("TAQUILLA", 120, 90);

            g2d.setColor(COLOR_DULCERIA);
            g2d.fillRect(300, 50, 200, 100);
            g2d.setColor(Color.BLACK);
            g2d.drawString("DULCERÍA", 370, 90);

            g2d.setColor(COLOR_SALA);
            g2d.fillRect(550, 50, 400, 400);
            g2d.setColor(Color.BLACK);
            g2d.drawString("SALA DE CINE", 700, 90);

            g2d.setColor(new Color(220, 220, 220));
            g2d.fillRect(300, 200, 100, 100);
            g2d.setColor(Color.BLACK);
            g2d.drawString("BAÑOS", 330, 250);

            g2d.setColor(new Color(150, 150, 150));
            g2d.fillRect(getWidth() - 50, getHeight() - 100, 30, 60);
            g2d.setColor(Color.BLACK);
            g2d.drawString("SALIDA", getWidth() - 60, getHeight() - 20);
        }

        private void dibujarTrabajadores(Graphics2D g2d) {
            int x = 60;
            for (VendedorTaquilla taquillero : taquilleros) {
                if (taquillero.isEstaEnTurno()) {
                    dibujarPersona(g2d, x, 80, Color.RED, "T" + taquillero.getId());
                }
                x += 40;
            }

            x = 310;
            for (VendedorDulceria dulcero : dulceros) {
                if (dulcero.isEstaEnTurno()) {
                    dibujarPersona(g2d, x, 80, Color.GREEN, "D" + dulcero.getId());
                }
                x += 40;
            }

            x = 560;
            for (Acomodador acomodador : acomodadores) {
                if (acomodador.isEstaEnTurno()) {
                    dibujarPersona(g2d, x, 80, Color.BLUE, "A" + acomodador.getId());
                }
                x += 40;
            }
        }

        private void dibujarPersona(Graphics2D g2d, int x, int y, Color color, String id) {
            g2d.setColor(color);
            g2d.fillOval(x, y, TAMANO_PERSONA, TAMANO_PERSONA);
            g2d.setColor(Color.BLACK);
            g2d.drawString(id, x, y + TAMANO_PERSONA + 15);
        }

        private void dibujarClientes(Graphics2D g2d) {
            for (Cliente cliente : clientes) {
                Point posicion = calcularPosicionCliente(cliente);
                dibujarPersona(g2d, posicion.x, posicion.y, Color.ORANGE, "C" + cliente.getClienteId());
            }
        }

        private Point calcularPosicionCliente(Cliente cliente) {
            EstadoCliente estado = cliente.getEstado();
            switch (estado) {
                case COMPRAR_BOLETO:
                    return new Point(150, 150);
                case COMPRAR_DULCES:
                    return new Point(400, 150);
                case CAMINAR_AL_BAÑO:
                case CAMINAR_AL_ASIENTO:
                    return new Point(350, 250);
                case SENTARSE:
                case VER_LA_PELICULA:
                    return new Point(700, 300);
                case CAMINAR_A_LA_SALIDA:
                    return new Point(900, 500);
                default:
                    return new Point(50, 500);
            }
        }

        private void dibujarAsientos(Graphics2D g2d) {
            int asientosDisponibles = recursos.getAsientosDisponibles();
            int maxAsientos = recursos.getMaxAsientos();

            int asientosPorFila = Math.min(8, maxAsientos);
            int filas = (int) Math.ceil((double) maxAsientos / asientosPorFila);
            int asientoActual = 0;

            int espacioEntreAsientos = 30;
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            for (int fila = 0; fila < filas && asientoActual < maxAsientos; fila++) {
                int asientosRestantes = maxAsientos - asientoActual;
                int asientosEnEstaFila = Math.min(asientosPorFila, asientosRestantes);

                for (int col = 0; col < asientosEnEstaFila; col++) {
                    if (asientoActual >= maxAsientos) {
                        break;
                    }

                    int x = 600 + (col * espacioEntreAsientos);
                    int y = 200 + (fila * espacioEntreAsientos);

                    boolean ocupado = (maxAsientos - asientosDisponibles) > asientoActual;

                    g2d.setColor(ocupado ? Color.RED : Color.GREEN);
                    g2d.fillRect(x, y, 20, 20);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, 20, 20);
                    g2d.drawString(String.valueOf(asientoActual + 1), x + 5, y + 15);

                    asientoActual++;
                }
            }
        }

        private void dibujarInformacion(Graphics2D g2d) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));

            String info = String.format("Función: %d | Asientos disponibles: %d/%d",
                    recursos.getFuncionActual(),
                    recursos.getAsientosDisponibles(),
                    recursos.getMaxAsientos());
            g2d.drawString(info, 50, 20);

            String colaInfo = String.format("Cola Taquilla: %d | Cola Dulcería: %d | Cola Acomodador: %d",
                    recursos.getBufferOccupancyTaquilla(),
                    recursos.getBufferOccupancyDulceria(),
                    recursos.getBufferOccupancyAcomodador());
            g2d.drawString(colaInfo, 50, 35);
        }
    }

    public void cerrar() {
        timer.stop();
        dispose();
    }
}