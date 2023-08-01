package com.imss.sivimss.pagoanticipado.model.response;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlanSFPAResponse {
    private String fecha;
    private String folio;
    private String idTitularSustituto;
    private String idTipoPago;
    private String tipoPagosMensuales;
    private String idEstatusPlan;
    private String estatusPlan;
    private String idPaquete;
    private String paquete;
    private String monto;
    private String nombreTitularSustituto;
}
