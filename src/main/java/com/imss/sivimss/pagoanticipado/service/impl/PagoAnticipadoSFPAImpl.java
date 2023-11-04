package com.imss.sivimss.pagoanticipado.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.imss.sivimss.pagoanticipado.beans.ActualizacionesPagosPlanSFPA;
import com.imss.sivimss.pagoanticipado.beans.BusquedasPlanSFPA;
import com.imss.sivimss.pagoanticipado.beans.InsercionesPagosSFPA;
import com.imss.sivimss.pagoanticipado.beans.PagosPlanSFPA;
import com.imss.sivimss.pagoanticipado.model.response.ReciboPdfResponse;
import com.imss.sivimss.pagoanticipado.service.PagoAnticipadoSFPAService;
import com.imss.sivimss.pagoanticipado.util.*;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import com.imss.sivimss.pagoanticipado.model.request.ActualizaPagoRequest;
import com.imss.sivimss.pagoanticipado.model.request.BusquedaRequest;
import com.imss.sivimss.pagoanticipado.model.request.ReciboPDFRequest;
import com.imss.sivimss.pagoanticipado.model.request.ReportePaDto;
import com.imss.sivimss.pagoanticipado.model.request.ReporteRequest;
import com.imss.sivimss.pagoanticipado.model.request.UsuarioDto;

@Service
public class PagoAnticipadoSFPAImpl implements PagoAnticipadoSFPAService {

    @Value("${endpoints.mod-catalogos}")
    private String consultas;
    @Value("${endpoints.ms-reportes}")
    private String urlReportes;

    @Value("${data.msit_REPORTE_PA}")
    private String reportePa;

    @Autowired
    private ProviderServiceRestTemplate providerRestTemplate;
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private Database database;

    private ResultSet rs;

    private Connection connection;

    private Statement statement;

    private PreparedStatement preparedStatement;

    @Autowired
    BusquedasPlanSFPA bean = new BusquedasPlanSFPA();
    @Autowired
    InsercionesPagosSFPA beanInserta = new InsercionesPagosSFPA();
    @Autowired
    ActualizacionesPagosPlanSFPA beanActualiza = new ActualizacionesPagosPlanSFPA();
    @Autowired
    PagosPlanSFPA pagosPlanSFPA = new PagosPlanSFPA();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PagoAnticipadoSFPAImpl.class);
    JsonParser jsonParser = new JsonParser();
    Gson json = new Gson();

    @Autowired
    private LogUtil logUtil;

    @Override
    public Response<?> buscarPlanSFPA(DatosRequest request, Authentication authentication) throws IOException {
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        BusquedaRequest busquedaRequest = json.fromJson(datosJson, BusquedaRequest.class);
        String folio = validaNull(busquedaRequest.getFolio());
        String fechaInicio = validaNull(busquedaRequest.getFechaInicio());
        String fechaFin = validaNull(busquedaRequest.getFechaFin());
        String nombreTitularSustituto = validaNull(busquedaRequest.getNombreTitularSustituto());
        String idVelatorio = validaNull(busquedaRequest.getIdVelatorio());
        return providerRestTemplate.consumirServicio(
                bean.buscarPlanSFPA(folio, fechaInicio, fechaFin,
                        buscarIdContratante(nombreTitularSustituto, authentication).toString(), idVelatorio).getDatos(),
                consultas + "/paginado", authentication);
    }

    @Override
    public Response<?> buscarFolios(DatosRequest request, Authentication authentication) throws IOException {
        JsonObject jsonObj = JsonParser.parseString((String) request.getDatos().get(AppConstantes.DATOS))
                .getAsJsonObject();
        String cadena = jsonObj.get("cadena").getAsString();
        return providerRestTemplate.consumirServicio(bean.buscarFolioAutoRellenable(cadena).getDatos(),
                consultas + "/consulta", authentication);
    }

    @Override
    public Response<?> metodosPago(DatosRequest request, Authentication authentication) throws IOException {
        return providerRestTemplate.consumirServicio(bean.obtenerMetodosPago().getDatos(), consultas + "/consulta",
                authentication);
    }

    @Override
    public Response<Object> generarPago(DatosRequest request, Authentication authentication)
            throws SQLException, IOException {
        UsuarioDto usuarioDto = json.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        Integer idUsuario = usuarioDto.getIdUsuario();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode datos = mapper.readTree(request.getDatos().get(AppConstantes.DATOS)
                    .toString());
            Integer idPagoSFPA = datos.get("idPagoSFPA").asInt();
            String fechaPago = datos.get("fechaPago").asText();
            String numeroAutorizacion = datos.get("numeroAutorizacion").asText();
            String folioAutorizacion = datos.get("folioAutorizacion").asText();
            String nombreBanco = datos.get("nombreBanco").asText();
            Double importe = datos.get("importe").doubleValue();
            Integer idMetodoPago = datos.get("idMetodoPago").asInt();
            log.info("id", idMetodoPago);

        } catch (Exception e) {
            log.error(AppConstantes.ERROR_QUERY);
            log.error(e.getMessage());
            logUtil.crearArchivoLog(Level.WARNING.toString(), this.getClass().getSimpleName(),
                    this.getClass().getPackage().toString(),
                    AppConstantes.ERROR_LOG_QUERY + AppConstantes.ERROR_CONSULTAR, AppConstantes.CONSULTA,
                    authentication);

            return new Response<>(true, 500, AppConstantes.OCURRIO_ERROR_GENERICO, e.getMessage());

        } finally {

            if (connection != null) {
                connection.close();
            }

            if (rs != null) {
                rs.close();
            }

        }

        return new Response<>(false, 200, AppConstantes.EXITO, null);

    }

    @Override
    public Response<Object> verDetallePagos(DatosRequest request, Authentication authentication)
            throws SQLException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Integer idPlan = 0;
        List<Object> list = new ArrayList<>();
        ResultSet rs2 = null;
        HashMap<String, Object> salida = new HashMap<>();
        try {

            JsonNode datos = mapper.readTree(request.getDatos().get(AppConstantes.DATOS)
                    .toString());
            idPlan = datos.get("idPlan").asInt();
            String consulta = pagosPlanSFPA.detallePagosSFPA();
            connection = database.getConnection();
            log.info("query  {}", consulta);

            Object datosGenerales = traeDatosGenerales(connection, idPlan);
            salida.put("datosGenerales", datosGenerales);

            preparedStatement = connection.prepareStatement(consulta, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, idPlan);

            rs2 = preparedStatement.executeQuery();
            rs = preparedStatement.executeQuery();
            int rowCount = rs2.last() ? rs2.getRow() : 0;
            ResultSetMetaData md = rs2.getMetaData();
            int columns = md.getColumnCount();

            if (rowCount > 0) {
                while (rs.next()) {
                    HashMap<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columns; ++i) {
                        row.put(md.getColumnName(i), rs.getObject(i));
                    }
                    list.add(row);
                }

                salida.put("detallePago", list);

            }

        } catch (Exception e) {
            log.error(AppConstantes.ERROR_QUERY);
            log.error(e.getMessage());
            logUtil.crearArchivoLog(Level.WARNING.toString(), this.getClass().getSimpleName(),
                    this.getClass().getPackage().toString(),
                    AppConstantes.ERROR_LOG_QUERY + AppConstantes.ERROR_CONSULTAR, AppConstantes.CONSULTA,
                    authentication);

            return new Response<>(true, 500, AppConstantes.OCURRIO_ERROR_GENERICO, e.getMessage());

        } finally {

            if (connection != null) {
                connection.close();
            }

            if (rs != null) {
                rs.close();
            }

            if (rs2 != null) {
                rs2.close();
            }

        }

        return new Response<>(false, 200, AppConstantes.EXITO, salida);

    }

    @Override
    public Response<?> actualizarPago(DatosRequest request, Authentication authentication) throws IOException {
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        ActualizaPagoRequest actualiza = json.fromJson(datosJson, ActualizaPagoRequest.class);
        log.info(actualiza.getIdPago().toString());
        return providerRestTemplate.consumirServicio(beanActualiza.actualizarMetodoPago(actualiza).getDatos(),
                consultas + "/actualizar", authentication);

    }

    @Override
    public Response<?> desactivarPago(DatosRequest request, Authentication authentication)
            throws IOException, SQLException {
        Response<?> response = new Response<>();
        ObjectMapper mapper = new ObjectMapper();
        Gson gson = new Gson();
        UsuarioDto usuario = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        Integer idPagoBitacora = 0;
        JsonNode datos = mapper.readTree(request.getDatos().get(AppConstantes.DATOS).toString());
        idPagoBitacora = datos.get("idPagoBitacora").asInt();
        ResultSet rs2 = null;
        try {
            connection = database.getConnection();
            statement = connection.createStatement();
            String consulta = pagosPlanSFPA.desactivarPagoBitacora();

            preparedStatement = connection.prepareStatement(consulta);

            preparedStatement.setInt(1, usuario.getIdUsuario());
            preparedStatement.setInt(2, idPagoBitacora);

            int rows = preparedStatement.executeUpdate();

            if (rows > 0) {
                response = new Response<>(false, 200, AppConstantes.EXITO);

            } else {
                response = new Response<>(true, 500, AppConstantes.ERROR_GUARDAR);
            }

        } catch (Exception e) {
            log.error(AppConstantes.ERROR_QUERY.concat(AppConstantes.ERROR_CONSULTAR));
            logUtil.crearArchivoLog(Level.WARNING.toString(), this.getClass().getSimpleName(),
                    this.getClass().getPackage().toString(),
                    AppConstantes.ERROR_LOG_QUERY + AppConstantes.ERROR_CONSULTAR, AppConstantes.CONSULTA,
                    authentication);
            throw new IOException(AppConstantes.ERROR_CONSULTAR, e.getCause());
        } finally {

            if (connection != null) {
                connection.close();
            }

            if (statement != null) {
                statement.close();
            }
            if (rs != null) {
                rs.close();
            }
            if (rs2 != null) {
                rs2.close();
            }

        }

        return response;

    }

    @Override
    public Response<?> generarPDF(DatosRequest request, Authentication authentication) throws IOException {
        UsuarioDto usuarioDto = json.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        ReciboPDFRequest recibo = json.fromJson(datosJson, ReciboPDFRequest.class);
        List<ReciboPdfResponse> infoRecibo;
        Response<?> responseRecibo = providerRestTemplate.consumirServicio(
                bean.obtenerInformacionRecibo(recibo.getIdPlanSfpa()).getDatos(), consultas + "/consulta",
                authentication);
        infoRecibo = Arrays.asList(modelMapper.map(responseRecibo.getDatos(), ReciboPdfResponse[].class));
        return providerRestTemplate.consumirServicioReportes(
                generarDatosReporte(infoRecibo.get(0), usuarioDto.getNombre()), urlReportes, authentication);
    }

    @Override
    public Response<?> descargarDocumento(DatosRequest request, Authentication authentication)
            throws IOException, ParseException {
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        ReporteRequest reporteRequest = json.fromJson(datosJson, ReporteRequest.class);
        Map<String, Object> envioDatos = generarDatosReporteGeneral(reporteRequest);
        return providerRestTemplate.consumirServicioReportes(envioDatos, urlReportes,
                authentication);
    }

    @Override
    public Response<?> descargarReportePA(DatosRequest request, Authentication authentication)
            throws IOException, ParseException {
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        ReportePaDto reporteRequest = json.fromJson(datosJson, ReportePaDto.class);
        Map<String, Object> envioDatos = generarDatosReportePa(reporteRequest);
        return providerRestTemplate.consumirServicioReportes(envioDatos, urlReportes,
                authentication);
    }

    public Integer buscarIdContratante(String nombreContratante, Authentication authentication) throws IOException {
        int espacios = 1;
        Integer idContratante = 0;
        String[] nombreCadena = nombreContratante.split(" ");
        String nombre = "";
        String primerApellido = "";
        String segundoApellido = "";
        espacios = nombreCadena.length - 1;
        if (espacios == 2) {
            nombre = nombreCadena[0];
            primerApellido = nombreCadena[1];
            segundoApellido = nombreCadena[2];
        } else if (espacios == 3) {
            nombre = nombreCadena[0] + " " + nombreCadena[1];
            primerApellido = nombreCadena[2];
            segundoApellido = nombreCadena[3];
        }
        Response<?> respuestaIdContratante = providerRestTemplate.consumirServicio(
                bean.buscarIdContratante(nombre, primerApellido, segundoApellido).getDatos(), consultas + "/consulta",
                authentication);
        if (!respuestaIdContratante.getDatos().toString().equals("[]")) {
            JsonArray objeto = (JsonArray) jsonParser.parse(respuestaIdContratante.getDatos().toString());
            JsonObject obj = (JsonObject) jsonParser.parse(objeto.get(0).toString());
            idContratante = obj.get("ID_CONTRATANTE").getAsInt();
            return idContratante;
        }
        log.info("nll");
        return 0;
    }

    public String validaNull(Object valor) {
        if (Objects.isNull(valor)) {
            return "";
        }
        return valor.toString();
    }

    public String obtenerMontoRestante(Authentication authentication, String idPlan) throws IOException {
        Response<?> respuesta = providerRestTemplate.consumirServicio(bean.obtenerRestante(idPlan).getDatos(),
                consultas + "/consulta", authentication);
        JsonArray objeto = (JsonArray) jsonParser.parse(respuesta.getDatos().toString());
        JsonObject obj = (JsonObject) jsonParser.parse(objeto.get(0).toString());
        String restante = obj.get("totalRestante").getAsString();
        if (restante.contains("-")) {
            restante = "0.0";
        }
        return restante;
    }

    public Boolean validaEsPrimerPago(Authentication authentication, String idPlan) throws IOException {
        Integer numPagos;
        Response<?> respuesta = providerRestTemplate.consumirServicio(
                bean.obtenerNumeroPagosRealizados(idPlan).getDatos(), consultas + "/consulta", authentication);
        JsonArray objeto = (JsonArray) jsonParser.parse(respuesta.getDatos().toString());
        JsonObject obj = (JsonObject) jsonParser.parse(objeto.get(0).toString());
        numPagos = obj.get("pagosRealizados").getAsInt();
        if (numPagos == 0) {
            log.info("- primer pago");
            return true;
        }
        log.info("- ya cuenta con pagos");
        return false;
    }

    public Map<String, Object> generarDatosReporte(ReciboPdfResponse infoRecibo, String nombreUsuario) {
        Map<String, Object> datosPdf = new HashMap<>();
        datosPdf.put("rutaNombreReporte", "reportes/plantilla/ANEXO26_RECIBO_PAGO_ANTICIPADO.jrxml");
        datosPdf.put("tipoReporte", "pdf");
        datosPdf.put("numeroContrato", infoRecibo.getNumeroFolio());
        datosPdf.put("numeroParcialidad", infoRecibo.getPagosRealizados());
        datosPdf.put("totalParcialidades", infoRecibo.getTotalMensualidades());
        datosPdf.put("nombreContratante", infoRecibo.getNombreContratante());
        datosPdf.put("importeTexto", ConvertirImporteLetra.importeEnTexto(Integer.parseInt(infoRecibo.getImporte())));
        datosPdf.put("importeNumero", infoRecibo.getImporte());
        datosPdf.put("paquete", infoRecibo.getNombrePaquete());
        datosPdf.put("velatorio", infoRecibo.getVelatorio());
        datosPdf.put("nombreUsuario", nombreUsuario);
        return datosPdf;
    }

    public Map<String, Object> generarDatosReporteGeneral(ReporteRequest reporteRequest) throws IOException {
        Map<String, Object> datosReporte = new HashMap<>();
        datosReporte.put("rutaNombreReporte", "reportes/generales/ReporteConsultaPagosAnticipadosSpfa.jrxml");
        datosReporte.put("tipoReporte", reporteRequest.getTipoReporte());
        datosReporte.put("idPlan", reporteRequest.getIdPlan());
        datosReporte.put("correoElectronico", reporteRequest.getCorreoElectronico());
        datosReporte.put("paquete", reporteRequest.getPaquete());
        datosReporte.put("estado", reporteRequest.getEstado());
        datosReporte.put("nombreContratante", reporteRequest.getNombreContratante());
        return datosReporte;
    }

    public Map<String, Object> generarDatosReportePa(ReportePaDto reporteRequest) throws IOException {
        Map<String, Object> datosReporte = new HashMap<>();
        String fechaInicio = validaNull(reporteRequest.getFecha_inicial());
        String fechaFin = validaNull(reporteRequest.getFecha_final());
        StringBuilder consulta = new StringBuilder("");
        String periodo = "";

        if (Objects.nonNull(reporteRequest.getId_delegacion())) {
            consulta.append(" AND VP.ID_DELEGACION = " + reporteRequest.getId_delegacion());
        }

        if (Objects.nonNull(reporteRequest.getId_velatorio())) {
            consulta.append(" AND PLAN.ID_VELATORIO = " + reporteRequest.getId_velatorio());
        }
        if (!fechaInicio.equals("")) {
            consulta.append(" AND PLAN.FEC_INGRESO >= STR_TO_DATE('" + fechaInicio + "','%d/%m/%Y') ");
            periodo = !fechaFin.equals("") ? "Periodo: del " + fechaInicio : "Periodo: desde " + fechaInicio;
        }
        if (!fechaFin.equals("")) {
            consulta.append(" AND PLAN.FEC_INGRESO  <= STR_TO_DATE('" + fechaFin + "','%d/%m/%Y') ");
            periodo += !fechaInicio.equals("") ? " al " + fechaFin : "Periodo: hasta " + fechaFin;
        }

        datosReporte.put("consultaOrdenes", consulta.toString());
        datosReporte.put("periodo", periodo);
        datosReporte.put("velatorio", validaNull(reporteRequest.getNombreVelatorio()));
        datosReporte.put("rutaNombreReporte", reportePa);
        datosReporte.put("tipoReporte", reporteRequest.getTipoReporte());
        log.info(datosReporte.get("consultaOrdenes").toString());
        return datosReporte;
    }

    @Override
    public Response<?> bitacoraDetallePagos(DatosRequest request, Authentication authentication)
            throws IOException, SQLException {
        Response<?> response = new Response<>();
        ObjectMapper mapper = new ObjectMapper();
        Integer idPagoParcialidad = 0;
        JsonNode datos = mapper.readTree(request.getDatos().get(AppConstantes.DATOS).toString());
        idPagoParcialidad = datos.get("idPagoParcialidad").asInt();
        ResultSet rs2 = null;
        try {
            connection = database.getConnection();
            statement = connection.createStatement();
            String consulta = pagosPlanSFPA.obtenerDetalleBitacoraPago();

            preparedStatement = connection.prepareStatement(consulta, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);

            preparedStatement.setInt(1, idPagoParcialidad);
            rs2 = preparedStatement.executeQuery();
            rs = preparedStatement.executeQuery();

            List<Object> list = new ArrayList<>();
            int rowCount = rs2.last() ? rs2.getRow() : 0;
            ResultSetMetaData md = rs2.getMetaData();
            int columns = md.getColumnCount();

            if (rowCount > 0) {
                while (rs.next()) {
                    HashMap<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columns; ++i) {
                        row.put(md.getColumnName(i), rs.getObject(i));
                    }
                    list.add(row);
                }

                response = new Response<>(false, 200, AppConstantes.EXITO, list);
                return response;
            }

        } catch (Exception e) {
            log.error(AppConstantes.ERROR_QUERY.concat(AppConstantes.ERROR_CONSULTAR));
            logUtil.crearArchivoLog(Level.WARNING.toString(), this.getClass().getSimpleName(),
                    this.getClass().getPackage().toString(),
                    AppConstantes.ERROR_LOG_QUERY + AppConstantes.ERROR_CONSULTAR, AppConstantes.CONSULTA,
                    authentication);
            throw new IOException(AppConstantes.ERROR_CONSULTAR, e.getCause());
        } finally {

            if (connection != null) {
                connection.close();
            }

            if (statement != null) {
                statement.close();
            }
            if (rs != null) {
                rs.close();
            }
            if (rs2 != null) {
                rs2.close();
            }

        }

        return response;
    }

    private Object traeDatosGenerales(Connection connection,
            Integer idPlan) throws SQLException {
        HashMap<String, Object> salida = new HashMap<>();
        ResultSet rs2 = null;
        try {

            String consultaDatosGenerales = pagosPlanSFPA.detallePlan();
            log.info("query {}", consultaDatosGenerales);
            preparedStatement = connection.prepareStatement(consultaDatosGenerales);
            preparedStatement.setInt(1, idPlan);
            rs2 = preparedStatement.executeQuery();
            rs = preparedStatement.executeQuery();
            ResultSetMetaData md = rs2.getMetaData();
            int columns = md.getColumnCount();
            int rowCount = rs2.last() ? rs2.getRow() : 0;
            if (rowCount > 0) {
                while (rs.next()) {
                    for (int i = 1; i <= columns; ++i) {
                        salida.put(md.getColumnName(i), rs.getObject(i));
                    }
                }
            }

        } catch (Exception e) {
            log.error(AppConstantes.ERROR_QUERY);
            log.error(e.getMessage());
            if (connection != null)
                connection.close();
            if (rs2 != null)
                rs.close();
            if (rs != null)
                rs.close();

            return salida;
        }
        return salida;

    }

}
