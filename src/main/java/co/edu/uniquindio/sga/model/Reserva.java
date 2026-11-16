package co.edu.uniquindio.sga.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public abstract class Reserva implements Facturable {
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
    