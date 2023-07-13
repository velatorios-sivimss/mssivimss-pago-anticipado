package com.imss.sivimss.pagoanticipado.util;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class Response<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Boolean error;

	private Integer codigo;

	private String mensaje;

	private transient T datos;

	public Response() {

	}

	public Response(Boolean error, Integer codigo, String mensaje) {
		super();
		this.error = error;
		this.codigo = codigo;
		this.mensaje = mensaje;
	}

	public Response(Boolean error, Integer codigo, String mensaje, T datos) {
		super();
		this.error = error;
		this.codigo = codigo;
		this.mensaje = mensaje;
		this.datos = datos;
	}

}
