package io.jeannyil.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import io.jeannyil.models.ErrorMessage;
import io.jeannyil.models.ResponseMessage;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * 
 * Service Response helper bean 
 *
 */
@ApplicationScoped
@Named("serviceResponseHelper")
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class ServiceResponseHelper {
	
	/**
	 * Generates the ResponseMessage
	 * @param fileName
	 * @param content
	 * @return ResponseMessage
	 */
	public ResponseMessage generateResponseMessage(String fileName, String content) {
		return new ResponseMessage(fileName, content != null ? content.trim() : null);
	}
	
	/**
	 * Generates the ResponseMessage
	 * @param erroCode
	 * @param errorDescription
	 * @param errorMessage
	 * @return ErrorMessage
	 */
	public ErrorMessage generateErrorMessage(String errorCode, String errorDescription, String errorMessage) {
		return new ErrorMessage(errorCode, errorDescription, errorMessage);
	}

}
