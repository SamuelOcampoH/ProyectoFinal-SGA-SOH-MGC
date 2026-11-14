package co.edu.uniquindio.sga.model;

import java.time.LocalDateTime;

public record MovimientoFolio(
        LocalDateTime fechaHora,
        String concepto,
        long monto, // Positivo = Cargo, Negativo = Pago o Reversión
        boolean esPago,
        MedioPago medioPago
) {}