package com.imss.sivimss.pagoanticipado.model.response;

import lombok.Data;

@Data
public class ReciboPdfResponse {
    private String numeroFolio;
    private String totalMensualidades;
    private String pagosRealizados;
    private String nombreContratante;
    private String importe;
    private String nombrePaquete;
    private String velatorio;
}
