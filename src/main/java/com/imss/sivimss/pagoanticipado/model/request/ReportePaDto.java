package com.imss.sivimss.pagoanticipado.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportePaDto {
	
	private Integer id_delegacion;
	private Integer id_velatorio;
	private String fecha_inicial;
	private String fecha_final;
	private String nombreVelatorio;
	private String tipoReporte;
	
}
