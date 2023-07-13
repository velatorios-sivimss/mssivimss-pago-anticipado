package com.imss.sivimss.pagoanticipado.util;

import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class ValidacionErrores {

	private Map<String, String> errores;

	private String mensaje;

	private long codigo;

	private boolean error;

	private Date fecha;

	public Map<String, String> getErrores() {
		return errores;
	}

	public void setErrores(Map<String, String> errores) {
		this.errores = errores;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public long getCodigo() {
		return codigo;
	}

	public void setCodigo(long codigo) {
		this.codigo = codigo;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public ValidacionErrores(Map<String, String> errores, Date fecha) {
		super();
		this.errores = errores;
		this.fecha = fecha;
		this.mensaje = "Error en la petición";
		this.codigo = HttpStatus.BAD_REQUEST.value();
		this.error = true;
	}

}
