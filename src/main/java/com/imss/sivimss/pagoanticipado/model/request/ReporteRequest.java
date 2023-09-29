package com.imss.sivimss.pagoanticipado.model.request;

import lombok.Data;

@Data
public class ReporteRequest {

    private String tipoReporte;
    private String nombreContratante;
    private Integer idPlan ;
    private String correoElectronico;
    private String paquete;
    private String estado;
}
