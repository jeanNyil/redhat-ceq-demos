package io.jeannyil.quarkus.camel.xmlvalidation.beans;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import io.jeannyil.quarkus.camel.xmlvalidation.models.ValidationResult;
import io.jeannyil.quarkus.camel.xmlvalidation.models.ValidationResult_;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * 
 * Validation Result helper bean 
 *
 */
@ApplicationScoped
@Named("validationResultHelper")
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class ValidationResultHelper {
	
	/**
	 * Generates a successful ValidationResult
	 * @return successful ValidationResult
	 */
	public ValidationResult generateOKValidationResult() {
		ValidationResult validationResult = new ValidationResult();
		ValidationResult_ validationAttributes = new ValidationResult_();
		validationAttributes.setStatus("OK");
		validationResult.setValidationResult(validationAttributes);
		return validationResult;
	}
	
	/**
	 * Generates a KO ValidationResult
	 * @param errorMessage
	 * @return KO ValidationResult
	 */
	public ValidationResult generateKOValidationResult(String errorMessage) {
		ValidationResult validationResult = new ValidationResult();
		ValidationResult_ validationAttributes = new ValidationResult_();
		validationAttributes.setStatus("KO");
		validationAttributes.setErrorMessage(errorMessage);
		validationResult.setValidationResult(validationAttributes);
		return validationResult;
	}

}
