package co.edu.uniquindio.sga.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public abstract class Reserva<Apartamento> implements Facturable {
    private final String codigo;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final List<Ocupante> ocupantes;
    private final long valorEstanciaCongelado;
    private final PoliticaCancelacion politicaCongelada;
    private final LocalDateTime fechaCreacion;
    private EstadoReserva estado;
    private final Folio folio;
    private long devolucionRegistrada;

    protected Reserva(String codigo, Huesped titular, Apartamento apartamento,
                      LocalDate fechaEntrada, LocalDate fechaSalida, List<Ocupante> ocupantes,
                      long valorEstanciaCongelado, PoliticaCancelacion politicaCongelada) {
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada.");
        }
        if (ocupantes == null || ocupantes.isEmpty() || ocupantes.size() > apartamento.getCapacidadMaxima()) {
            throw new IllegalArgumentException("Cantidad de ocupantes inválida o supera la capacidad.");
        }
        this.codigo = codigo;
        this.titular = titular;
        this.apartamento = apartamento;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.ocupantes = List.copyOf(ocupantes);
        this.valorEstanciaCongelado = valorEstanciaCongelado;
        this.politicaCongelada = politicaCongelada;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoReserva.PENDIENTE;
        this.folio = new Folio(codigo, valorEstanciaCongelado);
    }

    public abstract String getCanal();

    public abstract boolean cumpleRequisitoConfirmacion();

    public void verificarVencimiento24Horas(LocalDateTime ahora) {
        if (estado == EstadoReserva.PENDIENTE && Duration.between(fechaCreacion, ahora).toHours() >= 24) {
            estado = EstadoReserva.CANCELADA;
        }
    }

    public void confirmar() {
        if (!estado.puedeTransicionarA(EstadoReserva.CONFIRMADA)) {
            throw new IllegalStateException("Transición inválida a CONFIRMADA desde " + estado);
        }
        if (!cumpleRequisitoConfirmacion()) {
            throw new IllegalStateException("No cumple el requisito de confirmación del canal " + getCanal());
        }
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public void hacerCheckIn(LocalDate fechaHoy, LocalDateTime ahora) {
        if (!estado.puedeTransicionarA(EstadoReserva.EN_CURSO)) {
            throw new IllegalStateException("Solo se puede hacer check-in en estado CONFIRMADA.");
        }
        if (fechaHoy.isBefore(fechaEntrada)) {
            throw new IllegalStateException("Aún no ha llegado la fecha de entrada.");
        }
        if (apartamento.getEstado() != EstadoApartamento.PREPARADO) {
            throw new IllegalStateException("El apartamento no está PREPARADO.");
        }
        if (apartamento.getUltimaSalida() != null &&
                Duration.between(apartamento.getUltimaSalida(), ahora).toHours() < 3) {
            throw new IllegalStateException("No han pasado 3 horas desde la última salida.");
        }
        this.estado = EstadoReserva.EN_CURSO;
        apartamento.cambiarEstado(EstadoApartamento.OCUPADO, null);
    }

    public void hacerCheckOut(LocalDateTime ahora) {
        if (!estado.puedeTransicionarA(EstadoReserva.FINALIZADA)) {
            throw new IllegalStateException("Solo se puede hacer check-out si está EN_CURSO.");
        }
        this.estado = EstadoReserva.FINALIZADA;
        apartamento.setUltimaSalida(ahora);
        apartamento.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION, null);
    }

    public long calcularDevolucionCancelacion(LocalDateTime momentoCancelacion) {
        long pagado = folio.getTotalPagadoNeto();
        if (pagado <= 0) return 0;

        LocalDateTime limiteEntrada = fechaEntrada.atTime(15, 0);
        long horasAntelacion = Duration.between(momentoCancelacion, limiteEntrada).toHours();
        long diasAntelacion = ChronoUnit.DAYS.between(momentoCancelacion.toLocalDate(), fechaEntrada);

        if (diasAntelacion > politicaCongelada.diasSinPenalidad()) {
            return pagado;
        } else if (horasAntelacion >= 48) {
            long retencion = Math.round(valorEstanciaCongelado * politicaCongelada.porcentajeRetencionMedia());
            return Math.max(0, pagado - retencion);
        } else {
            return 0;
        }
    }

    public long cancelar(LocalDateTime momentoCancelacion) {
        if (!estado.puedeTransicionarA(EstadoReserva.CANCELADA)) {
            throw new IllegalStateException("No se puede cancelar desde estado " + estado);
        }
        long devolucion = calcularDevolucionCancelacion(momentoCancelacion);
        this.devolucionRegistrada = devolucion;
        if (devolucion > 0) {
            folio.registrarDevolucion(devolucion, "Cancelación de reserva");
        }
        this.estado = EstadoReserva.CANCELADA;
        return devolucion;
    }

    public void declararNoShow() {
        if (!estado.puedeTransicionarA(EstadoReserva.NO_SHOW)) {
            throw new IllegalStateException("Solo se declara No-Show desde CONFIRMADA.");
        }
        this.devolucionRegistrada = 0;
        this.estado = EstadoReserva.NO_SHOW;
    } public boolean solapaCon(LocalDate otraEntrada, LocalDate otraSalida) {
        return this.fechaEntrada.isBefore(otraSalida) && otraEntrada.isBefore(this.fechaSalida);
    }

    @Override
    public long calcularValorTotal() {
        return folio.getTotalCargos();
    }

    @Override
    public String obtenerDetalleFacturacion() {
        return String.format("Reserva %s [%s] | Cargos: $%,d | Pagado: $%,d | Saldo: $%,d",
                codigo, getCanal(), folio.getTotalCargos(), folio.getTotalPagadoNeto(), folio.getSaldoPendiente());
    }

    public String getCodigo() { return codigo; }
    public Huesped getTitular() { return titular; }
    public Apartamento getApartamento() { return apartamento; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public List<Ocupante> getOcupantes() { return ocupantes; }
    public long getValorEstanciaCongelado() { return valorEstanciaCongelado; }
    public EstadoReserva getEstado() { return estado; }
    public Folio getFolio() { return folio; }
    public long getDevolucionRegistrada() { return devolucionRegistrada; }
}