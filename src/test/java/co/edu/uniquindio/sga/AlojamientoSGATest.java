package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.model.*;
import co.edu.uniquindio.sga.service.AlojamientoSGA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlojamientoSGATest {
    private AlojamientoSGA sga;
    private Huesped huesped;
    private Apartamento apt101;

    @BeforeEach
    void setUp() {
        sga = new AlojamientoSGA();
        huesped = sga.getHuespedes().get(0);
        apt101 = sga.getApartamentos().get(0);
    }

    @Test
    void testSinDobleVentaYDeteccionConflictoExterno() {
        LocalDate entrada = LocalDate.now().plusDays(5);
        LocalDate salida = LocalDate.now().plusDays(8);
        List<Ocupante> ocupantes = List.of(new Ocupante("Samuel", LocalDate.of(2003, 4, 15)));

        ReservaPortal r1 = sga.crearReservaPortal("R-01", huesped, apt101, entrada, salida, ocupantes);
        assertNotNull(r1);

        // Intento de solapamiento en la noche del día 7 debe fallar
        assertThrows(IllegalStateException.class, () ->
                sga.crearReservaDirecta("R-02", huesped, apt101, entrada.plusDays(2), salida.plusDays(2), ocupantes));

        // Intento por canal externo registra conflicto sin sobrescribir
        assertThrows(IllegalStateException.class, () ->
                sga.recibirReservaExterna("R-03", "EXT-999", huesped, apt101, entrada, salida, ocupantes));
        assertEquals(1, sga.getConflictosCanal().size());

        // Reserva exactamente el día de salida (día 8) sí debe permitirse (noche de salida libre)
        assertDoesNotThrow(() ->
                sga.crearReservaDirecta("R-04", huesped, apt101, salida, salida.plusDays(2), ocupantes));
    }

    @Test
    void testLiquidacionPorTemporadasYMenoresDe6AniosNoPagan() {
        // 16 nov (Temporada MEDIA: $120.000) y 17 nov (Temporada MEDIA: $120.000), 18 nov (BAJA: $90.000)
        LocalDate entrada = LocalDate.of(2026, 11, 16);
        LocalDate salida = LocalDate.of(2026, 11, 19); // 3 noches: 16, 17, 18

        List<Ocupante> grupo = List.of(
                new Ocupante("Adulto 1", LocalDate.of(1995, 1, 1)), // Paga
                new Ocupante("Adulto 2", LocalDate.of(1998, 1, 1)), // Paga
                new Ocupante("Niño 4 años", LocalDate.of(2022, 5, 1)) // No paga (< 6 años), ocupa cupo
        );

        long total = sga.cotizarEstancia(apt101, entrada, salida, grupo, null);
        // 2 pagantes * (120.000 + 120.000 + 90.000) = 2 * 330.000 = 660.000
        assertEquals(660_000L, total);
    }

    @Test
    void testCicloDeVidaAnticipoPortalCheckInYCheckOut() {
        LocalDate hoy = LocalDate.now();
        List<Ocupante> grupo = List.of(new Ocupante("Samuel", LocalDate.of(2003, 4, 15)));

        ReservaPortal r = sga.crearReservaPortal("R-10", huesped, apt101, hoy, hoy.plusDays(2), grupo);

        // No confirma sin anticipo del 30%
        assertThrows(IllegalStateException.class, r::confirmar);

        // Paga anticipo del 30% y confirma
        long anticipo = Math.round(r.getValorEstanciaCongelado() * 0.30);
        r.getFolio().registrarPago(anticipo, MedioPago.TRANSFERENCIA);
        r.confirmar();
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());

        // Check-in cambia apartamento a OCUPADO
        r.hacerCheckIn(hoy, LocalDateTime.now());
        assertEquals(EstadoReserva.EN_CURSO, r.getEstado());
        assertEquals(EstadoApartamento.OCUPADO, apt101.getEstado());

        // Check-out finaliza reserva y deja apartamento PENDIENTE_PREPARACION
        r.hacerCheckOut(LocalDateTime.now());
        assertEquals(EstadoReserva.FINALIZADA, r.getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, apt101.getEstado());
    }

    @Test
    void testPoliticaCancelacionSegunAntelacion() {
        List<Ocupante> grupo = List.of(new Ocupante("Samuel", LocalDate.of(2003, 4, 15)));

        // Caso 1: Más de 7 días de antelación -> devuelve 100% de lo pagado
        ReservaDirecta rLejana = sga.crearReservaDirecta("R-20", huesped, apt101,
                LocalDate.now().plusDays(15), LocalDate.now().plusDays(17), grupo);
        rLejana.getFolio().registrarPago(100_000L, MedioPago.EFECTIVO);
        rLejana.confirmar();
        long dev100 = rLejana.cancelar(LocalDateTime.now());
        assertEquals(100_000L, dev100);

        // Caso 2: Entre 2 y 7 días -> retiene 50% del valor de la estancia
        ReservaDirecta rMedia = sga.crearReservaDirecta("R-21", huesped, apt101,
                LocalDate.now().plusDays(4), LocalDate.now().plusDays(6), grupo);
        long valorEstancia = rMedia.getValorEstanciaCongelado(); // 180.000
        rMedia.getFolio().registrarPago(valorEstancia, MedioPago.TARJETA);
        rMedia.confirmar();
        long dev50 = rMedia.cancelar(LocalDateTime.now());
        assertEquals(valorEstancia / 2, dev50);
    }
}
