package com.imss.sivimss.pagoanticipado.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private HttpStatus codigo;

	private String mensaje;

	private String datos;

	private boolean error;

	public BadRequestException(HttpStatus codigo, String mensaje) {
		super(mensaje);
		this.codigo = codigo;
		this.datos = "";
		this.mensaje = mensaje;
		this.error = true;
	}

	public HttpStatus getEstado() {
		return codigo;
	}

	public void setEstado(HttpStatus estado) {
		this.codigo = estado;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getDatos() {
		return datos;
	}

	public void setDatos(String datos) {
		this.datos = datos;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}
}