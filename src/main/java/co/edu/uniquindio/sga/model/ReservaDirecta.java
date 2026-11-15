package co.edu.uniquindio.sga.model;

import java.time.LocalDate;
import java.util.List;

public class ReservaDirecta extends Reserva {
    public ReservaDirecta(String codigo, Huesped titular, Apartamento apartamento,
                          LocalDate fechaEntrada, LocalDate fechaSalida, List<Ocupante> ocupantes,
                          long valorEstancia, PoliticaCancelacion politica) {
        super(codigo, titular, apartamento, fechaEntrada, fechaSalida, ocupantes, valorEstancia, politica);
    }

    @Override
    public String getCanal() { return "DIRECTA"; }

    @Override
    public boolean cumpleRequisitoConfirmacion() { return true; }
}