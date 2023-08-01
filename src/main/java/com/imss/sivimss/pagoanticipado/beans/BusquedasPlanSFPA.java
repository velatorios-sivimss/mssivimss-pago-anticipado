package com.imss.sivimss.pagoanticipado.beans;

import com.imss.sivimss.pagoanticipado.util.AppConstantes;
import com.imss.sivimss.pagoanticipado.util.DatosRequest;
import com.imss.sivimss.pagoanticipado.util.SelectQueryUtil;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;
import java.util.Map;
@Service
public class BusquedasPlanSFPA {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BusquedasPlanSFPA.class);

    public DatosRequest buscarPlanSFPA(String folio, String fechaInicio , String fechaFin, String idContratante, String idVelatorio) {
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        SelectQueryUtil query = new SelectQueryUtil();
        query.select("SP.FEC_INGRESO AS fecha", "SP.NUM_FOLIO_PLAN_SFPA AS folio", "SP.ID_TITULAR_SUBSTITUTO AS idTitularSustituto",
                        "SP.ID_TIPO_PAGO_MENSUAL AS idTipoPago", "TP.DES_TIPO_PAGO_MENSUAL AS tipoPagosMensuales" ,"SP.ID_ESTATUS_PLAN_SFPA AS idEstatusPlan",
                        "EST.DES_ESTATUS_PLAN_SFPA AS estatusPlan",
                        "SP.ID_PAQUETE AS idPaquete", "PAQ.DES_NOM_PAQUETE AS paquete", "PAQ.MON_PRECIO AS monto",
                        "CONCAT(PER.NOM_PERSONA, ' ', PER.NOM_PRIMER_APELLIDO, ' ', PER.NOM_SEGUNDO_APELLIDO) AS nombreTitularSustituto")
                .from("SVT_PLAN_SFPA SP")
                .leftJoin("SVT_PAQUETE PAQ", "SP.ID_PAQUETE = PAQ.ID_PAQUETE")
                .leftJoin("SVC_ESTATUS_PLAN_SFPA EST", "SP.ID_ESTATUS_PLAN_SFPA = EST.ID_ESTATUS_PLAN_SFPA")
                .leftJoin("SVC_TIPO_PAGO_MENSUAL TP","SP.ID_TIPO_PAGO_MENSUAL = TP.ID_TIPO_PAGO_MENSUAL")
                .leftJoin("SVC_CONTRATANTE CON","SP.ID_TITULAR_SUBSTITUTO = CON.ID_CONTRATANTE")
                .leftJoin("SVC_PERSONA PER","CON.ID_PERSONA = PER.ID_PERSONA")
                .where("SP.IND_ACTIVO = 1");
        if(!folio.equals("")){
            query.and("SP.NUM_FOLIO_PLAN_SFPA = '" + folio + "'");
        }
        if(!fechaInicio.equals("") && !fechaFin.equals("")){
            query.and("SP.FEC_INGRESO BETWEEN '" + fechaInicio + "' AND '" + fechaFin + "'");
        }
        if(!idVelatorio.equals("")){
            query.and("SP.ID_VELATORIO = " + idVelatorio);
        }
        if(!idContratante.equals("0")){
            query.and("SP.ID_TITULAR_SUBSTITUTO = " + idContratante);
        }
        String consulta = query.build();
        log.info(consulta);
        String encoded = DatatypeConverter.printBase64Binary(consulta.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest buscarIdContratante(String nombre, String primerApellido, String segundoApellido){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        SelectQueryUtil query = new SelectQueryUtil();
        query.select("SPS.NUM_FOLIO_PLAN_SFPA","SPS.ID_TITULAR_SUBSTITUTO","SC.ID_CONTRATANTE")
                .from("SVT_PLAN_SFPA SPS")
                .leftJoin("SVC_CONTRATANTE SC","SPS.ID_TITULAR_SUBSTITUTO = SC.ID_CONTRATANTE")
                .leftJoin("SVC_PERSONA SP","SC.ID_PERSONA = SP.ID_PERSONA")
                .where("SP.NOM_PERSONA = '" + nombre + "'")
                .and("SP.NOM_PRIMER_APELLIDO = '" + primerApellido + "'")
                .and("SP.NOM_SEGUNDO_APELLIDO = '" + segundoApellido + "'");
        String consulta = query.build();
        String encoded = DatatypeConverter.printBase64Binary(consulta.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest obtenerMetodosPago(){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        SelectQueryUtil query = new SelectQueryUtil();
        query.select("SMP.ID_METODO_PAGO AS idMetodoPago", "SMP.DESC_METODO_PAGO AS metodoPago")
                .from("SVC_METODO_PAGO SMP")
                .where("SMP.ID_METODO_PAGO IN(3,4,6,7)");
        String consulta = query.build();
        String encoded = DatatypeConverter.printBase64Binary(consulta.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest obtenerMontoPaquete(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        SelectQueryUtil query = new SelectQueryUtil();
        query.select("PAQ.MON_PRECIO AS importeTotal","SPS.ID_TIPO_PAGO_MENSUAL AS mesesPagar","TP.DES_TIPO_PAGO_MENSUAL as desMeses")
                .from ("SVT_PLAN_SFPA SPS")
                .leftJoin("SVT_PAQUETE PAQ","SPS.ID_PAQUETE  = PAQ.ID_PAQUETE")
                .leftJoin("SVC_TIPO_PAGO_MENSUAL TP","SPS.ID_TIPO_PAGO_MENSUAL = TP.ID_TIPO_PAGO_MENSUAL")
                .where("SPS.ID_PLAN_SFPA = " + idPlan);
        String consulta = query.build();
        String encoded = DatatypeConverter.printBase64Binary(consulta.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest obtenerNumeroPagosRealizados(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        SelectQueryUtil query = new SelectQueryUtil();
        query.select("COUNT(BP.ID_BITACORA_PAGO) AS pagosRealizados")
                .from ("SVT_PLAN_SFPA SPS")
                .leftJoin("SVC_BITACORA_PAGO_ANTICIPADO BP","SPS.ID_PLAN_SFPA = BP.ID_PLAN_SFPA")
                .where("SPS.ID_PLAN_SFPA = " + idPlan);
        String consulta = query.build();
        String encoded = DatatypeConverter.printBase64Binary(consulta.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }
    public DatosRequest obtenerRestante(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        SelectQueryUtil query = new SelectQueryUtil();
        query.select("SBP.DES_TOTAL_RESTANTE AS totalRestante")
                .from ("SVC_BITACORA_PAGO_ANTICIPADO SBP")
                .where("SBP.ID_BITACORA_PAGO = (SELECT MAX(SBP.ID_BITACORA_PAGO) FROM SVC_BITACORA_PAGO_ANTICIPADO SBP WHERE SBP.ID_PLAN_SFPA = " + idPlan + ")");
        String consulta = query.build();
        String encoded = DatatypeConverter.printBase64Binary(consulta.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }
    public DatosRequest obtenerInformacionRecibo(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        SelectQueryUtil query = new SelectQueryUtil();
        query.select("SPS.NUM_FOLIO_PLAN_SFPA AS numeroFolio","PM.DES_TIPO_PAGO_MENSUAL AS totalMensualidades","COUNT(BP.ID_BITACORA_PAGO) AS pagosRealizados",
                "CONCAT(PER.NOM_PERSONA, ' ', PER.NOM_PRIMER_APELLIDO, ' ', PER.NOM_SEGUNDO_APELLIDO) AS nombreContratante",
                "BP.DES_IMPORTE AS importe","PAQ.DES_NOM_PAQUETE AS nombrePaquete","SV.DES_VELATORIO AS velatorio")
                .from("SVT_PLAN_SFPA SPS")
                .leftJoin("SVC_TIPO_PAGO_MENSUAL PM","SPS.ID_TIPO_PAGO_MENSUAL = PM.ID_TIPO_PAGO_MENSUAL")
                .leftJoin("SVC_BITACORA_PAGO_ANTICIPADO BP","SPS.ID_PLAN_SFPA = BP.ID_PLAN_SFPA")
                .leftJoin("SVC_CONTRATANTE CON","SPS.ID_TITULAR_SUBSTITUTO = CON.ID_CONTRATANTE")
                .leftJoin("SVC_PERSONA PER", "CON.ID_PERSONA = PER.ID_PERSONA")
                .leftJoin("SVT_PAQUETE PAQ","SPS.ID_PAQUETE  = PAQ.ID_PAQUETE")
                .leftJoin("SVC_VELATORIO SV","SPS.ID_VELATORIO = SV.ID_VELATORIO")
                .where("SPS.ID_PLAN_SFPA = " + idPlan)
                .and("and BP.ID_BITACORA_PAGO = (SELECT MAX(SBP.ID_BITACORA_PAGO) FROM SVC_BITACORA_PAGO_ANTICIPADO SBP WHERE SBP.ID_PLAN_SFPA = " + idPlan +")");
        String consulta = query.build();
        log.info(consulta);
        String encoded = DatatypeConverter.printBase64Binary(consulta.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest obtenerDetallePlan(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        SelectQueryUtil query = new SelectQueryUtil();
        query.select("SPS.ID_PLAN_SFPA AS idPlan","SPS.NUM_FOLIO_PLAN_SFPA AS numFolio","TPM.DES_TIPO_PAGO_MENSUAL AS desNumeroPagos",
                        "PAQ.DES_NOM_PAQUETE AS nombrePaquete","CONCAT(SP.NOM_PERSONA , ' ', SP.NOM_PRIMER_APELLIDO, ' ', SP.NOM_SEGUNDO_APELLIDO) AS contratanteSubstituto"
                ,"SP.DES_CORREO AS correo","SD.DES_ESTADO  AS estado","VEL.DES_VELATORIO AS velatorio","EST.DES_ESTATUS_PLAN_SFPA AS estatusPlan")
                .from("SVT_PLAN_SFPA SPS")
                .leftJoin("SVC_TIPO_PAGO_MENSUAL TPM","SPS.ID_TIPO_PAGO_MENSUAL = TPM.ID_TIPO_PAGO_MENSUAL")
                .leftJoin("SVT_PAQUETE PAQ","SPS.ID_PAQUETE = PAQ.ID_PAQUETE")
                .leftJoin("SVC_CONTRATANTE CON","SPS.ID_TITULAR_SUBSTITUTO = CON.ID_CONTRATANTE")
                .leftJoin("SVC_PERSONA SP","CON.ID_PERSONA = SP.ID_PERSONA")
                .leftJoin("SVC_VELATORIO VEL","SPS.ID_VELATORIO = VEL.ID_VELATORIO")
                .leftJoin("SVC_ESTATUS_PLAN_SFPA EST","SPS.ID_ESTATUS_PLAN_SFPA = EST.ID_ESTATUS_PLAN_SFPA")
                .leftJoin("SVT_DOMICILIO SD","CON.ID_DOMICILIO = SD.ID_DOMICILIO")
                .where("SPS.ID_PLAN_SFPA = " + idPlan);
        String consulta = query.build();
        String encoded = DatatypeConverter.printBase64Binary(consulta.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

    public DatosRequest obtenerDetallePagos(String idPlan){
        DatosRequest dr = new DatosRequest();
        Map<String, Object> parametro = new HashMap<>();
        SelectQueryUtil query = new SelectQueryUtil();
        query.select("CONCAT(row_number() over (order by sbp.ID_BITACORA_PAGO) ,'/',tpm.DES_TIPO_PAGO_MENSUAL) as pagos",
                        "SBP.FEC_FECHA_PAGO AS fechaPago","MP.DESC_METODO_PAGO AS metodoPago",
                        "SBP.NUM_AUTORIZACION AS numeroAutorizacion","sbp.DES_FOLIO_AUTORIZACION as folioAutorizacion",
                        "SBP.DES_IMPORTE AS importePago","'Pagado' AS estatusPago")
                .from("SVC_PAGO_SFPA SPS")
                .leftJoin("SVC_BITACORA_PAGO_ANTICIPADO SBP","SPS.ID_BITACORA_PAGO = SBP.ID_BITACORA_PAGO")
                .leftJoin("SVC_TIPO_PAGO_MENSUAL TPM","SPS.ID_TIPO_PAGO_MENSUAL = TPM.ID_TIPO_PAGO_MENSUAL")
                .leftJoin("SVC_METODO_PAGO MP","SPS.ID_METODO_PAGO = MP.ID_METODO_PAGO")
                .where("SPS.ID_PLAN_SFPA = " + idPlan);
        String consulta = query.build();
        String encoded = DatatypeConverter.printBase64Binary(consulta.getBytes());
        parametro.put(AppConstantes.QUERY, encoded);
        dr.setDatos(parametro);
        return dr;
    }

}