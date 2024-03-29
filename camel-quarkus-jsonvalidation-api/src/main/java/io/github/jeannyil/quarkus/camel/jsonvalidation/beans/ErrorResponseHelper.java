package io.jeannyil.quarkus.camel.jsonvalidation.beans;

import java.util.ArrayList;

import io.jeannyil.quarkus.camel.jsonvalidation.models.Error;
import io.jeannyil.quarkus.camel.jsonvalidation.models.ErrorResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * 
 * Error Response helper bean 
 *
 */
@ApplicationScoped
@Named("errorResponseHelper")
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class ErrorResponseHelper {
	
	/**
	 * Generates an ErrorResponse object
	 * @param id
	 * @param description
	 * @param message
	 * @return ErrorResponse
	 */
	public ErrorResponse generateErrorResponse(String id, String description, String message) {
		ArrayList<String> messages = new ArrayList<String>(0);
		if (message != null) {
			messages.add(message);
		}
		Error error = new Error(id, description, messages);
		return new ErrorResponse(error);
	}

}
