package io.jeannyil.quarkus.camel.jsonvalidation.routes;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;

import io.jeannyil.quarkus.camel.jsonvalidation.models.ErrorResponse;

/* Exposes the Sample JSON Validation RESTful API

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class SampleJsonValidationApiRoute extends RouteBuilder {

	private static String logName = SampleJsonValidationApiRoute.class.getName();
	
	@Override
	public void configure() throws Exception {
		
		/**
		 * Catch unexpected exceptions
		 */
		onException(Exception.class).id("handle-all-other-exceptions")
			.handled(true)
			.maximumRedeliveries(0)
			.log(LoggingLevel.ERROR, logName, ">>> ${routeId} - Caught exception: ${exception.stacktrace}").id("log-api-unexpected")
			.to("direct:common-500").id("to-common-500")
			.log(LoggingLevel.INFO, logName, ">>> ${routeId} - OUT: headers:[${headers}] - body:[${body}]").id("log-api-unexpected-response")
		;
		
		
		/**
		 * REST configuration with Camel Quarkus Platform HTTP component
		 */
		restConfiguration()
			.component("platform-http")
			.enableCORS(true)
			.bindingMode(RestBindingMode.off) // RESTful responses will be explicitly marshaled for logging purposes
			.dataFormatProperty("prettyPrint", "true")
			.scheme("http")
			.host("0.0.0.0")
			.port("8080")
			.contextPath("/")
			.clientRequestValidation(true)
		;

		/**
		 * REST endpoint for the Service OpenAPI document 
		  */
        rest()
            .produces(MediaType.APPLICATION_JSON)
            .get("/openapi.json")
                .id("openapi-route")
                .description("Gets the OpenAPI specification for this service in JSON format")
                .to("direct:getOAS")
        ;

        // Returns the OAS
        from("direct:getOAS")
			.routeId("get-oas-route")
            .log(LoggingLevel.INFO, logName, ">>> IN: headers:[${headers}] - body:[${body}]")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/vnd.oai.openapi+json"))
            .setBody().constant("resource:classpath:openapi/openapi.json")
            .log(LoggingLevel.INFO, logName, ">>> OUT: headers:[${headers}] - body:[${body}]")
        ;
		
		/**
		 * REST endpoint for the Sample JSON Validation RESTful API 
		 */
		rest().id("sample-json-validation-restapi")
			.consumes(MediaType.APPLICATION_JSON)
			.produces(MediaType.APPLICATION_JSON)
				
			// Validates a `Membership` JSON instance
			.post("/validateMembershipJSON")
				.id("json-validation-api-route")
				.description("Validates a `Membership` JSON instance")
				.param()
					.name("body")
					.type(RestParamType.body)
					.description("A `Membership` JSON instance to be validated.")
					.dataType("string")
					.required(true)
					.example(MediaType.APPLICATION_JSON,
							 "{\n    \"requestType\": \"API\",\n    \"requestID\": 5948,\n    \"memberID\": 85623617,\n    \"status\": \"A\",\n    \"enrolmentDate\": \"2019-06-16\",\n    \"changedBy\": \"jeanNyil\",\n    \"forcedLevelCode\": \"69\",\n    \"vipOnInvitation\": \"Y\",\n    \"startDate\": \"2019-06-16\",\n    \"endDate\": \"2100-06-16\"\n}")
				.endParam()
				.responseMessage()
					.code(Response.Status.OK.getStatusCode())
					.message(Response.Status.OK.getReasonPhrase())
					.responseModel(io.jeannyil.quarkus.camel.jsonvalidation.models.ValidationResult.class)
					.example(MediaType.APPLICATION_JSON, 
							 "{\n    \"validationResult\": {\n        \"status\": \"OK\"\n    }\n}")
				.endResponseMessage()
				.responseMessage()
					.code(Response.Status.BAD_REQUEST.getStatusCode())
					.message(Response.Status.BAD_REQUEST.getReasonPhrase())
					.responseModel(io.jeannyil.quarkus.camel.jsonvalidation.models.ValidationResult.class)
					.example(MediaType.APPLICATION_JSON, 
							 "{\n    \"validationResult\": {\n        \"status\": \"KO\",\n        \"errorMessage\": \"6 errors found\"\n    }\n}")
				.endResponseMessage()
				.responseMessage()
					.code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
					.message(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
					.responseModel(ErrorResponse.class)
					.example(MediaType.APPLICATION_JSON, 
							 "{\n\t\"error\": {\n\t\t\"id\": \"500\",\n\t\t\"description\": \"Internal Server Error\",\n\t\t\"messages\": [\n\t\t\t\"java.lang.Exception: Mocked error message\"\n\t\t]\n\t}\n}")
				.endResponseMessage()
				// call the ValidateMembershipJSONRoute
				.to("direct:validateMembershipJSON")
		;
			
	}

}
