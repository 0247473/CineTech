import java.util.concurrent.TimeUnit;

public class VendedorDulceria extends Thread {
    private static int contadorVendedores = 0;
    private final int id;
    private EstadoVendedorDulceria estado;
    private final RecursosCine recursos;
    private boolean estaEnTurno;

    private int tiempoAtender;
    private int tiempoEntregarProducto;
    private int tiempoTurnoAcabado;

    public VendedorDulceria(RecursosCine recursos, int tiempoAtender, int tiempoEntregarProducto,
            int tiempoTurnoAcabado) {
        this.id = ++contadorVendedores;
        this.recursos = recursos;
        this.estado = EstadoVendedorDulceria.INICIAR;
        this.estaEnTurno = true;

        this.tiempoAtender = tiempoAtender;
        this.tiempoEntregarProducto = tiempoEntregarProducto;
        this.tiempoTurnoAcabado = tiempoTurnoAcabado;
        setName("Dulcero-" + id);
    }

    @Override
    public void run() {
        estado = EstadoVendedorDulceria.ESPERAR;
        while (estaEnTurno) {
            try {
                Cliente cliente = recursos.getColaDulceria().poll(30, TimeUnit.SECONDS);
                if (cliente != null) {
                    atenderCliente(cliente);
                } else {
                    // Si no hay clientes en la cola, verificar si hay clientes en el cine
                    if (!recursos.hayClientesEnCine()) {
                        estado = EstadoVendedorDulceria.TURNO_ACABADO;
                        System.out.println("Vendedor de dulcería " + id + " ha terminado su turno.");
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
        estado = EstadoVendedorDulceria.ATENDER;
        System.out.println("Vendedor de dulcería " + id + " atendiendo al cliente " + cliente.getClienteId());

        dormir(tiempoAtender);

        estado = EstadoVendedorDulceria.ENTREGAR_PRODUCTO;
        dormir(tiempoEntregarProducto);
        recursos.registrarClienteAtendidoDulceria();
        System.out.println("Vendedor de dulcería " + id + " entregó productos al cliente " + cliente.getClienteId());

        estado = EstadoVendedorDulceria.ESPERAR;
    }

    private void dormir(int milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Getters y setters
    public EstadoVendedorDulceria getEstado() {
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

    public void setTiempoEntregarProducto(int tiempo) {
        this.tiempoEntregarProducto = tiempo;
    }

    public int getTiempoEntregarProducto() {
        return tiempoEntregarProducto;
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