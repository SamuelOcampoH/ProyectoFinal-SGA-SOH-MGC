package co.edu.uniquindio.sga.model;

import java.time.LocalDate;
import java.util.List;

public class ReservaPortal extends Reserva {
    public ReservaPortal(String codigo, Huesped titular, Apartamento apartamento,
                         LocalDate fechaEntrada, LocalDate fechaSalida, List<Ocupante> ocupantes,
                         long valorEstancia, PoliticaCancelacion politica) {
        super(codigo, titular, apartamento, fechaEntrada, fechaSalida, ocupantes, valorEstancia, politica);
    }

    @Override
    public String getCanal() { return "PORTAL"; }

    @Override
    public boolean cumpleRequisitoConfirmacion() {
        long anticipoMinimo = Math.round(getValorEstanciaCongelado() * 0.30);
        return getFolio().getTotalPagadoNeto() >= anticipoMinimo;
    }
}