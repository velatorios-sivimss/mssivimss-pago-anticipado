package com.imss.sivimss.pagoanticipado.model.response;

import lombok.Data;

@Data
public class DetallePagosSFPAResponse {
    private String pagos;
    private String fechaPago;
    private String metodoPago;
    private String idMetodoPago;
    private String numeroAutorizacion;
    private String folioAutorizacion;
    private String estatusPago;
    private Integer idBitacoraPago;
    private String nombreBanco;
    private String velatorio;
    private String monto;
    private String noReciboPago;
}
