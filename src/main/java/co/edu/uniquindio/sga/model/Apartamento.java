package co.edu.uniquindio.sga.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Apartamento {
    private final String codigo;
    private String nombre;
    private int numeroDormitorios;
    private int capacidadMaxima;
    private final List<String> caracteristicas;
    private long tarifaBaja;
    private long tarifaMedia;
    private long tarifaAlta;
    private EstadoApartamento estado;
    private String motivoFueraServicio;
    private LocalDateTime ultimaSalida;
    private boolean activo;

    public Apartamento(String codigo, String nombre, int numeroDormitorios, int capacidadMaxima,
                       List<String> caracteristicas, long tarifaBaja, long tarifaMedia, long tarifaAlta) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }
        this.codigo = codigo.trim();
        setNombre(nombre);
        setNumeroDormitorios(numeroDormitorios);
        setCapacidadMaxima(capacidadMaxima);
        this.caracteristicas = new ArrayList<>(caracteristicas == null ? List.of() : caracteristicas);
        setTarifas(tarifaBaja, tarifaMedia, tarifaAlta);
        this.estado = EstadoApartamento.PREPARADO;
        this.activo = true;
    }

    /** Tarifa por persona por noche según la temporada. */
    public long getTarifa(Temporada temporada) {
        return switch (temporada) {
            case BAJA -> tarifaBaja;
            case MEDIA -> tarifaMedia;
            case ALTA -> tarifaAlta;
        };
    }

    public void setTarifas(long baja, long media, long alta) {
        if (baja <= 0 || media <= 0 || alta <= 0) {
            throw new IllegalArgumentException("Las tarifas deben ser mayores que cero.");
        }
        this.tarifaBaja = baja;
        this.tarifaMedia = media;
        this.tarifaAlta = alta;
    }

    /** Cambia el estado operativo validando las transiciones permitidas. */
    public void cambiarEstado(EstadoApartamento nuevo, String motivo) {
        boolean permitido = switch (estado) {
            case PREPARADO -> nuevo == EstadoApartamento.OCUPADO || nuevo == EstadoApartamento.FUERA_DE_SERVICIO;
            case OCUPADO -> nuevo == EstadoApartamento.PENDIENTE_PREPARACION;
            case PENDIENTE_PREPARACION -> nuevo == EstadoApartamento.EN_PREPARACION || nuevo == EstadoApartamento.FUERA_DE_SERVICIO;
            case EN_PREPARACION -> nuevo == EstadoApartamento.PREPARADO || nuevo == EstadoApartamento.FUERA_DE_SERVICIO;
            case FUERA_DE_SERVICIO -> nuevo == EstadoApartamento.PENDIENTE_PREPARACION;
        };
        if (!permitido) {
            throw new IllegalStateException("Cambio de estado no permitido: " + estado + " -> " + nuevo);
        }
        if (nuevo == EstadoApartamento.FUERA_DE_SERVICIO) {
            if (motivo == null || motivo.isBlank()) {
                throw new IllegalArgumentException("Debe registrar el motivo de fuera de servicio.");
            }
            motivoFueraServicio = motivo.trim();
        } else {
            motivoFueraServicio = null;
        }
        this.estado = nuevo;
    }

    public void agregarCaracteristica(String c) {
        if (c != null && !c.isBlank() && !caracteristicas.contains(c.trim())) caracteristicas.add(c.trim());
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        this.nombre = nombre.trim();
    }
    public int getNumeroDormitorios() { return numeroDormitorios; }
    public void setNumeroDormitorios(int n) {
        if (n < 1) throw new IllegalArgumentException("Debe tener al menos un dormitorio.");
        this.numeroDormitorios = n;
    }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int c) {
        if (c < 1) throw new IllegalArgumentException("La capacidad máxima debe ser al menos 1.");
        this.capacidadMaxima = c;
    }
    public List<String> getCaracteristicas() { return Collections.unmodifiableList(caracteristicas); }
    public EstadoApartamento getEstado() { return estado; }
    public String getMotivoFueraServicio() { return motivoFueraServicio; }
    public LocalDateTime getUltimaSalida() { return ultimaSalida; }
    public void setUltimaSalida(LocalDateTime ultimaSalida) { this.ultimaSalida = ultimaSalida; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Apartamento a && codigo.equals(a.codigo));
    }
    @Override
    public int hashCode() { return Objects.hash(codigo); }
    @Override
    public String toString() { return codigo + " - " + nombre + " (" + estado + ")"; }
}