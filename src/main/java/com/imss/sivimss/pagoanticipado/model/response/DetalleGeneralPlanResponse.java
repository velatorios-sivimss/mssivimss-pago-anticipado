package com.imss.sivimss.pagoanticipado.model.response;

import lombok.Data;

import java.util.List;

@Data
public class DetalleGeneralPlanResponse {
    private DetallePlanResponse detallePlan;
    private List<DetallePagosResponse> pagos;
}
