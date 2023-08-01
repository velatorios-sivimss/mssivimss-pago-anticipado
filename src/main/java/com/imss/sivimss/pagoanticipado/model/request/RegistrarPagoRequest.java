package com.imss.sivimss.pagoanticipado.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RegistrarPagoRequest {
    @JsonProperty
    private String idPlan;
    @JsonProperty
    private String idTipoPago;
    @JsonProperty
    private String fechaPago;
    @JsonProperty
    private String numeroAutorizacion;
    @JsonProperty
    private String folioAutorizacion;
    @JsonProperty
    private String nombreBanco;
    @JsonProperty
    private String importe;
}
