package com.imss.sivimss.pagoanticipado.model.response;

import lombok.Data;

@Data
public class DetallePlanResponse {
    private String idPlan;
    private String numFolio;
    private String desNumeroPagos;
    private String nombrePaquete;
    private String contratanteSubstituto;
    private String correo;
    private String estado;
    private String velatorio;
    private String estatusPlan;
    private String total;
    private String restante;
}
