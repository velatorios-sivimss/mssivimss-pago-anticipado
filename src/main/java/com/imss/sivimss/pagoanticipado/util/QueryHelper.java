package com.imss.sivimss.pagoanticipado.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import org.springframework.http.HttpStatus;

import com.imss.sivimss.pagoanticipado.exception.BadRequestException;

/**
 * 
 * @author pnolasco
 *
 */
public class QueryHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final transient String ESPACIO = " ";

	private StringBuilder querySelect;
	private StringBuilder queryJoin;
	private StringBuilder queryWhere;

	private transient HashMap<String, Object> parametros;

	public QueryHelper(final String query) {
		this.querySelect = new StringBuilder(query);
		this.queryWhere = new StringBuilder();
		this.parametros = new HashMap<>();
	}

	public void agregarParametroValues(final String nombreParametro, final String valorParametro) {
		if (!valorParametro.isEmpty()) {
			this.parametros.put(nombreParametro, valorParametro);
		}
	}

	public void addColumn(final String nombreParametro, final String valorParametro) {
		if (!valorParametro.isEmpty()) {
			this.parametros.put(nombreParametro, valorParametro);
		}
	}

	public void addJoin(String joinQ) {
		this.queryJoin.append(ESPACIO).append(joinQ);
	}

	public void addWhere(String whereQ) {
		this.queryWhere.append(ESPACIO).append(whereQ);
	}

	public String obtenerQueryInsertar() {
		StringBuilder querySelectFinal = null;
		StringBuilder queryValues = null;
		querySelectFinal = new StringBuilder(this.querySelect);
		queryValues = new StringBuilder("(");
		querySelectFinal.append(ESPACIO);
		int count = 0;
		int max = parametros.size();
		Set<String> keys = parametros.keySet();
		querySelectFinal.append("(");

		for (String string : keys) {
			querySelectFinal.append(string);
			queryValues.append(parametros.get(string));
			count++;
			if (count != max) {
				querySelectFinal.append(",");
				queryValues.append(",");

			}

		}
		querySelectFinal.append(")");
		queryValues.append(")");
		querySelectFinal.append(ESPACIO).append("VALUES").append(ESPACIO).append(queryValues).append(";");

		return querySelectFinal.toString();
	}

	public String obtenerQueryActualizar() {
		StringBuilder querySelectFinal = null;
		StringBuilder queryValues = null;
		querySelectFinal = new StringBuilder(this.querySelect);
		querySelectFinal.append(ESPACIO);

		queryValues = new StringBuilder("SET");
		queryValues.append(ESPACIO);

		int count = 0;
		int max = parametros.size();
		Set<String> keys = parametros.keySet();
		for (String string : keys) {
			queryValues.append(string + " = coalesce(" + parametros.get(string) + "," + string + ")");
			count++;
			if (count != max) {
				queryValues.append(",").append(ESPACIO);

			}

		}
		if (queryWhere.length() <= 0) {
			throw new BadRequestException(HttpStatus.BAD_REQUEST, "Sintaxis incorrecta, no se realizo la transacción.");
		}
		querySelectFinal.append(queryValues).append(ESPACIO).append("WHERE").append(queryWhere).append(";");
		return querySelectFinal.toString();
	}

}
