package co.edu.uniquindio.sga.model;

public record PoliticaCancelacion(
        int diasSinPenalidad,
        int diasPenalidadMedia,
        double porcentajeRetencionMedia
) {
    public static PoliticaCancelacion estandar() {
        return new PoliticaCancelacion(7, 2, 0.50);
    }
}
