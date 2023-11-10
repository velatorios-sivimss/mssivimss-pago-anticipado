package com.imss.sivimss.pagoanticipado.beans;

import com.imss.sivimss.pagoanticipado.util.QueryHelper;
import com.imss.sivimss.pagoanticipado.util.SelectQueryUtil;
import org.springframework.stereotype.Service;

@Service
public class PagosPlanSFPA {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PagosPlanSFPA.class);

    private String query;

    public String detallePagosSFPA() {
        return "SELECT pg.idPagoSFPA, CONCAT(CAST((@ROW := @ROW + 1) AS VARCHAR(255)),'/', (\r\n" + //
                "SELECT COUNT(pf.ID_PAGO_SFPA)\r\n" + //
                "FROM SVT_PAGO_SFPA pf\r\n" + //
                "WHERE pf.IND_ACTIVO = 1 AND pf.ID_PLAN_SFPA = pg.idPlanSFPA)) AS noPagos, \r\n" + //
                "pg.idPlanSFPA, pg.velatorio, DATE_FORMAT(pg.fechaParcialidad,'%d/%m/%Y') AS fechaParcialidad, \r\n" + //
                "pg.importeMensual, pg.estatusPago, pg.importePagado, \r\n" + //
                "CASE WHEN pg.importePagado < pg.importeMensual && pg.fechaParcialidad = CURDATE() THEN TRUE \r\n" + //
                "WHEN pg.importePagado = pg.importeMensual THEN FALSE\r\n" + //
                " ELSE FALSE END AS validaPago, \r\n" + //
                "pg.importePagado , pg.importeMensual ,\r\n" + //
                " pg.fechaParcialidad , CURDATE() ,\r\n" + //
                "CASE WHEN pg.idEstatus = 2 THEN 0 WHEN MONTH(pg.fechaParcialidad) = MONTH(CURDATE()) \r\n" + //
                "THEN (pg.importeFaltante + pg.importeMensual) - pg.importePagadoBitacora\r\n" + //
                " ELSE pg.importeMensual END AS importeAcumulado\r\n" + //
                "FROM (\r\n" + //
                "SELECT ps.ID_PAGO_SFPA AS idPagoSFPA, ps.ID_PLAN_SFPA AS idPlanSFPA,ps.ID_ESTATUS_PAGO AS idEstatus, v.DES_VELATORIO AS velatorio, ps.FEC_PARCIALIDAD AS fechaParcialidad, ps.IMP_MONTO_MENSUAL AS importeMensual, ep.DES_ESTATUS_PAGO_ANTICIPADO AS estatusPago, (\r\n"
                + //
                "SELECT\r\n" + //
                "(IFNULL(SUM(bpa.IMP_PAGO),0) + IFNULL(SUM(bpa.IMP_AUTORIZADO_VALE_PARITARIO),0))\r\n" + //
                "FROM SVC_BITACORA_PAGO_ANTICIPADO bpa\r\n" + //
                "WHERE bpa.IND_ACTIVO = 1 AND bpa.ID_PAGO_SFPA = ps.ID_PAGO_SFPA) AS importePagado, ps.IND_ACTIVO, (\r\n"
                + //
                "SELECT IFNULL(SUM(sps.IMP_MONTO_MENSUAL),0)\r\n" + //
                "FROM SVT_PAGO_SFPA sps\r\n" + //
                "WHERE sps.ID_ESTATUS_PAGO = 2 AND sps.IND_ACTIVO = 1 AND sps.FEC_PARCIALIDAD <= CURDATE() AND sps.ID_PLAN_SFPA = ps.ID_PLAN_SFPA) AS importeFaltante, (\r\n"
                + //
                "SELECT IFNULL(SUM(bpaa.IMP_PAGO),0)  + IFNULL(SUM(bpaa.IMP_AUTORIZADO_VALE_PARITARIO),0)\r\n" + //
                "FROM SVT_PAGO_SFPA sps\r\n" + //
                "JOIN SVC_BITACORA_PAGO_ANTICIPADO bpaa ON bpaa.ID_PAGO_SFPA= sps.ID_PAGO_SFPA\r\n" + //
                "WHERE sps.IND_ACTIVO = 1 AND sps.ID_PLAN_SFPA = ps.ID_PLAN_SFPA) AS importePagadoBitacora, ps.IMP_MONTO_MENSUAL\r\n"
                + //
                "FROM SVT_PAGO_SFPA ps\r\n" + //
                "JOIN SVT_PLAN_SFPA pls ON pls.ID_PLAN_SFPA = ps.ID_PLAN_SFPA\r\n" + //
                "JOIN SVC_VELATORIO v ON v.ID_VELATORIO = pls.ID_VELATORIO\r\n" + //
                "JOIN SVC_ESTATUS_PAGO_ANTICIPADO ep ON ep.ID_ESTATUS_PAGO_ANTICIPADO = ps.ID_ESTATUS_PAGO) AS pg, (\r\n"
                + //
                "SELECT @ROW := 0) r\r\n" + //
                "WHERE pg.idPlanSFPA = ? AND pg.IND_ACTIVO = 1";

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
        selectQuery.select(" IFNULL(ps.ID_PLAN_SFPA,0) AS idPlan, IFNULL(ps.NUM_FOLIO_PLAN_SFPA,'') AS folio," +
                " IFNULL(d.DES_DELEGACION,'') AS estado, IFNULL(p.REF_PAQUETE_NOMBRE,'') AS paquete," +
                "  CONCAT(IFNULL(pr.NOM_PERSONA,''),' '," +
                "   IFNULL(pr.NOM_PRIMER_APELLIDO,''),' ', IFNULL(pr.NOM_SEGUNDO_APELLIDO,'')) AS nombre," +
                " IFNULL(ps.IMP_PRECIO,0) AS precio," +
                " (IFNULL(ps.IMP_PRECIO,0) -  (IFNULL(SUM(bp.IMP_PAGO),0)+ IFNULL(SUM(bp.IMP_AUTORIZADO_VALE_PARITARIO),0))) AS costoRestante,"
                +
                "  IFNULL(SUM(bp.IMP_PAGO),0)+ IFNULL(SUM(bp.IMP_AUTORIZADO_VALE_PARITARIO),0) AS importePagado," +
                "   IFNULL(pr.REF_CORREO,'') AS correo")
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

    public String insertarPagoBitagoraSFPA() {

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
                "  NUM_VALE_PARITARIO ,   " +
                "  FEC_VALE_PARITARIO ,   " +
                "  IMP_AUTORIZADO_VALE_PARITARIO ,   " +
                "  FEC_ALTA )"
                + "VALUES (NULL,?,?,?,?,?,?,?,?,1,?,?,?,?,CURDATE())";
    }

    public String validaMontoPagoSFPA() {
        return "SELECT CAST(IFNULL(SUM(sps.IMP_MONTO_MENSUAL),0) AS DOUBLE) AS deuda , " +
                " 0.0 AS pagado, " +
                " 0.0 AS mensualidad " +
                " FROM SVT_PAGO_SFPA sps " +
                " WHERE sps.ID_ESTATUS_PAGO = 2  " +
                " AND sps.IND_ACTIVO = 1 " +
                " AND sps.FEC_PARCIALIDAD <= CURDATE() AND sps.ID_PLAN_SFPA = ? " +
                " UNION   " +
                " SELECT 0.0, " +
                " CAST(IFNULL(SUM(bpaa.IMP_PAGO),0) AS DOUBLE), " +
                " 0.0 " +
                " FROM SVT_PAGO_SFPA sps " +
                " JOIN SVC_BITACORA_PAGO_ANTICIPADO bpaa ON bpaa.ID_PAGO_SFPA= sps.ID_PAGO_SFPA " +
                " WHERE sps.IND_ACTIVO = 1 " +
                " AND sps.ID_PLAN_SFPA = ? " +
                " union " +
                " SELECT 0.0,0.0,  " +
                " CAST(sps.IMP_MONTO_MENSUAL AS DOUBLE)  " +
                " FROM SVT_PAGO_SFPA sps " +
                " WHERE sps.IND_ACTIVO = 1  " +
                " AND sps.ID_PLAN_SFPA = ? " +
                " AND sps.ID_PAGO_SFPA = ?";
    }

    public String actualizaEstatusPagoSFPA() {
        return " UPDATE SVT_PAGO_SFPA SET ID_ESTATUS_PAGO = ?," +
                " ID_USUARIO_MODIFICA = ?," +
                " FEC_ACTUALIZACION = CURDATE() " +
                " WHERE ID_PAGO_SFPA =?" +
                " AND ID_PLAN_SFPA = ?";
    }

    public String totalPagado() {
        return "SELECT ifnull(SUM(pa.IMP_PAGO),0)- IFNULL( psf.IMP_PRECIO,0) AS total" +
                "  FROM SVC_BITACORA_PAGO_ANTICIPADO pa" +
                " JOIN SVT_PAGO_SFPA ps ON ps.ID_PAGO_SFPA = pa.ID_PAGO_SFPA" +
                " JOIN SVT_PLAN_SFPA psf ON psf.ID_PLAN_SFPA = ps.ID_PLAN_SFPA" +
                " WHERE ps.ID_PLAN_SFPA = ?" +
                " AND pa.IND_ACTIVO= 1";
    }

    public String actualizaEstatusPlan() {
        return " UPDATE SVT_PLAN_SFPA SET ID_ESTATUS_PLAN_SFPA=?," +
                " ID_USUARIO_MODIFICA= ?," +
                " FEC_ACTUALIZACION=  CURDATE()" +
                "  WHERE ID_PLAN_SFPA = ?";
    }

    public String actualizarPagoBitagoraSFPA() {
        return " UPDATE  SVC_BITACORA_PAGO_ANTICIPADO SET" +
                " FEC_PAGO = ?," +
                " NUM_AUTORIZACION = ?," +
                " REF_FOLIO_AUTORIZACION = ?, " +
                " REF_BANCO = ?, " +
                " IMP_PAGO = ?, " +
                " ID_METODO_PAGO = ?, " +
                " FEC_ACTUALIZACION = CURDATE() , " +
                " ID_USUARIO_MODIFICA = ? " +
                " WHERE  ID_BITACORA_PAGO = ?" +
                " AND  ID_PAGO_SFPA = ?";
    }

}
