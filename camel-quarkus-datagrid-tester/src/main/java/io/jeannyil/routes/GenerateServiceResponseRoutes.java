package io.jeannyil.routes;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import io.jeannyil.constants.DirectEndpointConstants;
import java.lang.IllegalArgumentException;

/* Routes that return the services response messages in JSON format

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class GenerateServiceResponseRoutes extends RouteBuilder {
	
	private static String logName = GenerateServiceResponseRoutes.class.getName();

	@Override
	public void configure() throws Exception {
		
		/**
		 * Route that returns the error response message in JSON format
		 * The following properties are expected to be set on the incoming Camel Exchange Message if customization is needed:
		 * <br>- CamelHttpResponseCode ({@link org.apache.camel.Exchange#HTTP_RESPONSE_CODE})
		 * <br>- CamelHttpResponseText ({@link org.apache.camel.Exchange#HTTP_RESPONSE_TEXT})
		 */
		from(DirectEndpointConstants.DIRECT_GENERATE_ERROR_MESSAGE)
			.routeId("generate-error-response-route")
			.log(LoggingLevel.INFO, logName, ">>> IN: headers:[${headers}] - body:[${body}]")
			.filter(simple("${in.header.CamelHttpResponseCode} == null")) // Defaults to 500 HTTP Code
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
				.setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()))
			.end() // end filter
			.setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
			.setBody()
				.method("serviceResponseHelper", 
						"generateErrorMessage(${headers.CamelHttpResponseCode}, ${headers.CamelHttpResponseText}, ${exception})")
			.end()
			.marshal().json(JsonLibrary.Jackson, true)
			.convertBodyTo(String.class)
			.log(LoggingLevel.INFO, logName, ">>> OUT: headers:[${headers}] - body:[${body}]")
		;

		/**
		 * Route that returns the successful response response message in JSON format
		 * The following properties are expected and required on the incoming Camel Exchange Message:
		 * <br>- CamelMinioObjectName ({@link org.apache.camel.component.minio.MinioConstants#OBJECT_NAME})
		 * The following properties are expected to be set on the incoming Camel Exchange Message if customization is needed:
		 * <br>- CamelHttpResponseCode ({@link org.apache.camel.Exchange#HTTP_RESPONSE_CODE})
		 * <br>- CamelHttpResponseText ({@link org.apache.camel.Exchange#HTTP_RESPONSE_TEXT})
		 */
		from(DirectEndpointConstants.DIRECT_GENERATE_OK_MESSAGE)
			.routeId("generate-ok-response-route")
			.filter(simple("${in.header.CamelMinioObjectName} == null"))
				.throwException(new IllegalArgumentException("CamelMinioObjectName (org.apache.camel.component.minio.MinioConstants#OBJECT_NAME}) header is missing on the Camel Exchange!"))
			.end() // end filter
			.setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
			.setBody()
				.method("serviceResponseHelper", 
						"generateResponseMessage(${headers.CamelMinioObjectName}, ${body})")
			.end()
			.marshal().json(JsonLibrary.Jackson, true)
			.convertBodyTo(String.class)
			.log(LoggingLevel.INFO, logName, ">>> OUT: headers:[${headers}] - body:[${body}]")
		;
		
	}

}