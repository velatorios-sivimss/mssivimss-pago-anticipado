package com.imss.sivimss.pagoanticipado.model.request;

import lombok.Data;

@Data
public class ReporteRequest {

    private String tipoReporte;
    private String idVelatorio;
    private String folioPlan;
    private String fechaInicio;
    private String fechaFin;
    private String nombreContratante;
}
