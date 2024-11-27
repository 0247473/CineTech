import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;

public class Cliente extends Thread {
    private final int id;
    private EstadoCliente estado;
    private final RecursosCine recursos;
    private boolean tieneBoleto;
    private boolean quiereDulces;
    private boolean necesitaBaño;

    private int tiempoCompraBoleto;
    private int tiempoCompraDulces;
    private int tiempoVerPelicula;
    private int tiempoSalir;
    private int tiempoBaño;

    public Cliente(int id, RecursosCine recursos, int tiempoCompraBoleto, int tiempoCompraDulces,
            int tiempoVerPelicula, int tiempoSalir, int tiempoBaño, double probDulces, double probBaño) {
        this.id = id;
        this.recursos = recursos;
        this.estado = EstadoCliente.ENTRAR_AL_CINE;
        this.tieneBoleto = false;
        this.quiereDulces = Math.random() < probDulces;
        this.necesitaBaño = Math.random() < probBaño;

        this.tiempoCompraBoleto = tiempoCompraBoleto;
        this.tiempoCompraDulces = tiempoCompraDulces;
        this.tiempoVerPelicula = tiempoVerPelicula;
        this.tiempoSalir = tiempoSalir;
        this.tiempoBaño = tiempoBaño;
    }

    // Verifica si el cliente puede comprar un boleto, si hay, continua, si no, se va a casa
    // Despues entra al cine y se decide por probabilidad si quiere dulces o no y si necesita ir al baño o no
    // Ve la pelicula, sale del cine y se va a casa y manda un mensaje al servidor avisando que se va a casa
    @Override
    public void run() {
        try {
            long tiempoTotal = calcularTiempoTotal();
            if (tiempoTotal + System.currentTimeMillis() > recursos.getTiempoInicioPelicula()) {
                System.out.println("Cliente " + id + " no alcanzará a ver la película, se va a casa.");
                estado = EstadoCliente.CAMINAR_A_LA_SALIDA;
                dormir(tiempoSalir);
                registrar(String.valueOf(id));
                return;
            }

            entrarAlCine();

            if (recursos.getAsientosDisponibles() <= 0) {
                System.out.println("Cliente " + id + " ve que no hay asientos disponibles, se va a casa.");
                estado = EstadoCliente.CAMINAR_A_LA_SALIDA;
                dormir(tiempoSalir);
                registrar(String.valueOf(id));
                return;
            }

            if (!tieneBoleto) {
                comprarBoleto();
            }
            if (necesitaBaño) {
                irAlBaño();
            }
            if (quiereDulces) {
                comprarDulces();
            }
            verPelicula();
            salir();
            registrar(String.valueOf(id));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Aqui explica cuanto se tarda en realizar todo su movimiento
    private long calcularTiempoTotal() {
        long tiempo = tiempoCompraBoleto;
        if (necesitaBaño)
            tiempo += tiempoBaño;
        if (quiereDulces)
            tiempo += tiempoCompraDulces;
        tiempo += 2000;
        return tiempo;
    }

    //Cambia de estado a entrar al cine
    private void entrarAlCine() throws InterruptedException {
        estado = EstadoCliente.ENTRAR_AL_CINE;
        System.out.println("Cliente " + id + " entrando al cine");
        dormir(1000);
    }

    //Cambia de estado a comprar boleto
    private void comprarBoleto() throws InterruptedException {
        estado = EstadoCliente.COMPRAR_BOLETO;
        System.out.println("Cliente " + id + " haciendo fila para boletos");
        recursos.getColaTaquilla().put(this);
        recursos.getSemaforoTaquilla().acquire();
        dormir(tiempoCompraBoleto);
        tieneBoleto = true;
        recursos.getSemaforoTaquilla().release();
        System.out.println("Cliente " + id + " ha recibido su boleto");
    }

    //Cambia de estado a caminar a la dulceria
    private void comprarDulces() throws InterruptedException {
        estado = EstadoCliente.CAMINAR_A_LA_DULCERIA;
        System.out.println("Cliente " + id + " caminando a la dulcería");
        dormir(1000);
        estado = EstadoCliente.COMPRAR_DULCES;
        recursos.getColaDulceria().put(this);
        recursos.getSemaforoDulceria().acquire();
        dormir(tiempoCompraDulces);
        recursos.getSemaforoDulceria().release();
        System.out.println("Cliente " + id + " ha recibido sus dulces");
    }

    //Cambia de estado a esperar al acomodador, ve la pelicula y sale del cine
    private void verPelicula() throws InterruptedException {
        estado = EstadoCliente.ESPERAR_AL_ACOMODADOR;
        if (!recursos.puedeEntrarSala()) {
            System.out.println("Cliente " + id + " no puede entrar a la sala, se va del cine");
            estado = EstadoCliente.CAMINAR_A_LA_SALIDA;
            return;
        }

        recursos.getColaAcomodador().put(this);
        recursos.getSemaforoAcomodador().acquire();

        estado = EstadoCliente.CAMINAR_AL_ASIENTO;
        dormir(1000);

        estado = EstadoCliente.SENTARSE;
        System.out.println("Cliente " + id + " se ha sentado en su asiento");
        recursos.getSemaforoAcomodador().release();
        recursos.incrementarClientesEnSala();

        try {
            recursos.esperarInicioPelicula();

            estado = EstadoCliente.VER_LA_PELICULA;
            System.out.println("Cliente " + id + " está viendo la película");

            recursos.esperarFinPelicula();

        } catch (BrokenBarrierException e) {
            System.out.println("Cliente " + id + ": Hubo un problema con la sincronización de la película");
        } finally {
            recursos.decrementarClientesEnSala();
        }
    }

    //Sale del cine
    private void salir() throws InterruptedException {
        estado = EstadoCliente.CAMINAR_A_LA_SALIDA;
        dormir(tiempoSalir);
        System.out.println("Cliente " + id + " saliendo del cine");
    }

    // Manda una conexion al servidor el cual manda el ID del cliente para que se imprima en el servidor
    private void registrar(String nombre) {
        try (Socket socket = new Socket("localhost", 1234)) {
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println(nombre);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Cambia el estado a ir al baño
    private void irAlBaño() throws InterruptedException {
        estado = EstadoCliente.CAMINAR_AL_BAÑO;
        System.out.println("Cliente " + id + " usando el baño");
        dormir(tiempoBaño);
    }

    private void dormir(int milisegundos) throws InterruptedException {
        Thread.sleep(milisegundos);
    }

    public EstadoCliente getEstado() {
        return estado;
    }

    public int getTiempoCompraBoleto() {
        return tiempoCompraBoleto;
    }

    public int getTiempoCompraDulces() {
        return tiempoCompraDulces;
    }

    public int getTiempoVerPelicula() {
        return tiempoVerPelicula;
    }

    public int getTiempoSalir() {
        return tiempoSalir;
    }

    public int getTiempoBaño() {
        return tiempoBaño;
    }

    public int getClienteId() {
        return id;
    }

    public void setTiempoCompraBoleto(int tiempo) {
        this.tiempoCompraBoleto = tiempo;
    }

    public void setTiempoCompraDulces(int tiempo) {
        this.tiempoCompraDulces = tiempo;
    }

    public void setTiempoVerPelicula(int tiempo) {
        this.tiempoVerPelicula = tiempo;
    }

    public void setTiempoSalir(int tiempo) {
        this.tiempoSalir = tiempo;
    }

    public void setTiempoBaño(int tiempo) {
        this.tiempoBaño = tiempo;
    }
}