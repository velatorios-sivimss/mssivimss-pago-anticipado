package com.imss.sivimss.pagoanticipado.service.impl;

import com.imss.sivimss.pagoanticipado.service.PagoAnticipadoSFPAService;
import com.imss.sivimss.pagoanticipado.util.DatosRequest;
import com.imss.sivimss.pagoanticipado.util.Response;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PagoAnticipadoSFPAImpl implements PagoAnticipadoSFPAService {
    @Override
    public Response<?> buscarPlanSFPA(DatosRequest request, Authentication authentication) throws IOException {
        return null;
    }
}
