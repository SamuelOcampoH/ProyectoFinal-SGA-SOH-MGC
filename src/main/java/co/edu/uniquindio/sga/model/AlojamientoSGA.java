package co.edu.uniquindio.sga.model;

import co.edu.uniquindio.sga.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class AlojamientoSGA {
    private final List<Apartamento> apartamentos = new ArrayList<>();
    private final List<Huesped> huespedes = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();
    private final List<PeriodoTemporada> temporadas = new ArrayList<>();
    private final List<BloqueoApartamento> bloqueos = new ArrayList<>();
    private final List<Novedad> novedades = new ArrayList<>();
    private final List<ConflictoCanal> conflictosCanal = new ArrayList<>();
    private PoliticaCancelacion politicaActual = PoliticaCancelacion.estandar();

    public AlojamientoSGA() {
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        empleados.add(new Administrador("Admin UQ", "1001", LocalDate.of(1990, 5, 10), "3100000001", "admin@uq.edu.co", "admin123"));
        empleados.add(new Recepcionista("Laura Recepción", "2001", LocalDate.of(1998, 8, 20), "3100000002", "recep@uq.edu.co", "recep123"));
        huespedes.add(new Huesped("Samuel Ocampo", "109491", LocalDate.of(2003, 4, 15), "3112223344", "samuel@uq.edu.co", "1234", null));

        apartamentos.add(new Apartamento("APT-101", "Suite Mirador Valle", 2, 4,
                List.of("Balcón", "Vista a las montañas", "Chimenea"), 90000, 120000, 160000));
        apartamentos.add(new Apartamento("APT-102", "Aparta-Suite Palma", 1, 2,
                List.of("Jacuzzi", "Cocina equipada"), 110000, 140000, 190000));
        apartamentos.add(new Apartamento("APT-201", "Refugio Familiar Salento", 3, 6,
                List.of("Terraza privada", "2 Baños"), 80000, 105000, 140000));

        temporadas.add(new PeriodoTemporada(Temporada.ALTA, LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 20)));
        temporadas.add(new PeriodoTemporada(Temporada.MEDIA, LocalDate.of(2026, 11, 14), LocalDate.of(2026, 11, 17)));
    }

    public Autenticable iniciarSesion(String usuario, String password) {
        for (Empleado e : empleados) {
            if (e.validarCredenciales(usuario, password)) return e;
        }
        for (Huesped h : huespedes) {
            if (h.validarCredenciales(usuario, password)) return h;
        }
        return null;
    }

    public Temporada obtenerTemporada(LocalDate fecha) {
        for (PeriodoTemporada p : temporadas) {
            if (p.aplicaA(fecha)) return p.temporada();
        }
        return Temporada.BAJA;
    }

    public long cotizarEstancia(Apartamento apt, LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes, List<String> desgloseSalida) {
        if (!salida.isAfter(entrada)) throw new IllegalArgumentException("Salida debe ser posterior a entrada.");
        int pagantes = (int) ocupantes.stream().filter(o -> o.pagaTarifa(entrada)).count();
        long total = 0;

        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            Temporada temp = obtenerTemporada(noche);
            long tarifaPersona = apt.getTarifa(temp);
            long subtotalNoche = tarifaPersona * pagantes;
            total += subtotalNoche;
            if (desgloseSalida != null) {
                desgloseSalida.add(String.format("%s | Temp: %s | %d pagantes x $%,d = $%,d",
                        noche, temp, pagantes, tarifaPersona, subtotalNoche));
            }
        }
        return total;
    }

    public boolean estaDisponible(Apartamento apt, LocalDate entrada, LocalDate salida) {
        if (!apt.isActivo() || apt.getEstado() == EstadoApartamento.FUERA_DE_SERVICIO) return false;

        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            final LocalDate n = noche;
            boolean bloqueado = bloqueos.stream().anyMatch(b -> b.codigoApartamento().equals(apt.getCodigo()) && b.cubreFecha(n));
            if (bloqueado) return false;
        }

        for (Reserva r : reservas) {
            r.verificarVencimiento24Horas(LocalDateTime.now());
            if (r.getEstado().esActiva() && r.getApartamento().getCodigo().equals(apt.getCodigo())) {
                if (r.solapaCon(entrada, salida)) return false;
            }
        }
        return true;
    }

    public List<Apartamento> buscarDisponibles(LocalDate entrada, LocalDate salida, int totalPersonas) {
        return apartamentos.stream()
                .filter(a -> a.getCapacidadMaxima() >= totalPersonas)
                .filter(a -> estaDisponible(a, entrada, salida))
                .collect(Collectors.toList());
    }

    public ReservaPortal crearReservaPortal(String codigo, Huesped titular, Apartamento apt,
                                            LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes) {
        validarCreacionReserva(apt, entrada, salida, ocupantes);
        long valor = cotizarEstancia(apt, entrada, salida, ocupantes, null);
        ReservaPortal r = new ReservaPortal(codigo, titular, apt, entrada, salida, ocupantes, valor, politicaActual);
        reservas.add(r);
        return r;
    }

    public ReservaDirecta crearReservaDirecta(String codigo, Huesped titular, Apartamento apt,
                                              LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes) {
        validarCreacionReserva(apt, entrada, salida, ocupantes);
        long valor = cotizarEstancia(apt, entrada, salida, ocupantes, null);
        ReservaDirecta r = new ReservaDirecta(codigo, titular, apt, entrada, salida, ocupantes, valor, politicaActual);
        reservas.add(r);
        return r;
    }