package com.imss.sivimss.pagoanticipado.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.imss.sivimss.pagoanticipado.beans.ActualizacionesPagosPlanSFPA;
import com.imss.sivimss.pagoanticipado.beans.BusquedasPlanSFPA;
import com.imss.sivimss.pagoanticipado.beans.InsercionesPagosSFPA;
import com.imss.sivimss.pagoanticipado.model.request.*;
import com.imss.sivimss.pagoanticipado.model.response.DetalleGeneralPlanResponse;
import com.imss.sivimss.pagoanticipado.model.response.DetallePagosResponse;
import com.imss.sivimss.pagoanticipado.model.response.DetallePlanResponse;
import com.imss.sivimss.pagoanticipado.model.response.ReciboPdfResponse;
import com.imss.sivimss.pagoanticipado.service.PagoAnticipadoSFPAService;
import com.imss.sivimss.pagoanticipado.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@Service
public class PagoAnticipadoSFPAImpl implements PagoAnticipadoSFPAService {

    @Value("${endpoints.mod-catalogos}")
    private String consultas;
    @Value("${endpoints.ms-reportes}")
    private String urlReportes;
    @Autowired
    private ProviderServiceRestTemplate providerRestTemplate;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    BusquedasPlanSFPA bean = new BusquedasPlanSFPA();
    @Autowired
    InsercionesPagosSFPA beanInserta = new InsercionesPagosSFPA();
    @Autowired
    ActualizacionesPagosPlanSFPA beanActualiza = new ActualizacionesPagosPlanSFPA();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PagoAnticipadoSFPAImpl.class);
    JsonParser jsonParser = new JsonParser();
    Gson json = new Gson();

    @Override
    public Response<?> buscarPlanSFPA(DatosRequest request, Authentication authentication) throws IOException {
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        BusquedaRequest busquedaRequest = json.fromJson(datosJson, BusquedaRequest.class);
        String folio = validaNull(busquedaRequest.getFolio());
        String fechaInicio = validaNull(busquedaRequest.getFechaInicio());
        String fechaFin = validaNull(busquedaRequest.getFechaFin());
        String nombreTitularSustituto = validaNull(busquedaRequest.getNombreTitularSustituto());
        String idVelatorio = validaNull(busquedaRequest.getIdVelatorio());
        return providerRestTemplate.consumirServicio(bean.buscarPlanSFPA(folio, fechaInicio, fechaFin, buscarIdContratante(nombreTitularSustituto, authentication).toString(), idVelatorio).getDatos(), consultas + "/consulta", authentication);
    }

    @Override
    public Response<?> metodosPago(DatosRequest request, Authentication authentication) throws IOException {
        return providerRestTemplate.consumirServicio(bean.obtenerMetodosPago().getDatos(), consultas + "/consulta", authentication);
    }

    @Override
    public Response<?> generarPago(DatosRequest request, Authentication authentication) throws IOException {
        UsuarioDto usuarioDto = json.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        RegistrarPagoRequest pagoRequest = json.fromJson(datosJson, RegistrarPagoRequest.class);
        Response<?> respuestaInfoPaquete = providerRestTemplate.consumirServicio(bean.obtenerMontoPaquete(pagoRequest.getIdPlan()).getDatos(), consultas + "/consulta", authentication);
        String montoTotal = "";
        JsonArray objeto = (JsonArray) jsonParser.parse(respuestaInfoPaquete.getDatos().toString());
        JsonObject obj = (JsonObject) jsonParser.parse(objeto.get(0).toString());
        String mesesPagar = obj.get("mesesPagar").getAsString();
        String desMeses = obj.get("desMeses").getAsString();
        Boolean banderaPrimerPago = validaEsPrimerPago(authentication, pagoRequest.getIdPlan());
        if (banderaPrimerPago) {
            montoTotal = obj.get("importeTotal").getAsString();
        } else {
            montoTotal = obtenerMontoRestante(authentication, pagoRequest.getIdPlan());
        }
        Response<?> response = providerRestTemplate.consumirServicio(beanInserta.insertarBitacoraPago(pagoRequest, montoTotal, obj.get("importeTotal").getAsString(), mesesPagar, desMeses, usuarioDto.getIdUsuario().toString()).getDatos(), consultas + "/crearMultiple", authentication);
        if(montoTotal.equals("0.0")){
            providerRestTemplate.consumirServicio(beanActualiza.actualizarEstatusPagadoPlanSFPA(pagoRequest.getIdPlan()).getDatos(), consultas + "/actualizar", authentication);
        }

        if (response.getCodigo() == 200 && banderaPrimerPago) {
            providerRestTemplate.consumirServicio(beanActualiza.actualizarEstatusVigentePlanSFPA(pagoRequest.getIdPlan()).getDatos(), consultas + "/actualizar", authentication);
        }
        if(montoTotal.equals(pagoRequest.getImporte()) && response.getCodigo() == 200){
            providerRestTemplate.consumirServicio(beanActualiza.actualizarEstatusPagadoPlanSFPA(pagoRequest.getIdPlan()).getDatos(), consultas + "/actualizar", authentication);
        }
        if(Double.valueOf(pagoRequest.getImporte()) > Double.valueOf(montoTotal) ){
            log.info("el importe es mayor");
            providerRestTemplate.consumirServicio(beanActualiza.actualizarEstatusPagadoPlanSFPA(pagoRequest.getIdPlan()).getDatos(), consultas + "/actualizar", authentication);
        }
        return response;
    }

    @Override
    public Response<?> verDetallePagos(DatosRequest request, Authentication authentication) throws IOException {
        Response<?> response = new Response<>();
        JsonObject objeto = (JsonObject) jsonParser.parse((String) request.getDatos().get(AppConstantes.DATOS));
        String idPlan = objeto.get("idPlan").getAsString();
        List<DetallePlanResponse> infoDetallePlan;
        List<DetallePagosResponse> infoDetallePago;
        Response<?> responseDetallePlan = providerRestTemplate.consumirServicio(bean.obtenerDetallePlan(idPlan).getDatos()
                , consultas + "/consulta", authentication);
        Response<?> responseDetallePago = providerRestTemplate.consumirServicio(bean.obtenerDetallePagos(idPlan).getDatos()
                , consultas + "/consulta", authentication);
        infoDetallePlan = Arrays.asList(modelMapper.map(responseDetallePlan.getDatos(), DetallePlanResponse[].class));
        infoDetallePago = Arrays.asList(modelMapper.map(responseDetallePago.getDatos(),DetallePagosResponse[].class));
        DetalleGeneralPlanResponse respuestaGeneral = new DetalleGeneralPlanResponse();
        respuestaGeneral.setDetallePlan(infoDetallePlan.get(0));
        respuestaGeneral.setPagos(infoDetallePago);
        response.setDatos(ConvertirGenerico.convertInstanceOfObject(respuestaGeneral));
        response.setError(false);
        response.setCodigo(200);
        response.setMensaje("");
        return response;
    }

    @Override
    public Response<?> generarPDF(DatosRequest request, Authentication authentication) throws IOException {
        UsuarioDto usuarioDto = json.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        ReciboPDFRequest recibo = json.fromJson(datosJson, ReciboPDFRequest.class);
        List<ReciboPdfResponse> infoRecibo;
        Response<?> responseRecibo = providerRestTemplate.consumirServicio(bean.obtenerInformacionRecibo(recibo.getIdPlanSfpa()).getDatos()
                , consultas + "/consulta", authentication);
        infoRecibo = Arrays.asList(modelMapper.map(responseRecibo.getDatos(), ReciboPdfResponse[].class));
        return providerRestTemplate.consumirServicioReportes(generarDatosReporte(infoRecibo.get(0), usuarioDto.getNombre()), urlReportes, authentication);
    }

    @Override
    public Response<?> descargarDocumento(DatosRequest request, Authentication authentication) throws IOException, ParseException {
        String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
        ReporteRequest reporteRequest = json.fromJson(datosJson, ReporteRequest.class);
        Map<String, Object> envioDatos = generarDatosReporteGeneral(reporteRequest,authentication);
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
        Response<?> respuestaIdContratante = providerRestTemplate.consumirServicio(bean.buscarIdContratante(nombre, primerApellido, segundoApellido).getDatos(), consultas + "/consulta", authentication);
        if (!respuestaIdContratante.getDatos().toString().equals("[]")) {
            JsonArray objeto = (JsonArray) jsonParser.parse(respuestaIdContratante.getDatos().toString());
            JsonObject obj = (JsonObject) jsonParser.parse(objeto.get(0).toString());
            idContratante = obj.get("ID_CONTRATANTE").getAsInt();
            return idContratante;
        }
        return 0;
    }


    public String validaNull(Object valor) {
        if (Objects.isNull(valor)) {
            return "";
        }
        return valor.toString();
    }

    public String obtenerMontoRestante(Authentication authentication, String idPlan) throws IOException {
        Response<?> respuesta = providerRestTemplate.consumirServicio(bean.obtenerRestante(idPlan).getDatos(), consultas + "/consulta", authentication);
        JsonArray objeto = (JsonArray) jsonParser.parse(respuesta.getDatos().toString());
        JsonObject obj = (JsonObject) jsonParser.parse(objeto.get(0).toString());
        String restante = obj.get("totalRestante").getAsString();
        log.info("- restante : " + restante);
        if(restante.contains("-")){
            restante= "0.0";
        }
        return restante;
    }

    public Boolean validaEsPrimerPago(Authentication authentication, String idPlan) throws IOException {
        Integer numPagos;
        Response<?> respuesta = providerRestTemplate.consumirServicio(bean.obtenerNumeroPagosRealizados(idPlan).getDatos(), consultas + "/consulta", authentication);
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

    public Map<String,Object> generarDatosReporteGeneral(ReporteRequest reporteRequest,Authentication authentication) throws IOException {
        Map<String, Object> datosReporte = new HashMap<>();
        String folio = validaNull(reporteRequest.getFolioPlan());
        String fechaInicio = validaNull(reporteRequest.getFechaInicio());
        String fechaFin = validaNull(reporteRequest.getFechaFin());
        String nombreTitularSustituto = validaNull(reporteRequest.getNombreContratante());
        String idVelatorio = validaNull(reporteRequest.getIdVelatorio());
        String consulta = "";
        if(!folio.equals("")){
            consulta += " AND SP.NUM_FOLIO_PLAN_SFPA = '" + folio + "'";
        }
        if(!fechaInicio.equals("") && !fechaFin.equals("")){
            consulta += " AND SP.FEC_INGRESO BETWEEN '" + fechaInicio + "' AND '" + fechaFin + "'";
        }
        if(!idVelatorio.equals("")){
            consulta += " AND SP.ID_VELATORIO = " + idVelatorio;
        }
        if(!buscarIdContratante(nombreTitularSustituto, authentication).toString().equals("0")){
            consulta +=" AND SP.ID_TITULAR_SUBSTITUTO = " + buscarIdContratante(nombreTitularSustituto, authentication).toString();
        }
        datosReporte.put("rutaNombreReporte","reportes/generales/ReporteConsultaPagosAnticipadosSpfa.jrxml");
        datosReporte.put("tipoReporte",reporteRequest.getTipoReporte());
        datosReporte.put("idVelatorio",idVelatorio);
        datosReporte.put("consulta",consulta);
        return datosReporte;
    }
}