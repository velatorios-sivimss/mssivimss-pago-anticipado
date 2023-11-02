package com.imss.sivimss.pagoanticipado.beans;

import com.imss.sivimss.pagoanticipado.model.request.ActualizaPagoRequest;
import com.imss.sivimss.pagoanticipado.util.AppConstantes;
import com.imss.sivimss.pagoanticipado.util.DatosRequest;
import com.imss.sivimss.pagoanticipado.util.QueryHelper;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class PagosPlanSFPA {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PagosPlanSFPA.class);

    private String query;
    
    public String detallePagosSFPA() {
        return "SELECT pg.ID_PAGO_SFPA AS idPagoSFPA, " +
                " pg.noPagos, pg.ID_PLAN_SFPA AS idPlanSFPA, " +
                " pg.DES_VELATORIO AS velatorio,  " +
                " DATE_FORMAT( pg.FEC_PARCIALIDAD,'%d/%m/%Y') AS fechaParcialidad, " +
                " FORMAT(pg.IMP_MONTO_MENSUAL,2) AS importeMensual, " +
                " pg.DES_ESTATUS_PAGO_ANTICIPADO AS estatusPago, " +
                " pg.importePagado, " +
                " CASE WHEN  PG.importePagado < pg.IMP_MONTO_MENSUAL THEN TRUE  " +
                " WHEN  PG.importePagado = pg.IMP_MONTO_MENSUAL THEN FALSE " +
                " ELSE FALSE " +
                " END AS validaPago, " +
                " case when pg.FEC_PARCIALIDAD = CURDATE() && pg.importeFaltante =0  then FORMAT(pg.IMP_MONTO_MENSUAL,2) "
                +
                " when pg.FEC_PARCIALIDAD = CURDATE()   then FORMAT(pg.importeFaltante,2) " +
                " ELSE FORMAT(pg.IMP_MONTO_MENSUAL,2) " +
                " END AS importeAcumulado " +
                " FROM ( " +
                " SELECT ps.ID_PAGO_SFPA ,CONCAT(CAST((@row := @row + 1) AS VARCHAR(255)),'/', " +
                " ( select COUNT(pf.ID_PAGO_SFPA) FROM SVT_PAGO_SFPA pf " +
                " WHERE  pf.IND_ACTIVO = 1 " +
                " AND pf.ID_PLAN_SFPA =ps.ID_PLAN_SFPA)) AS noPagos, " +
                " ps.ID_PLAN_SFPA , " +
                " v.DES_VELATORIO  , " +
                " ps.FEC_PARCIALIDAD,  " +
                " ps.IMP_MONTO_MENSUAL, " +
                " ep.DES_ESTATUS_PAGO_ANTICIPADO  , " +
                " (SELECT  ifnull(SUM(bpa.IMP_PAGO),0) FROM  svc_bitacora_pago_anticipado bpa " +
                " WHERE bpa.ID_PAGO_SFPA= ps.ID_PAGO_SFPA ) as importePagado, " +
                " ps.IND_ACTIVO, " +
                " (SELECT  ifnull(SUM(sps.IMP_MONTO_MENSUAL),0)  " +
                " FROM SVT_PAGO_SFPA sps " +
                " WHERE sps.ID_ESTATUS_PAGO= 2 " +
                " AND sps.IND_ACTIVO = 1 " +
                " AND sps.FEC_PARCIALIDAD = CURDATE() " +
                " AND sps.ID_PLAN_SFPA = ps.ID_PLAN_SFPA) AS importeFaltante " +
                " from SVT_PAGO_SFPA ps " +
                " JOIN SVT_PLAN_SFPA pls ON pls.ID_PLAN_SFPA = ps.ID_PLAN_SFPA " +
                " JOIN SVC_VELATORIO  v ON v.ID_VELATORIO = pls.ID_VELATORIO " +
                " JOIN SVC_ESTATUS_PAGO_ANTICIPADO ep ON ep.ID_ESTATUS_PAGO_ANTICIPADO = ps.ID_ESTATUS_PAGO, " +
                " (SELECT @row := 0) r " +
                " ) AS pg " +
                " WHERE pg.ID_PLAN_SFPA = 112 " +
                " AND pg.IND_ACTIVO = 1 ";

    }

    public DatosRequest actualizarMetodoPago(ActualizaPagoRequest request) {
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVC_BITACORA_PAGO_ANTICIPADO");
        if (!Objects.isNull(request.getFechaPago())) {
            q.agregarParametroValues("FEC_PAGO", "'" + request.getFechaPago() + "'");
        }
        if (!Objects.isNull(request.getNumeroAutorizacion())) {
            q.agregarParametroValues("NUM_AUTORIZACION", "'" + request.getNumeroAutorizacion() + "'");
        }
        if (!Objects.isNull(request.getFolioAutorizacion())) {
            q.agregarParametroValues("REF_FOLIO_AUTORIZACION", "'" + request.getFolioAutorizacion() + "'");
        }
        if (!Objects.isNull(request.getIdTipoPago())) {
            q.agregarParametroValues("ID_METODO_PAGO", "'" + request.getIdTipoPago() + "'");
        }
        if (!Objects.isNull(request.getImporte())) {
            q.agregarParametroValues("IMP_PAGO", "'" + request.getImporte() + "'");
        }
        q.agregarParametroValues("REF_BANCO", "'" + request.getNombreBanco() + "'");
        q.addWhere("ID_BITACORA_PAGO = " + request.getIdPago());
        String query = q.obtenerQueryActualizar();
        log.info(query);
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest desactivarPago(String idPagoBitacora) {
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVC_PAGO_SFPA");
        q.agregarParametroValues("IND_ACTIVO", "0");
        q.agregarParametroValues("ID_ESTATUS_PAGO", "3");
        q.addWhere("ID_BITACORA_PAGO = " + idPagoBitacora);
        String query = q.obtenerQueryActualizar();
        log.info(query);
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest insertarPagoAnticipado(String idPagoBitacora) {
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVC_PAGO_SFPA");
        q.agregarParametroValues("IND_ACTIVO", "0");
        q.agregarParametroValues("ID_ESTATUS_PAGO", "3");
        q.addWhere("ID_BITACORA_PAGO = " + idPagoBitacora);
        String query = q.obtenerQueryActualizar();
        log.info(query);
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest actualizarNuevoRestante(String idPagoBitacora, String nuevoRestante) {
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVC_BITACORA_PAGO_ANTICIPADO");
        q.agregarParametroValues("IMP_TOTAL_RESTANTE", "'" + nuevoRestante + "'");
        q.addWhere("ID_BITACORA_PAGO = " + idPagoBitacora);
        String query = q.obtenerQueryActualizar();
        log.info(query);
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }
    
    public String obtenerDetalleBitacoraPago() {
    	
    	return query;
    }

}
