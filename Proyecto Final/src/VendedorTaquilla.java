import java.util.concurrent.TimeUnit;

public class VendedorTaquilla extends Thread {
    private static int contadorVendedores = 0;
    private final int id;
    private EstadoVendedorTaquilla estado;
    private final RecursosCine recursos;
    private boolean estaEnTurno;

    private int tiempoAtender;
    private int tiempoImprimirBoleto;
    private int tiempoTurnoAcabado;

    public VendedorTaquilla(RecursosCine recursos, int tiempoAtender, int tiempoImprimirBoleto,
            int tiempoTurnoAcabado) {
        this.id = ++contadorVendedores;
        this.recursos = recursos;
        this.estado = EstadoVendedorTaquilla.INICIAR;
        this.estaEnTurno = true;

        this.tiempoAtender = tiempoAtender;
        this.tiempoImprimirBoleto = tiempoImprimirBoleto;
        this.tiempoTurnoAcabado = tiempoTurnoAcabado;
        setName("Taquillero-" + id);
    }

    @Override
    
    public void run() {
        estado = EstadoVendedorTaquilla.ESPERAR;
        while (estaEnTurno) {
            try {
                Cliente cliente = recursos.getColaTaquilla().poll(30, TimeUnit.SECONDS);
                if (cliente != null) {
                    atenderCliente(cliente);
                } else {
                    if (!recursos.hayClientesEnCine()) {
                        estado = EstadoVendedorTaquilla.TURNO_ACABADO;
                        System.out.println("Vendedor de taquilla " + id + " ha terminado su turno.");
                        estaEnTurno = false;
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void atenderCliente(Cliente cliente) {
        estado = EstadoVendedorTaquilla.ATENDER;
        System.out.println("Vendedor de taquilla " + id + " atendiendo al cliente " + cliente.getClienteId());

        dormir(tiempoAtender);

        estado = EstadoVendedorTaquilla.VERIFICAR_SI_HAY_BOLETO;
        if (recursos.reservarAsiento()) {
            estado = EstadoVendedorTaquilla.IMPRIMIR_BOLETO;
            dormir(tiempoImprimirBoleto);
            recursos.registrarClienteComproBoleto();
            System.out.println("Vendedor de taquilla " + id + " entregó boleto al cliente " + cliente.getClienteId());
        } else {
            estado = EstadoVendedorTaquilla.COMENTAR_AL_CLIENTE;
            System.out.println("Vendedor de taquilla " + id + " informó al cliente " + cliente.getClienteId() +
                    " que no hay asientos disponibles para la función " + recursos.getFuncionActual());
        }

        estado = EstadoVendedorTaquilla.ESPERAR;
    }

    private void dormir(int milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public EstadoVendedorTaquilla getEstado() {
        return estado;
    }

    public long getId() {
        return id;
    }

    public int getTiempoAtender() {
        return tiempoAtender;
    }

    public void setTiempoAtender(int tiempo) {
        this.tiempoAtender = tiempo;
    }

    public int getTiempoImprimirBoleto() {
        return tiempoImprimirBoleto;
    }

    public void setTiempoImprimirBoleto(int tiempo) {
        this.tiempoImprimirBoleto = tiempo;
    }

    public int getTiempoTurnoAcabado() {
        return tiempoTurnoAcabado;
    }

    public void setTiempoTurnoAcabado(int tiempo) {
        this.tiempoTurnoAcabado = tiempo;
    }

    public boolean isEstaEnTurno() {
        return estaEnTurno;
    }

    public void setEstaEnTurno(boolean estaEnTurno) {
        this.estaEnTurno = estaEnTurno;
    }
}