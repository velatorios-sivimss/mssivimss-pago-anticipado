package com.imss.sivimss.pagoanticipado.util;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({ "error", "codigo", "mensaje", "datos" })
public class ErrorsMessageResponse {

	private Date fecha;

	private String mensaje;

	private String datos;

	private long codigo;

	private boolean error;

	public ErrorsMessageResponse(Date timestamp, long codigo, String mensaje, String detalles) {
		super();
		this.fecha = timestamp;
		this.error = true;
		this.codigo = codigo;
		this.mensaje = mensaje;
		this.datos = detalles;

	}

	public void setError(boolean error) {
		this.error = error;
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

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

}
