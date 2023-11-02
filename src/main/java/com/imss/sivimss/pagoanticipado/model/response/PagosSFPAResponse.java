package com.imss.sivimss.pagoanticipado.model.response;

import lombok.Data;

@Data
public class PagosSFPAResponse {
    private Integer idPagoSFPA;
    private String noPagos;
    private Integer idPlanSFPA;
    private String velatorio;
    private String fechaParcialidad;
    private Double importeMensual;
    private String estatusPago;
    private Double importePagado;
    private Boolean validaPago;
    private double importeAcumulado;
}
