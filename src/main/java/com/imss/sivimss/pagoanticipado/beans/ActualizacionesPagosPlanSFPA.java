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
public class ActualizacionesPagosPlanSFPA {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActualizacionesPagosPlanSFPA.class);
    public DatosRequest actualizarEstatusVigentePlanSFPA(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVT_PLAN_SFPA");
        q.agregarParametroValues("ID_ESTATUS_PLAN_SFPA","2");
        q.addWhere("ID_PLAN_SFPA = " + idPlan);
        String query = q.obtenerQueryActualizar();
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest actualizarEstatusPagadoPlanSFPA(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVT_PLAN_SFPA");
        q.agregarParametroValues("ID_ESTATUS_PLAN_SFPA","4");
        q.addWhere("ID_PLAN_SFPA = " + idPlan);
        String query = q.obtenerQueryActualizar();
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }
    public DatosRequest actualizarEstatusGeneradoPlanSFPA(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVT_PLAN_SFPA");
        q.agregarParametroValues("ID_ESTATUS_PLAN_SFPA","1");
        q.addWhere("ID_PLAN_SFPA = " + idPlan);
        String query = q.obtenerQueryActualizar();
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest actualizarEstatusVigentePagoSFPA(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVC_PAGO_SFPA");
        q.agregarParametroValues("ID_ESTATUS_PAGO","1");
        q.addWhere("ID_PLAN_SFPA = " + idPlan);
        String query = q.obtenerQueryActualizar();
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest actualizarEstatusCerradoPagoSFPA(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVC_PAGO_SFPA");
        q.agregarParametroValues("ID_ESTATUS_PAGO","6");
        q.addWhere("ID_PLAN_SFPA = " + idPlan);
        String query = q.obtenerQueryActualizar();
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest actualizarMetodoPago(ActualizaPagoRequest request){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVC_BITACORA_PAGO_ANTICIPADO");
        if(!Objects.isNull(request.getFechaPago())){
        q.agregarParametroValues("FEC_PAGO","'" + request.getFechaPago() + "'");
        }
        if(!Objects.isNull(request.getNumeroAutorizacion())){
            q.agregarParametroValues("NUM_AUTORIZACION","'" + request.getNumeroAutorizacion()+ "'");
        }
        if(!Objects.isNull(request.getFolioAutorizacion())){
            q.agregarParametroValues("REF_FOLIO_AUTORIZACION","'" + request.getFolioAutorizacion()+ "'");
        }
        if(!Objects.isNull(request.getIdTipoPago())){
            q.agregarParametroValues("ID_METODO_PAGO","'" + request.getIdTipoPago()+ "'");
        }
        if(!Objects.isNull(request.getImporte())){
            q.agregarParametroValues("IMP_PAGO","'" + request.getImporte()+ "'");
        }
        q.agregarParametroValues("REF_BANCO", "'" + request.getNombreBanco()+ "'");
        q.addWhere("ID_BITACORA_PAGO = " + request.getIdPago());
        String query = q.obtenerQueryActualizar();
        log.info(query);
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest desactivarPago(String idPagoBitacora){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVC_PAGO_SFPA");
        q.agregarParametroValues("IND_ACTIVO","0");
        q.agregarParametroValues("ID_ESTATUS_PAGO","3");
        q.addWhere("ID_BITACORA_PAGO = " + idPagoBitacora);
        String query = q.obtenerQueryActualizar();
        log.info(query);
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest actualizarNuevoRestante(String idPagoBitacora, String nuevoRestante){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        final QueryHelper q = new QueryHelper("UPDATE SVC_BITACORA_PAGO_ANTICIPADO");
        q.agregarParametroValues("IMP_TOTAL_RESTANTE","'" + nuevoRestante + "'");
        q.addWhere("ID_BITACORA_PAGO = " + idPagoBitacora);
        String query = q.obtenerQueryActualizar();
        log.info(query);
        String encoded = DatatypeConverter.printBase64Binary(query.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

}
