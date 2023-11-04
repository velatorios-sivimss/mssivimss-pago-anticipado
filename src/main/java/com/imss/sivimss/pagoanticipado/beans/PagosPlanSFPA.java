package com.imss.sivimss.pagoanticipado.beans;

import com.imss.sivimss.pagoanticipado.util.QueryHelper;
import com.imss.sivimss.pagoanticipado.util.SelectQueryUtil;
import org.springframework.stereotype.Service;

@Service
public class PagosPlanSFPA {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PagosPlanSFPA.class);

    private String query;

    public String detallePagosSFPA() {
        return "SELECT pg.idPagoSFPA,  " +
                " CONCAT(CAST((@ROW := @ROW + 1) AS VARCHAR(255)),'/', (  " +
                " SELECT COUNT(pf.ID_PAGO_SFPA)  " +
                " FROM SVT_PAGO_SFPA pf  " +
                " WHERE pf.IND_ACTIVO = 1 AND pf.ID_PLAN_SFPA = pg.idPlanSFPA )) AS noPagos,  " +
                " pg.idPlanSFPA, pg.velatorio,  " +
                " DATE_FORMAT(pg.fechaParcialidad,'%d/%m/%Y') AS fechaParcialidad,  " +
                " pg.importeMensual, pg.estatusPago, pg.importePagado,  " +
                " CASE WHEN pg.importePagado < pg.importeMensual && pg.fechaParcialidad = CURDATE() THEN TRUE   " +
                " WHEN pg.importePagado = pg.importeMensual THEN FALSE   " +
                " ELSE FALSE END AS validaPago,   " +
                " CASE WHEN pg.idEstatus = 2 THEN 0  " +
                " WHEN MONTH(pg.fechaParcialidad) = MONTH(CURDATE())   " +
                " THEN (pg.importeFaltante +  pg.importeMensual)  - pg.importePagadoBitacora  " +
                " ELSE pg.importeMensual END AS importeAcumulado   " +
                " FROM (  " +
                " SELECT ps.ID_PAGO_SFPA AS idPagoSFPA,  ps.ID_PLAN_SFPA AS   " +
                " idPlanSFPA,ps.ID_ESTATUS_PAGO AS idEstatus,  v.DES_VELATORIO AS velatorio, ps.FEC_PARCIALIDAD AS fechaParcialidad,  "
                +
                " ps.IMP_MONTO_MENSUAL AS importeMensual, ep.DES_ESTATUS_PAGO_ANTICIPADO AS estatusPago, (  " +
                " SELECT IFNULL(SUM(bpa.IMP_PAGO),0)  " +
                " FROM SVC_BITACORA_PAGO_ANTICIPADO bpa  " +
                " WHERE bpa.IND_ACTIVO = 1 AND bpa.ID_PAGO_SFPA = ps.ID_PAGO_SFPA  ) AS importePagado,   " +
                " ps.IND_ACTIVO, (  " +
                " SELECT IFNULL(SUM(sps.IMP_MONTO_MENSUAL),0)  " +
                " FROM SVT_PAGO_SFPA sps  " +
                " WHERE sps.ID_ESTATUS_PAGO = 2 and sps.IND_ACTIVO = 1   " +
                " AND sps.FEC_PARCIALIDAD <= CURDATE()   " +
                " AND sps.ID_PLAN_SFPA = ps.ID_PLAN_SFPA) AS importeFaltante,  " +
                " (  " +
                " SELECT IFNULL(SUM(bpaa.IMP_PAGO),0)  " +
                " FROM SVT_PAGO_SFPA sps  " +
                " JOIN SVC_BITACORA_PAGO_ANTICIPADO bpaa ON bpaa.ID_PAGO_SFPA= sps.ID_PAGO_SFPA  " +
                " WHERE sps.IND_ACTIVO = 1    " +
                " AND sps.ID_PLAN_SFPA = ps.ID_PLAN_SFPA  " +
                " ) AS importePagadoBitacora,   " +
                " ps.IMP_MONTO_MENSUAL  " +
                " FROM SVT_PAGO_SFPA ps  " +
                " JOIN SVT_PLAN_SFPA pls ON pls.ID_PLAN_SFPA = ps.ID_PLAN_SFPA  " +
                " JOIN SVC_VELATORIO v ON v.ID_VELATORIO = pls.ID_VELATORIO  " +
                " JOIN SVC_ESTATUS_PAGO_ANTICIPADO ep ON ep.ID_ESTATUS_PAGO_ANTICIPADO = ps.ID_ESTATUS_PAGO  " +
                " ) AS pg,  " +
                " (  " +
                " SELECT @ROW := 0) r  " +
                " WHERE pg.idPlanSFPA = ? AND pg.IND_ACTIVO = 1  ";
    }

    public String obtenerDetalleBitacoraPago() {

        SelectQueryUtil selectQueryUtil = new SelectQueryUtil();
        SelectQueryUtil selectQuery = new SelectQueryUtil();
        selectQueryUtil
                .select("SBPA.ID_BITACORA_PAGO AS idBitacora",
                        "SBPA .FEC_PAGO AS fechaPago",
                        "SBPA .IMP_PAGO AS importePago",
                        "SBPA.ID_METODO_PAGO as idMetodoPago",
                        "SMP.DES_METODO_PAGO AS desMetodoPago",
                        "SBPA.NUM_AUTORIZACION AS numeroAutorizacion",
                        "SBPA.REF_FOLIO_AUTORIZACION AS folioAutorizacion",
                        "SBPA.REF_BANCO AS referenciaBancaria",
                        "SBPA.NUM_VALE_PARITARIO AS numeroValeParitario",
                        "SBPA.FEC_VALE_PARITARIO AS fechaValeParitario",
                        "SBPA.IMP_AUTORIZADO_VALE_PARITARIO AS importeValeParitario",
                        "CASE WHEN SBPA.IND_ACTIVO = 1 THEN 'Pagado'" +
                                "ELSE 'Cancelado' END AS estatus")
                .from("SVC_BITACORA_PAGO_ANTICIPADO SBPA ")
                .innerJoin("SVT_PAGO_SFPA SPS", "SBPA.ID_PAGO_SFPA = SPS.ID_PAGO_SFPA")
                .innerJoin("SVC_METODO_PAGO SMP", "SBPA.ID_METODO_PAGO = SMP.ID_METODO_PAGO")
                .innerJoin("SVC_ESTATUS_PAGO_ANTICIPADO SPA", "SPS.ID_ESTATUS_PAGO = SPA.ID_ESTATUS_PAGO_ANTICIPADO")
                .where("SPS.ID_PAGO_SFPA=? GROUP BY SBPA.ID_BITACORA_PAGO ORDER BY SBPA.FEC_PAGO, SBPA.IND_ACTIVO DESC");

        query = selectQuery.select("FORMAT((@I:= @I+1),0) AS numeroPago, TBL1.*")
                .from("(" + selectQueryUtil.build() + ") TBL1,(SELECT @I:=0) C").build();
        log.info(query);
        return query;

    }

    public String desactivarPagoBitacora() {
        final QueryHelper q = new QueryHelper("UPDATE SVC_BITACORA_PAGO_ANTICIPADO");
        q.agregarParametroValues("IND_ACTIVO", "0");
        q.addColumn("FEC_BAJA", "CURRENT_DATE()");
        q.addColumn("ID_USUARIO_BAJA", "?");
        q.addWhere("ID_BITACORA_PAGO = ?");
        query = q.obtenerQueryActualizar();
        log.info(query);
        return query;
    }

    public String detallePlan() {
        SelectQueryUtil selectQuery = new SelectQueryUtil();
        selectQuery.select(" IFNULL( ps.ID_PLAN_SFPA,0) AS idPlan, IFNULL( ps.NUM_FOLIO_PLAN_SFPA,'') AS folio, " +
                "IFNULL(d.DES_DELEGACION,'') AS delegacion, IFNULL( p.REF_PAQUETE_NOMBRE,'') AS paquete," +
                " concat(ifnull(pr.NOM_PERSONA,''),' ',IFNULL(pr.NOM_PRIMER_APELLIDO,''),' ',IFNULL(pr.NOM_SEGUNDO_APELLIDO,'')) AS nombre,"
                +
                "IFNULL( ps.IMP_PRECIO,0) AS precio," +
                "(ifnull(ps.IMP_PRECIO,0) - ifnull(SUM(bp.IMP_PAGO),0)) AS costoRestante")
                .from("SVT_PLAN_SFPA ps ")
                .innerJoin("SVC_VELATORIO v", "v.ID_VELATORIO = ps.ID_VELATORIO")
                .innerJoin("SVC_DELEGACION   d", "d.ID_DELEGACION = v.ID_DELEGACION")
                .innerJoin("SVT_PAQUETE  p", "p.ID_PAQUETE = ps.ID_PAQUETE")
                .innerJoin("SVC_CONTRATANTE  c", "c.ID_CONTRATANTE = ps.ID_TITULAR")
                .innerJoin("SVC_PERSONA    pr", "pr.ID_PERSONA = c.ID_PERSONA")
                .innerJoin("SVT_PAGO_SFPA   psf", "psf.ID_PLAN_SFPA = ps.ID_PLAN_SFPA")
                .and("PSF.IND_ACTIVO = 1")
                .innerJoin("SVC_BITACORA_PAGO_ANTICIPADO  bp", "bp.ID_PAGO_SFPA = psf.ID_PAGO_SFPA")
                .and("bp.IND_ACTIVO = 1")
                .where(" PS.ID_PLAN_SFPA = ? ");

        query = selectQuery.build();
        return query;
    }

    public String insertarPagoBitagora() {

        return "INSERT INTO SVC_BITACORA_PAGO_ANTICIPADO"
                + "( ID_BITACORA_PAGO ,   " +
                " ID_PAGO_SFPA ,  " +
                "  IND_TIPO_PAGO , " +
                "  FEC_PAGO ,   " +
                "  NUM_AUTORIZACION ,   " +
                "  REF_FOLIO_AUTORIZACION ,   " +
                "  REF_BANCO ,   " +
                "  IMP_PAGO ,   " +
                "  ID_METODO_PAGO ,  " +
                "  IND_ACTIVO ,   " +
                "  ID_USUARIO_ALTA ,   " +
                "  FEC_ALTA )"
                + "VALUES (NULL,?,?,?,?,?,?,?,1,?,CURDATE())";
    }

}
