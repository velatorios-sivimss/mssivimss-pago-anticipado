package com.imss.sivimss.pagoanticipado.service;

import com.imss.sivimss.pagoanticipado.util.DatosRequest;
import com.imss.sivimss.pagoanticipado.util.Response;
import org.springframework.security.core.Authentication;

import java.io.IOException;

public interface PagoAnticipadoSFPAService {
Response<?> buscarPlanSFPA(DatosRequest request, Authentication authentication) throws IOException;
}
