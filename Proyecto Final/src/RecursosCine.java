import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RecursosCine {
    private final int maxAsientos;
    private long tiempoInicioPelicula;
    private long duracionPelicula;
    private boolean peliculaIniciada = false;
    private boolean peliculaTerminada = false;
    private int funcionActual = 1;
    private final int totalFunciones;
    private final CyclicBarrier barrierInicioPelicula;
    private final CyclicBarrier barrierFinPelicula;

    private final Semaphore semaforoTaquilla;
    private final Semaphore semaforoDulceria;
    private final Semaphore semaforoAcomodador;

    private final BlockingQueue<Cliente> colaTaquilla;
    private final BlockingQueue<Cliente> colaDulceria;
    private final BlockingQueue<Cliente> colaAcomodador;

    private final AtomicInteger asientosDisponibles;
    private final AtomicInteger clientesSinBoleto;
    private final AtomicInteger clientesQuierenDulces;
    private final AtomicInteger clientesConBoleto;
    private final Set<Integer> asientosOcupados;
    private final AtomicInteger clientesEnSala;

    public RecursosCine(int bufferTaquilla, int bufferDulceria, int bufferAcomodador,
            int maxAsientos, long tiempoInicioPelicula, long duracionPelicula,
            int totalFunciones, int clientesEsperados) {
        this.maxAsientos = maxAsientos;
        this.tiempoInicioPelicula = tiempoInicioPelicula;
        this.duracionPelicula = duracionPelicula;
        this.totalFunciones = totalFunciones;

        this.semaforoTaquilla = new Semaphore(1);
        this.semaforoDulceria = new Semaphore(1);
        this.semaforoAcomodador = new Semaphore(1);

        this.colaTaquilla = new LinkedBlockingQueue<>(bufferTaquilla);
        this.colaDulceria = new LinkedBlockingQueue<>(bufferDulceria);
        this.colaAcomodador = new LinkedBlockingQueue<>(bufferAcomodador);

        this.asientosDisponibles = new AtomicInteger(maxAsientos);
        this.clientesSinBoleto = new AtomicInteger(clientesEsperados);
        this.clientesQuierenDulces = new AtomicInteger(0);
        this.clientesConBoleto = new AtomicInteger(0);
        this.asientosOcupados = Collections.synchronizedSet(new HashSet<>());
        this.clientesEnSala = new AtomicInteger(0);

        this.barrierInicioPelicula = new CyclicBarrier(clientesEsperados + 1, this::iniciarPelicula);
        this.barrierFinPelicula = new CyclicBarrier(clientesEsperados + 1, this::terminarPelicula);

        iniciarControladorFunciones();
    }

    // Espera a los clientes para iniciar y inicia la funcion cuando todos esten listos, no se aceptan despues clientes
    private void iniciarControladorFunciones() {
        Thread controlador = new Thread(() -> {
            try {
                while (funcionActual <= totalFunciones) {
                    System.out.println("Esperando que los clientes estén listos para la función " + funcionActual);
                    barrierInicioPelicula.await();
                    peliculaIniciada = true;
                    System.out.println("¡Inicia la función " + funcionActual + "!");
                    Thread.sleep(duracionPelicula);
                    barrierFinPelicula.await();
                    peliculaTerminada = true;
                    System.out.println("¡Termina la función " + funcionActual + "!");

                    //prepararSiguienteFuncion();
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
            }
        });
        controlador.setDaemon(true);
        controlador.start();
    }

    /* 
    private void prepararSiguienteFuncion() {
        funcionActual++;
        if (funcionActual <= totalFunciones) {
            peliculaIniciada = false;
            peliculaTerminada = false;
            asientosDisponibles.set(maxAsientos);
            asientosOcupados.clear();
            clientesEnSala.set(0);
            tiempoInicioPelicula = System.currentTimeMillis() + 5000;
            System.out.println("Preparando función " + funcionActual);
        }
    }*/

    //Checa si hay clientes en el cine, sino para mandar a los trabajadores para que acaben turno
    public synchronized boolean hayClientesEnCine() {
        return clientesEnSala.get() > 0 || 
                !colaTaquilla.isEmpty() || 
                !colaDulceria.isEmpty() || 
                !colaAcomodador.isEmpty() || 
                clientesSinBoleto.get() > 0 || 
                clientesQuierenDulces.get() > 0 || 
                clientesConBoleto.get() > 0; 
    }

    private void iniciarPelicula() {
        peliculaIniciada = true;
        System.out.println("¡La película está comenzando! Todos los espectadores están en sus asientos.");
    }

    private void terminarPelicula() {
        peliculaTerminada = true;
        System.out.println("¡La película ha terminado! Los espectadores pueden salir.");
    }

    public synchronized boolean puedeEntrarSala() {
        return asientosDisponibles.get() > 0 && !peliculaIniciada && funcionActual <= totalFunciones;
    }

    //Usa el acomodador para checar si hay acientos y bloquearlos
    public synchronized boolean reservarAsiento() {
        if (asientosDisponibles.get() > 0 && !peliculaIniciada) {
            int numeroAsiento = encontrarPrimerAsientoDisponible();
            if (numeroAsiento != -1) {
                asientosOcupados.add(numeroAsiento);
                asientosDisponibles.decrementAndGet();
                return true;
            }
        }
        return false;
    }

    //Checa cual es el asiento más cercano disponible
    private int encontrarPrimerAsientoDisponible() {
        for (int i = 1; i <= maxAsientos; i++) {
            if (!asientosOcupados.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    public void esperarInicioPelicula() throws InterruptedException, BrokenBarrierException {
        System.out.println("Cliente esperando a que inicie la película...");
        barrierInicioPelicula.await();
    }

    public void esperarFinPelicula() throws InterruptedException, BrokenBarrierException {
        System.out.println("Cliente esperando a que termine la película...");
        barrierFinPelicula.await();
    }

    public synchronized void registrarClienteQuiereDulces() {
        clientesQuierenDulces.incrementAndGet();
    }

    public synchronized void registrarClienteComproBoleto() {
        clientesSinBoleto.decrementAndGet();
        clientesConBoleto.incrementAndGet();
    }

    public synchronized void registrarClienteAtendidoDulceria() {
        clientesQuierenDulces.decrementAndGet();
    }

    public synchronized void registrarClienteAcomodado() {
        clientesConBoleto.decrementAndGet();
    }

    public boolean hayClientesPendientesTaquilla() {
        return clientesSinBoleto.get() > 0 || !colaTaquilla.isEmpty();
    }

    public boolean hayClientesPendientesDulceria() {
        return clientesQuierenDulces.get() > 0 || !colaDulceria.isEmpty();
    }

    public boolean hayClientesPendientesAcomodar() {
        return clientesConBoleto.get() > 0 || !colaAcomodador.isEmpty();
    }

    public Semaphore getSemaforoTaquilla() {
        return semaforoTaquilla;
    }

    public Semaphore getSemaforoDulceria() {
        return semaforoDulceria;
    }

    public Semaphore getSemaforoAcomodador() {
        return semaforoAcomodador;
    }

    public BlockingQueue<Cliente> getColaTaquilla() {
        return colaTaquilla;
    }

    public BlockingQueue<Cliente> getColaDulceria() {
        return colaDulceria;
    }

    public BlockingQueue<Cliente> getColaAcomodador() {
        return colaAcomodador;
    }

    public int getAsientosDisponibles() {
        return asientosDisponibles.get();
    }

    public int getMaxAsientos() {
        return maxAsientos;
    }

    public synchronized boolean isPeliculaIniciada() {
        return peliculaIniciada;
    }

    public synchronized boolean isPeliculaTerminada() {
        return peliculaTerminada;
    }

    public synchronized boolean hayMasFunciones() {
        return funcionActual <= totalFunciones;
    }

    public synchronized int getFuncionActual() {
        return funcionActual;
    }

    public long getTiempoInicioPelicula() {
        return tiempoInicioPelicula;
    }

    public long getDuracionPelicula() {
        return duracionPelicula;
    }

    public int getClientesEnSala() {
        return clientesEnSala.get();
    }

    public void setTiempoInicioPelicula(long tiempo) {
        this.tiempoInicioPelicula = tiempo;
    }

    public void setDuracionPelicula(long duracion) {
        this.duracionPelicula = duracion;
    }

    public void incrementarClientesEnSala() {
        clientesEnSala.incrementAndGet();
    }

    public void decrementarClientesEnSala() {
        clientesEnSala.decrementAndGet();
    }

    public long getTiempoRestante() {
        return Math.max(0, tiempoInicioPelicula - System.currentTimeMillis());
    }

    public int getBufferSizeTaquilla() {
        return colaTaquilla.remainingCapacity();
    }

    public int getBufferSizeDulceria() {
        return colaDulceria.remainingCapacity();
    }

    public int getBufferSizeAcomodador() {
        return colaAcomodador.remainingCapacity();
    }

    public int getBufferOccupancyTaquilla() {
        return colaTaquilla.size();
    }

    public int getBufferOccupancyDulceria() {
        return colaDulceria.size();
    }

    public int getBufferOccupancyAcomodador() {
        return colaAcomodador.size();
    }
}