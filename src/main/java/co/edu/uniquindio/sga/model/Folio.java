package co.edu.uniquindio.sga.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Folio {
    private final String codigoReserva;
    private final List<MovimientoFolio> movimientos = new ArrayList<>();

    public Folio(String codigoReserva, long valorEstancia) {
        if (codigoReserva == null || codigoReserva.isBlank()) {
            throw new IllegalArgumentException("El código de la reserva es obligatorio.");
        }
        this.codigoReserva = codigoReserva;
        agregarCargo("Alojamiento", valorEstancia);
    }

    public void agregarCargo(String concepto, long monto) {
        validarConcepto(concepto);
        if (monto <= 0) {
            throw new IllegalArgumentException("El cargo debe ser mayor que cero.");
        }
        movimientos.add(new MovimientoFolio(LocalDateTime.now(), concepto.trim(), monto, false, null));
    }

    public void reversarCargo(String concepto, long monto) {
        validarConcepto(concepto);
        if (monto <= 0 || monto > getTotalCargos()) {
            throw new IllegalArgumentException("Monto de reversa inválido.");
        }
        movimientos.add(new MovimientoFolio(LocalDateTime.now(), "Reversa: " + concepto.trim(), -monto, false, null));
    }

    public void registrarPago(long monto, MedioPago medio) {
        if (medio == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El pago debe ser mayor que cero.");
        }
        if (monto > getSaldoPendiente()) {
            throw new IllegalArgumentException("El pago supera el saldo pendiente.");
        }
        movimientos.add(new MovimientoFolio(LocalDateTime.now(), "Pago", -monto, true, medio));
    }

    public void registrarDevolucion(long monto, String concepto) {
        validarConcepto(concepto);
        if (monto <= 0 || monto > getTotalPagadoNeto()) {
            throw new IllegalArgumentException("La devolución debe ser positiva y no superar lo pagado.");
        }
        movimientos.add(new MovimientoFolio(LocalDateTime.now(), "Devolución: " + concepto.trim(), monto, true, null));
    }

    public long getTotalCargos() {
        return movimientos.stream().filter(m -> !m.esPago()).mapToLong(MovimientoFolio::monto).sum();
    }

    public long getTotalPagadoNeto() {
        return -movimientos.stream().filter(MovimientoFolio::esPago).mapToLong(MovimientoFolio::monto).sum();
    }

    public long getSaldoPendiente() {
        return getTotalCargos() - getTotalPagadoNeto();
    }

    public String getCodigoReserva() { return codigoReserva; }

    public List<MovimientoFolio> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    private void validarConcepto(String concepto) {
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio.");
        }
    }
}