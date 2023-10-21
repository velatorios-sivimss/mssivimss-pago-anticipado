package com.imss.sivimss.pagoanticipado.beans;

import com.imss.sivimss.pagoanticipado.model.request.RegistrarPagoRequest;
import com.imss.sivimss.pagoanticipado.util.AppConstantes;
import com.imss.sivimss.pagoanticipado.util.DatosRequest;
import com.imss.sivimss.pagoanticipado.util.QueryHelper;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class InsercionesPagosSFPA {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InsercionesPagosSFPA.class);

    public DatosRequest insertarBitacoraPago(RegistrarPagoRequest registrarPagoRequest, String montoTotal, String montoInicial, String mesesPagar, String desMeses, String usuario) {
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        Double montoRestante = (Double.valueOf(montoTotal) - Double.valueOf(registrarPagoRequest.getImporte()));
        String fecPago = Objects.isNull(registrarPagoRequest.getFechaPago()) ? "" : "'" + registrarPagoRequest.getFechaPago() + "'";
        final QueryHelper queryHelper = new QueryHelper("INSERT SVC_BITACORA_PAGO_ANTICIPADO");
        queryHelper.agregarParametroValues("ID_PLAN_SFPA", String.valueOf(registrarPagoRequest.getIdPlan()));
        queryHelper.agregarParametroValues("FEC_PAGO", fecPago);
        queryHelper.agregarParametroValues("NUM_AUTORIZACION", setValor(registrarPagoRequest.getNumeroAutorizacion()));
        queryHelper.agregarParametroValues("REF_BANCO", "'" + registrarPagoRequest.getNombreBanco() + "'");
        queryHelper.agregarParametroValues("IMP_PAGO", setValor(registrarPagoRequest.getImporte()));
        queryHelper.agregarParametroValues("IMP_TOTAL_RESTANTE", "'" + montoRestante + "'");
        queryHelper.agregarParametroValues("ID_METODO_PAGO", "'" + registrarPagoRequest.getIdTipoPago() + "'");
        queryHelper.agregarParametroValues("ID_USUARIO_ALTA", "'" + usuario + "'");
        String qr = queryHelper.obtenerQueryInsertar() + " -- " + insertarPago(montoInicial, registrarPagoRequest.getIdTipoPago(), usuario, String.valueOf(registrarPagoRequest.getIdPlan()), mesesPagar, desMeses);
        String encoded = DatatypeConverter.printBase64Binary(qr.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        parametro.put("separador", "--");
        parametro.put("replace", "idTabla");
        dr.setDatos(parametro);
        return dr;
    }

    public String insertarPago(String montoTotalInicial, String idTipoPago, String usuario, String idPlan, String mesesPagar, String desMeses) {
        Double montoMensual = Double.valueOf(montoTotalInicial) / Integer.valueOf(desMeses);
        final QueryHelper q = new QueryHelper("INSERT INTO SVC_PAGO_SFPA");
        q.agregarParametroValues("ID_PLAN_SFPA", "'" + idPlan + "'");
        q.agregarParametroValues("ID_FLUJO_PAGOS", "'4'");
        q.agregarParametroValues("ID_TIPO_PAGO_MENSUAL", mesesPagar);
        q.agregarParametroValues("IMP_TOTAL", "'" + montoTotalInicial + "'");
        q.agregarParametroValues("IMP_MONTO_MENSUAL", "'" + montoMensual + "'");
        q.agregarParametroValues("ID_METODO_PAGO", "'" + idTipoPago + "'");
        q.agregarParametroValues("ID_USUARIO_ALTA", "'" + usuario + "'");
        q.agregarParametroValues("ID_BITACORA_PAGO", "idTabla");
        q.agregarParametroValues("ID_ESTATUS_PAGO", "1");
        return q.obtenerQueryInsertar();
    }

    private String setValor(String valor) {
        if (valor == null || valor.equals("")) {
            return "NULL";
        } else {
            return "'" + valor + "'";
        }
    }
}
