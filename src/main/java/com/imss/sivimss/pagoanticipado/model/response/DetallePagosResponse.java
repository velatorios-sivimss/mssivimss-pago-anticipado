package com.imss.sivimss.pagoanticipado.model.response;

import lombok.Data;

@Data
public class DetallePagosResponse {
private String pagos;
private String fechaPago;
private String metodoPago;
private String numeroAutorizacion;
private String folioAutorizacion;
private String estatusPago;
private Integer idBitacoraPago;
private String nombreBanco;
private String velatorio;
}
