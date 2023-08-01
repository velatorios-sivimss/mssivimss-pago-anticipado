package com.imss.sivimss.pagoanticipado.beans;

import com.imss.sivimss.pagoanticipado.util.AppConstantes;
import com.imss.sivimss.pagoanticipado.util.DatosRequest;
import com.imss.sivimss.pagoanticipado.util.QueryHelper;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;
import java.util.Map;

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
}
