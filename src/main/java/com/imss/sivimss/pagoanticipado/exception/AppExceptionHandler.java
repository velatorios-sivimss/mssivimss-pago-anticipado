package com.imss.sivimss.pagoanticipado.exception;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.imss.sivimss.pagoanticipado.util.ErrorsMessageResponse;
import com.imss.sivimss.pagoanticipado.util.ValidacionErrores;

@ControllerAdvice // permite manejar exepciones handler de toda la aplicacion
public class AppExceptionHandler {

	/**
	 * Metodo que controla las excepciones de tipo validacion en los dto
	 * 
	 * @return
	 */
	@ExceptionHandler(value = { MethodArgumentNotValidException.class })
	public ResponseEntity<Object> handleValidationErrorException(MethodArgumentNotValidException ex,
			WebRequest webRequest) {
		Map<String, String> errores = new HashMap<>();
		for (ObjectError error : ex.getBindingResult().getAllErrors()) {
			String valorCampo = ((FieldError) error).getField();
			String mensajeError = error.getDefaultMessage();
			errores.put(valorCampo, mensajeError);
		}

		ValidacionErrores validacionErrores = new ValidacionErrores(errores, new Date());
		return new ResponseEntity<>(validacionErrores, new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}

	/**
	 * metodo que controla todas las exeception internal server
	 * 
	 * @param exception
	 * @param webRequest
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorsMessageResponse> manejadorInternalServerError(Exception exception,
			WebRequest webRequest) {
		ErrorsMessageResponse errorDetalles = new ErrorsMessageResponse(new Date(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), webRequest.getDescription(false));
		return new ResponseEntity<>(errorDetalles, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * metodo que controlara todas las excepciones de la aplicacion not found
	 * 
	 * @param exception
	 * @param webRequest
	 * @return
	 */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorsMessageResponse> manejadorResourceNotFoundException(ResourceNotFoundException exception,
			WebRequest webRequest) {
		ErrorsMessageResponse errorDetalles = new ErrorsMessageResponse(new Date(), HttpStatus.NOT_FOUND.value(),
				exception.getMessage(), webRequest.getDescription(false));
		return new ResponseEntity<>(errorDetalles, HttpStatus.NOT_FOUND);
	}

	/**
	 * metodo que controlara todas las excepciones de la aplicacion bad request
	 * 
	 * @param exception
	 * @param webRequest
	 * @return
	 */
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorsMessageResponse> manejadorResourceNotFoundException(BadRequestException exception,
			WebRequest webRequest) {
		ErrorsMessageResponse errorDetalles = new ErrorsMessageResponse(new Date(), HttpStatus.BAD_REQUEST.value(),
				exception.getMessage(), webRequest.getDescription(false));
		return new ResponseEntity<>(errorDetalles, HttpStatus.BAD_REQUEST);
	}

}