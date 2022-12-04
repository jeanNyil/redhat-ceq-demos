package io.jeannyil.quarkus.camel.xmlvalidation.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import javax.ws.rs.core.Response;

/* Routes to handle common HTTP errors

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class HttpErrorRoute extends RouteBuilder {
	
	private static String logName = HttpErrorRoute.class.getName();

	@Override
	public void configure() throws Exception {
		
		/**
		 * Route that returns the common 500-Internal-Server-Error response in JSON format
		 */
		from("direct:common-500")
			.routeId("common-500-http-code-route")
			.log(LoggingLevel.INFO, logName, ">>> ${routeId} - IN: headers:[${headers}] - body:[${body}]")
			.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
			.setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json")).id("set-500-json-content-type")
			.setBody()
				.method("errorResponseHelper", 
						"generateErrorResponse(${headers.CamelHttpResponseCode}, ${headers.CamelHttpResponseText}, ${exception})")
			.end()
			.marshal().json(JsonLibrary.Jackson, true).id("marshal-500-errorresponse-to-json")
			.convertBodyTo(String.class).id("convert-500-errorresponse-to-string") // Stream caching is enabled on the CamelContext
			.log(LoggingLevel.INFO, logName, ">>> ${routeId} - OUT: headers:[${headers}] - body:[${body}]").id("log-common-500-response")
		;
		
		/**
		 * Route that returns a custom error response in JSON format
		 * The following properties are expected to be set on the incoming Camel Exchange:
		 * <br>- errorId ({@link io.jeannyil.quarkus.camel.xmlvalidation.constants.APIConstants#ERROR_ID})
		 * <br>- errorDescription ({@link io.jeannyil.quarkus.camel.xmlvalidation.constants.APIConstants#ERROR_DESCRIPTION })
		 * <br>- errorMessage ({@link io.jeannyil.quarkus.camel.xmlvalidation.constants.APIConstants#ERROR_MESSAGE })
		 * <br>- httpStatusCode ({@link io.jeannyil.quarkus.camel.xmlvalidation.constants.APIConstants#HTTP_STATUS_CODE })
		 * <br>- httpStatusMsg ({@link io.jeannyil.quarkus.camel.xmlvalidation.constants.APIConstants#HTTP_STATUS_MSG })
		 */
		from("direct:custom-http-error")
			.routeId("custom-http-error-route")
			.log(LoggingLevel.INFO, logName, ">>> ${routeId} - IN: headers:[${headers}] - body:[${body}]")
			.setHeader(Exchange.HTTP_RESPONSE_CODE, simple("${exchangeProperty.httpStatusCode}"))
			.setHeader(Exchange.HTTP_RESPONSE_TEXT, simple("${exchangeProperty.httpStatusMsg}"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
			.setBody()
				.method("errorResponseHelper", 
						"generateErrorResponse(${exchangeProperty.errorId}, ${exchangeProperty.errorDescription}, ${exchangeProperty.errorMessage})")
			.end()
			.marshal().json(JsonLibrary.Jackson, true)
			.convertBodyTo(String.class) // Stream caching is enabled on the CamelContext
			.log(LoggingLevel.INFO, logName, ">>> ${routeId} - OUT: headers:[${headers}] - body:[${body}]")
		;
	}

}