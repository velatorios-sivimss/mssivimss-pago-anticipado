package com.imss.sivimss.pagoanticipado.model.request;

import lombok.Data;

@Data
public class ActualizaPagoRequest {
private Integer idPago;
private String numeroAutorizacion;
private String folioAutorizacion;
private String nombreBanco;
private String fechaPago;
private String idTipoPago;
private String importe;
}
