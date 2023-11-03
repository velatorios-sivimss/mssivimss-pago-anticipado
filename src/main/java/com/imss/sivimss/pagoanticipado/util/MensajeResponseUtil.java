package com.imss.sivimss.pagoanticipado.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MensajeResponseUtil {

	private static final Logger log = LoggerFactory.getLogger(MensajeResponseUtil.class);

	private MensajeResponseUtil() {
		super();
	}

	public static Response<Object> mensajeResponse(Response<Object> respuestaGenerado, String numeroMensaje) {
		Integer codigo = respuestaGenerado.getCodigo();
		if (codigo == 200) {
			respuestaGenerado.setMensaje(numeroMensaje);
		} else {
			log.error("Error.. {}", respuestaGenerado.getMensaje());
			respuestaGenerado.setMensaje("5");
		}
		return respuestaGenerado;
	}

	public static Response<Object> mensajeConsultaResponse(Response<Object> respuestaGenerado, String numeroMensaje) {
		Integer codigo = respuestaGenerado.getCodigo();
		if (codigo == 200 && (!respuestaGenerado.getDatos().toString().contains("id"))) {
			respuestaGenerado.setMensaje(numeroMensaje);
		}
		return respuestaGenerado;
	}

	public static Response<Object> mensajeConsultaResponseObject(Response<Object> respuestaGenerado,
			String numeroMensaje) {
		Integer codigo = respuestaGenerado.getCodigo();
		if (codigo == 200 && !respuestaGenerado.getDatos().toString().contains("[]")) {
			respuestaGenerado.setMensaje(numeroMensaje);
		} else if (codigo == 400 || codigo == 404 || codigo == 500) {
			log.error("Error.. {}", respuestaGenerado.getMensaje());
			respuestaGenerado.setMensaje(numeroMensaje);
		}
		return respuestaGenerado;
	}

}
