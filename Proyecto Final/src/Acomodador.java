import java.util.concurrent.TimeUnit;

public class Acomodador extends Thread {
    private static int contadorAcomodadores = 0;
    private final int id;
    private EstadoAcomodador estado;
    private final RecursosCine recursos;
    private boolean estaEnTurno;
    private int tiempoRevisarBoleto;
    private int tiempoLlevarAlCliente;
    private int tiempoRegresar;

    public Acomodador(RecursosCine recursos, int tiempoRevisarBoleto, int tiempoLlevarAlCliente, int tiempoRegresar) {
        this.id = ++contadorAcomodadores;
        this.recursos = recursos;
        this.estado = EstadoAcomodador.INICIAR_TRABAJO;
        this.estaEnTurno = true;
        this.tiempoRevisarBoleto = tiempoRevisarBoleto;
        this.tiempoLlevarAlCliente = tiempoLlevarAlCliente;
        this.tiempoRegresar = tiempoRegresar;
        setName("Acomodador-" + id);
    }

    // Inicia en el estado de esperar, mientras trabaja, checa si hay clientes, si no, se queda en el estado de esperar
    // Cuando acabe el turno, cambia de estado a turno acabado
    // Checa constantemente si hay clientes para mover el cliente a su asiento
    @Override
    public void run() {
        estado = EstadoAcomodador.ESPERAR_EN_LA_SALA;
        while (estaEnTurno) {
            try {
                Cliente cliente = recursos.getColaAcomodador().poll(30, TimeUnit.SECONDS);
                if (cliente != null) {
                    atenderCliente(cliente);
                } else {
                    if (!recursos.hayClientesEnCine()) {
                        estado = EstadoAcomodador.TURNO_ACABADO;
                        System.out.println("Acomodador " + id + " ha terminado su turno.");
                        estaEnTurno = false;
                        break;
                    }
                }

                if (recursos.isPeliculaIniciada() && !recursos.getColaAcomodador().isEmpty()) {
                    estado = EstadoAcomodador.ESPERAR_ASIENTOS_DISPONIBLES;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // Aqui si hay un cliente, cambia su estado y lo mueve a su asiento y regresa a su puesto
    private void atenderCliente(Cliente cliente) {
        if (!recursos.puedeEntrarSala()) {
            estado = EstadoAcomodador.ESPERAR_ASIENTOS_DISPONIBLES;
            System.out.println("Acomodador " + id + " esperando asientos disponibles o inicio de siguiente función");
            return;
        }

        estado = EstadoAcomodador.REVISAR_EL_BOLETO;
        System.out.println("Acomodador " + id + " revisando boleto del cliente " + cliente.getClienteId());
        dormir(tiempoRevisarBoleto);

        estado = EstadoAcomodador.LLEVAR_AL_CLIENTE_HACIA_EL_LUGAR;
        dormir(tiempoLlevarAlCliente);
        recursos.registrarClienteAcomodado();
        System.out.println("Acomodador " + id + " llevó al cliente " + cliente.getClienteId() + " a su asiento.");

        estado = EstadoAcomodador.REGRESAR_A_SU_PUESTO;
        dormir(tiempoRegresar);
        estado = EstadoAcomodador.ESPERAR_EN_LA_SALA;
    }

    // Se pone en pausa durante un tiempo
    private void dormir(int milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public EstadoAcomodador getEstado() {
        return estado;
    }

    public long getId() {
        return id;
    }

    public int getTiempoRevisarBoleto() {
        return tiempoRevisarBoleto;
    }

    public int getTiempoLlevarAlCliente() {
        return tiempoLlevarAlCliente;
    }

    public int getTiempoRegresar() {
        return tiempoRegresar;
    }

    public void setTiempoRevisarBoleto(int tiempo) {
        this.tiempoRevisarBoleto = tiempo;
    }

    public void setTiempoLlevarAlCliente(int tiempo) {
        this.tiempoLlevarAlCliente = tiempo;
    }

    public void setTiempoRegresar(int tiempo) {
        this.tiempoRegresar = tiempo;
    }

    public boolean isEstaEnTurno() {
        return estaEnTurno;
    }

    public void setEstaEnTurno(boolean estaEnTurno) {
        this.estaEnTurno = estaEnTurno;
    }
}