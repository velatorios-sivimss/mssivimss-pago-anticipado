package com.imss.sivimss.pagoanticipado.service;

import com.imss.sivimss.pagoanticipado.util.DatosRequest;
import com.imss.sivimss.pagoanticipado.util.Response;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public interface PagoAnticipadoSFPAService {
    Response<?> buscarPlanSFPA(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> buscarFolios(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> metodosPago(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> generarPago(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> verDetallePagos(DatosRequest request, Authentication authentication) throws IOException, SQLException;

    Response<?> actualizarPago(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> desactivarPago(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> generarPDF(DatosRequest request, Authentication authentication) throws IOException;

    Response<?> descargarDocumento(DatosRequest request, Authentication authentication)
            throws IOException, ParseException;

    Response<?> descargarReportePA(DatosRequest request, Authentication authentication)
            throws IOException, ParseException;

    Response<?> bitacoraDetallePagos(DatosRequest request, Authentication authentication) throws IOException, SQLException;

}