import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class SimulacionCine {
    private JFrame frame;
    private JTextArea infoArea;
    private volatile boolean running = true;
    private Map<String, JComponent> configComponents;
    private VentanaVisual ventanaVisual;
    private Thread monitoringThread;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulacionCine().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Simulación CineTech");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupAndExit();
            }
        });
        frame.setSize(1600, 900);
        frame.setLayout(new BorderLayout());
    
        JPanel mainPanel = new JPanel(new BorderLayout());
    
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
    
        configPanel.add(createAgentesConfigPanel());
        configPanel.add(createBuffersConfigPanel());
        configPanel.add(createTiemposConfigPanel());
        configPanel.add(createFuncionesConfigPanel());
        configPanel.add(createProbabilidadesConfigPanel());
    
        JScrollPane configScrollPane = new JScrollPane(configPanel);
        configScrollPane.setPreferredSize(new Dimension(500, 800));
        mainPanel.add(configScrollPane, BorderLayout.WEST);
    
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        JScrollPane infoScrollPane = new JScrollPane(infoArea);
        mainPanel.add(infoScrollPane, BorderLayout.CENTER);
    
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    
        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createAgentesConfigPanel() {
        JPanel panel = createTitledPanel("Configuración de Agentes");
        configComponents = new HashMap<>();

        addLabeledSpinner(panel, "Número de Clientes:", "numClientes", 1, 10, 50, 1);
        addLabeledSpinner(panel, "Número de Taquilleros:", "numTaquilleros", 1, 2, 10, 1);
        addLabeledSpinner(panel, "Número de Dulceros:", "numDulceros", 1, 2, 10, 1);
        addLabeledSpinner(panel, "Número de Acomodadores:", "numAcomodadores", 1, 2, 10, 1);

        return panel;
    }

    private boolean validarLimites() {
        StringBuilder mensajeError = new StringBuilder("Se han detectado los siguientes problemas:\n\n");
        boolean hayErrores = false;

        int numClientes = getSpinnerValue("numClientes");
        if (numClientes > 50) {
            mensajeError.append("- El número de clientes no puede exceder 50\n");
            ((JSpinner)configComponents.get("numClientes")).setValue(50);
            hayErrores = true;
        }

        int numTaquilleros = getSpinnerValue("numTaquilleros");
        if (numTaquilleros > 10) {
            mensajeError.append("- El número de taquilleros no puede exceder 10\n");
            ((JSpinner)configComponents.get("numTaquilleros")).setValue(10);
            hayErrores = true;
        }

        int numDulceros = getSpinnerValue("numDulceros");
        if (numDulceros > 10) {
            mensajeError.append("- El número de dulceros no puede exceder 10\n");
            ((JSpinner)configComponents.get("numDulceros")).setValue(10);
            hayErrores = true;
        }

        int numAcomodadores = getSpinnerValue("numAcomodadores");
        if (numAcomodadores > 10) {
            mensajeError.append("- El número de acomodadores no puede exceder 10\n");
            ((JSpinner)configComponents.get("numAcomodadores")).setValue(10);
            hayErrores = true;
        }

        int bufferTaquilla = getSpinnerValue("bufferTaquilla");
        if (bufferTaquilla > 20) {
            mensajeError.append("- El tamaño de la cola de taquilla no puede exceder 20\n");
            ((JSpinner)configComponents.get("bufferTaquilla")).setValue(20);
            hayErrores = true;
        }

        int bufferDulceria = getSpinnerValue("bufferDulceria");
        if (bufferDulceria > 20) {
            mensajeError.append("- El tamaño de la cola de dulcería no puede exceder 20\n");
            ((JSpinner)configComponents.get("bufferDulceria")).setValue(20);
            hayErrores = true;
        }

        int bufferAcomodador = getSpinnerValue("bufferAcomodador");
        if (bufferAcomodador > 20) {
            mensajeError.append("- El tamaño de la cola de acomodador no puede exceder 20\n");
            ((JSpinner)configComponents.get("bufferAcomodador")).setValue(20);
            hayErrores = true;
        }

        int capacidadSala = getSpinnerValue("capacidadSala");
        if (capacidadSala > 200) {
            mensajeError.append("- La capacidad de la sala no puede exceder 200\n");
            ((JSpinner)configComponents.get("capacidadSala")).setValue(200);
            hayErrores = true;
        }

        if (hayErrores) {
            mensajeError.append("\nLos valores han sido ajustados automáticamente a los límites permitidos.");
            JOptionPane.showMessageDialog(frame,
                mensajeError.toString(),
                "Límites Excedidos",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private JPanel createBuffersConfigPanel() {
        JPanel panel = createTitledPanel("Configuración de Buffers");

        addLabeledSpinner(panel, "Tamaño Cola Taquilla:", "bufferTaquilla", 1, 5, 20, 1);
        addLabeledSpinner(panel, "Tamaño Cola Dulcería:", "bufferDulceria", 1, 5, 20, 1);
        addLabeledSpinner(panel, "Tamaño Cola Acomodador:", "bufferAcomodador", 1, 5, 20, 1);
        addLabeledSpinner(panel, "Capacidad de la Sala:", "capacidadSala", 1, 50, 200, 1);

        return panel;
    }

    private JPanel createTiemposConfigPanel() {
        JPanel panel = createTitledPanel("Configuración de Tiempos (ms)");

        addLabeledSpinner(panel, "Tiempo Compra Boleto:", "tiempoCompraBoleto", 500, 2000, 10000, 500);
        addLabeledSpinner(panel, "Tiempo Compra Dulces:", "tiempoCompraDulces", 500, 2000, 10000, 500);
        addLabeledSpinner(panel, "Tiempo Ver Película:", "tiempoVerPelicula", 5000, 30000, 120000, 5000);
        addLabeledSpinner(panel, "Tiempo Salir:", "tiempoSalir", 500, 1000, 5000, 500);
        addLabeledSpinner(panel, "Tiempo Baño:", "tiempoBaño", 500, 1500, 5000, 500);
        addLabeledSpinner(panel, "Tiempo entre Funciones:", "tiempoEntreFunciones", 1000, 5000, 20000, 1000);

        addLabeledSpinner(panel, "Tiempo Atender Taquilla:", "tiempoAtenderTaquilla", 500, 2000, 5000, 500);
        addLabeledSpinner(panel, "Tiempo Imprimir Boleto:", "tiempoImprimirBoleto", 500, 2000, 5000, 500);
        addLabeledSpinner(panel, "Tiempo Atender Dulcería:", "tiempoAtenderDulceria", 500, 1500, 5000, 500);
        addLabeledSpinner(panel, "Tiempo Entregar Producto:", "tiempoEntregarProducto", 500, 1500, 5000, 500);
        addLabeledSpinner(panel, "Tiempo Revisar Boleto:", "tiempoRevisarBoleto", 500, 1000, 3000, 500);

        return panel;
    }

    private JPanel createFuncionesConfigPanel() {
        JPanel panel = createTitledPanel("Configuración de Funciones");

        addLabeledSpinner(panel, "Número de Funciones:", "numFunciones", 1, 3, 10, 1);
        addLabeledSpinner(panel, "Duración de la Película (ms):", "duracionPelicula", 10000, 30000, 120000, 5000);
        addLabeledSpinner(panel, "Tiempo de Inicio Primera Función (ms):", "tiempoInicioPrimeraFuncion", 5000, 20000,
                60000, 5000);

        return panel;
    }

    private JPanel createProbabilidadesConfigPanel() {
        JPanel panel = createTitledPanel("Configuración de Probabilidades (%)");

        addLabeledSpinner(panel, "Probabilidad de Comprar Dulces:", "probDulces", 0, 70, 100, 5);
        addLabeledSpinner(panel, "Probabilidad de ir al Baño:", "probBaño", 0, 30, 100, 5);

        return panel;
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    private void addLabeledSpinner(JPanel panel, String label, String key, int min, int value, int max, int step) {
        panel.add(new JLabel(label));
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        
        switch (key) {
            case "numClientes":
                spinner.setToolTipText("Límite máximo: 50 clientes");
                break;
            case "numTaquilleros":
            case "numDulceros":
            case "numAcomodadores":
                spinner.setToolTipText("Límite máximo: 10 trabajadores");
                break;
            case "bufferTaquilla":
            case "bufferDulceria":
            case "bufferAcomodador":
                spinner.setToolTipText("Límite máximo: 20");
                break;
            case "capacidadSala":
                spinner.setToolTipText("Límite máximo: 200 asientos");
                break;
        }
        
        configComponents.put(key, spinner);
        panel.add(spinner);
    }


    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JButton startButton = new JButton("Iniciar Simulación");
        JButton stopButton = new JButton("Detener Simulación");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            startSimulation();
        });

        stopButton.addActionListener(e -> {
            stopSimulation();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        });

        JButton creditosButton = new JButton("Créditos");
        creditosButton.addActionListener(e -> new VentanaCreditos(frame));

        JButton exitButton = new JButton("Salir");
        exitButton.addActionListener(e -> cleanupAndExit());

        panel.add(startButton);
        panel.add(stopButton);
        panel.add(creditosButton);
        panel.add(exitButton);

        return panel;
    }

    private void cleanupAndExit() {
        stopSimulation();

        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            window.dispose();
        }

        System.exit(0);
    }

    private void stopSimulation() {
        running = false;
        if (monitoringThread != null) {
            monitoringThread.interrupt();
        }
    }

    private int getSpinnerValue(String key) {
        return ((SpinnerNumberModel) ((JSpinner) configComponents.get(key)).getModel()).getNumber().intValue();
    }

    private void startSimulation() {
        int numClientes = getSpinnerValue("numClientes");
        int numTaquilla = getSpinnerValue("numTaquilleros");
        int numDulceria = getSpinnerValue("numDulceros");
        int numAcomodadores = getSpinnerValue("numAcomodadores");

        int bufferTaquilla = getSpinnerValue("bufferTaquilla");
        int bufferDulceria = getSpinnerValue("bufferDulceria");
        int bufferAcomodador = getSpinnerValue("bufferAcomodador");
        int numAsientos = getSpinnerValue("capacidadSala");

        int numFunciones = getSpinnerValue("numFunciones");
        long duracionPelicula = getSpinnerValue("duracionPelicula");
        long tiempoInicioPelicula = System.currentTimeMillis() + getSpinnerValue("tiempoInicioPrimeraFuncion");

        int tiempoCompraBoleto = getSpinnerValue("tiempoCompraBoleto");
        int tiempoCompraDulces = getSpinnerValue("tiempoCompraDulces");
        int tiempoVerPelicula = getSpinnerValue("tiempoVerPelicula");
        int tiempoSalir = getSpinnerValue("tiempoSalir");
        int tiempoBaño = getSpinnerValue("tiempoBaño");

        double probDulces = getSpinnerValue("probDulces") / 100.0;
        double probBaño = getSpinnerValue("probBaño") / 100.0;

        RecursosCine recursos = new RecursosCine(bufferTaquilla, bufferDulceria, bufferAcomodador,
                numAsientos, tiempoInicioPelicula, duracionPelicula, numFunciones, numClientes);

        List<Cliente> clientes = new ArrayList<>();
        List<VendedorTaquilla> taquilleros = new ArrayList<>();
        List<VendedorDulceria> dulceros = new ArrayList<>();
        List<Acomodador> acomodadores = new ArrayList<>();
        List<Thread> allThreads = new ArrayList<>();

        initializeSimulation(recursos, clientes, taquilleros, dulceros, acomodadores, allThreads,
                numClientes, numTaquilla, numDulceria, numAcomodadores,
                tiempoCompraBoleto, tiempoCompraDulces, tiempoVerPelicula, tiempoSalir, tiempoBaño,
                probDulces, probBaño);

        startMonitoring(recursos, clientes, taquilleros, dulceros, acomodadores, allThreads);
    }

    private void initializeSimulation(RecursosCine recursos, List<Cliente> clientes,
            List<VendedorTaquilla> taquilleros, List<VendedorDulceria> dulceros,
            List<Acomodador> acomodadores, List<Thread> allThreads,
            int numClientes, int numTaquilla, int numDulceria, int numAcomodadores,
            int tiempoCompraBoleto, int tiempoCompraDulces, int tiempoVerPelicula,
            int tiempoSalir, int tiempoBaño, double probDulces, double probBaño) {

        running = true;

        int tiempoAtenderTaquilla = getSpinnerValue("tiempoAtenderTaquilla");
        int tiempoImprimirBoleto = getSpinnerValue("tiempoImprimirBoleto");
        int tiempoAtenderDulceria = getSpinnerValue("tiempoAtenderDulceria");
        int tiempoEntregarProducto = getSpinnerValue("tiempoEntregarProducto");
        int tiempoRevisarBoleto = getSpinnerValue("tiempoRevisarBoleto");

        // Inicializar clientes
        for (int i = 1; i <= numClientes; i++) {
            Cliente cliente = new Cliente(i, recursos, tiempoCompraBoleto, tiempoCompraDulces,
                    tiempoVerPelicula, tiempoSalir, tiempoBaño, probDulces, probBaño);
            clientes.add(cliente);
            allThreads.add(cliente);
            cliente.start();
        }

        // Inicializar taquilleros con nuevos tiempos
        for (int i = 1; i <= numTaquilla; i++) {
            VendedorTaquilla taquillero = new VendedorTaquilla(recursos, tiempoAtenderTaquilla,
                    tiempoImprimirBoleto, 1000);
            taquilleros.add(taquillero);
            allThreads.add(taquillero);
            taquillero.start();
        }

        // Inicializar dulceros con nuevos tiempos
        for (int i = 1; i <= numDulceria; i++) {
            VendedorDulceria dulcero = new VendedorDulceria(recursos, tiempoAtenderDulceria,
                    tiempoEntregarProducto, 1000);
            dulceros.add(dulcero);
            allThreads.add(dulcero);
            dulcero.start();
        }

        // Inicializar acomodadores con nuevos tiempos
        for (int i = 1; i <= numAcomodadores; i++) {
            Acomodador acomodador = new Acomodador(recursos, tiempoRevisarBoleto, 1500, 1000);
            acomodadores.add(acomodador);
            allThreads.add(acomodador);
            acomodador.start();
        }
    }

    private void startMonitoring(RecursosCine recursos, List<Cliente> clientes,
            List<VendedorTaquilla> taquilleros, List<VendedorDulceria> dulceros,
            List<Acomodador> acomodadores, List<Thread> allThreads) {

        monitoringThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    updateGUI(recursos, clientes, taquilleros, dulceros, acomodadores);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitoringThread.start();

        new VentanaMonitoreoUnificada(recursos, clientes, taquilleros, dulceros, acomodadores, allThreads);
        ventanaVisual = new VentanaVisual(recursos, clientes, taquilleros, dulceros, acomodadores);
    }

    private void updateGUI(RecursosCine recursos, List<Cliente> clientes,
        List<VendedorTaquilla> taquilleros, List<VendedorDulceria> dulceros,
        List<Acomodador> acomodadores) {

    if (!running)
        return;

    SwingUtilities.invokeLater(() -> {
        StringBuilder sb = new StringBuilder();
        
        String formatoColumna = "%-40s %-40s %-40s\n";
        
        sb.append(String.format(formatoColumna, 
            "=== Estados Generales ===", 
            "=== Estados de Colas ===",
            "=== Estados de Clientes ==="));
        
        List<String> estadosGenerales = new ArrayList<>();
        estadosGenerales.add(String.format("Función actual: %d", recursos.getFuncionActual()));
        estadosGenerales.add(String.format("Tiempo restante: %d ms", recursos.getTiempoRestante()));
        estadosGenerales.add(String.format("Asientos: %d/%d", 
            recursos.getAsientosDisponibles(), recursos.getMaxAsientos()));

        List<String> estadosColas = new ArrayList<>();
        estadosColas.add(String.format("Cola Taquilla: %d/%d",
            recursos.getBufferOccupancyTaquilla(), recursos.getBufferSizeTaquilla()));
        estadosColas.add(String.format("Cola Dulcería: %d/%d",
            recursos.getBufferOccupancyDulceria(), recursos.getBufferSizeDulceria()));
        estadosColas.add(String.format("Cola Acomodador: %d/%d",
            recursos.getBufferOccupancyAcomodador(), recursos.getBufferSizeAcomodador()));

        Map<EstadoCliente, Integer> estadosClientes = new HashMap<>();
        for (Cliente cliente : clientes) {
            estadosClientes.merge(cliente.getEstado(), 1, Integer::sum);
        }
        List<String> estadosClientesStr = new ArrayList<>();
        for (EstadoCliente estado : EstadoCliente.values()) {
            estadosClientesStr.add(String.format("%s: %d", 
                estado, estadosClientes.getOrDefault(estado, 0)));
        }

        int maxRows = Math.max(Math.max(estadosGenerales.size(), estadosColas.size()), estadosClientesStr.size());
        for (int i = 0; i < maxRows; i++) {
            String col1 = i < estadosGenerales.size() ? estadosGenerales.get(i) : "";
            String col2 = i < estadosColas.size() ? estadosColas.get(i) : "";
            String col3 = i < estadosClientesStr.size() ? estadosClientesStr.get(i) : "";
            sb.append(String.format(formatoColumna, col1, col2, col3));
        }

        sb.append("\n");

        sb.append(String.format("%-60s %-60s\n", 
            "=== Estados de Taquilleros ===", 
            "=== Estados de Dulceros ==="));

        Map<EstadoVendedorTaquilla, Integer> estadosTaquilleros = new HashMap<>();
        for (VendedorTaquilla taquillero : taquilleros) {
            estadosTaquilleros.merge(taquillero.getEstado(), 1, Integer::sum);
        }
        List<String> estadosTaquillerosStr = new ArrayList<>();
        for (EstadoVendedorTaquilla estado : EstadoVendedorTaquilla.values()) {
            estadosTaquillerosStr.add(String.format("%s: %d", 
                estado, estadosTaquilleros.getOrDefault(estado, 0)));
        }

        Map<EstadoVendedorDulceria, Integer> estadosDulceros = new HashMap<>();
        for (VendedorDulceria dulcero : dulceros) {
            estadosDulceros.merge(dulcero.getEstado(), 1, Integer::sum);
        }
        List<String> estadosDulcerosStr = new ArrayList<>();
        for (EstadoVendedorDulceria estado : EstadoVendedorDulceria.values()) {
            estadosDulcerosStr.add(String.format("%s: %d", 
                estado, estadosDulceros.getOrDefault(estado, 0)));
        }

        int maxRowsTrabajadores = Math.max(estadosTaquillerosStr.size(), estadosDulcerosStr.size());
        for (int i = 0; i < maxRowsTrabajadores; i++) {
            String col1 = i < estadosTaquillerosStr.size() ? estadosTaquillerosStr.get(i) : "";
            String col2 = i < estadosDulcerosStr.size() ? estadosDulcerosStr.get(i) : "";
            sb.append(String.format("%-60s %-60s\n", col1, col2));
        }

        sb.append("\n=== Estados de Acomodadores ===\n");
        Map<EstadoAcomodador, Integer> estadosAcomodadores = new HashMap<>();
        for (Acomodador acomodador : acomodadores) {
            estadosAcomodadores.merge(acomodador.getEstado(), 1, Integer::sum);
        }
        for (EstadoAcomodador estado : EstadoAcomodador.values()) {
            sb.append(String.format("%s: %d acomodadores\n", 
                estado, estadosAcomodadores.getOrDefault(estado, 0)));
        }

        infoArea.setText(sb.toString());
    });
}
}