package com.imss.sivimss.pagoanticipado.model.request;

import lombok.Data;

@Data
public class BusquedaRequest {

    private String idVelatorio;
    private String folio;
    private String fechaInicio;
    private String fechaFin;
    private String nombreTitularSustituto;
}
