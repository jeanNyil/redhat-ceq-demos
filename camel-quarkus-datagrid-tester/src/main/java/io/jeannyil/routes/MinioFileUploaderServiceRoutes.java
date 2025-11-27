package io.jeannyil.routes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.component.minio.MinioConstants;

import io.jeannyil.constants.DirectEndpointConstants;

/* MinioFileUploaderService endpoints definition

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class MinioFileUploaderServiceRoutes extends RouteBuilder {
    
    private static String logName = MinioFileUploaderServiceRoutes.class.getName();
    
    @Override
    public void configure() throws Exception {

        // Catch unexpected exceptions
		onException(Exception.class)
            .handled(true)
            .maximumRedeliveries(0)
            .log(LoggingLevel.ERROR, logName, ">>> Caught exception: ${exception.stacktrace}")
            .to(DirectEndpointConstants.DIRECT_GENERATE_ERROR_MESSAGE)
        ;

        // Implements the uploadCsvFile operation
        from("direct:uploadCsvFile")
            .routeId("uploadCsvFile")
            .setHeader(MinioConstants.OBJECT_NAME, simple("fruits-${date:now:yyyyMMdd-HHmmss}.csv"))
            .log(LoggingLevel.INFO, logName, ">>> Uploading ${header.CamelMinioObjectName} to MinIO server...")
            .setBody().constant("resource:classpath:minio-test-files/fruits.csv")
            .log(LoggingLevel.DEBUG, logName, ">>> ${header.CamelMinioObjectName} file content:\n${body}")
            .to("minio://camel-quarkus-datagrid-tester" +
                "?autoCreateBucket=true" +
                "&endpoint={{minio.endpoint}}" +
                "&secure=true" +
                "&accessKey={{minio.access-key}}" +
                "&secretKey={{minio.secret-key}}")
            .log(LoggingLevel.INFO, logName, ">>> ${header.CamelMinioObjectName} uploaded to MinIO server - DONE!")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.CREATED.getStatusCode()))
			.setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.CREATED.getReasonPhrase()))
            .to(DirectEndpointConstants.DIRECT_GENERATE_OK_MESSAGE)
        ;

        // Implements the uploadJsonFile operation
        from("direct:uploadJsonFile")
            .routeId("uploadJsonFile")
            .setHeader(MinioConstants.OBJECT_NAME, simple("fruits-${date:now:yyyyMMdd-HHmmss}.json"))
            .log(LoggingLevel.INFO, logName, ">>> Uploading ${header.CamelMinioObjectName} to MinIO server...")
            .setBody().constant("resource:classpath:minio-test-files/fruits.json")
            .log(LoggingLevel.DEBUG, logName, ">>> ${header.CamelMinioObjectName} file content:\n${body}")
            .to("minio://camel-quarkus-datagrid-tester" +
                "?autoCreateBucket=true" +
                "&endpoint={{minio.endpoint}}" +
                "&secure=true" +
                "&accessKey={{minio.access-key}}" +
                "&secretKey={{minio.secret-key}}")
            .log(LoggingLevel.INFO, logName, ">>> ${header.CamelMinioObjectName} uploaded to MinIO server - DONE!")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.CREATED.getStatusCode()))
			.setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.CREATED.getReasonPhrase()))
            .to(DirectEndpointConstants.DIRECT_GENERATE_OK_MESSAGE)
        ;

        // Implements the uploadXmlFile operation
        from("direct:uploadXmlFile")
            .routeId("uploadXmlFile")
            .setHeader(MinioConstants.OBJECT_NAME, simple("fruits-${date:now:yyyyMMdd-HHmmss}.xml"))
            .log(LoggingLevel.INFO, logName, ">>> Uploading ${header.CamelMinioObjectName} to MinIO server...")
            .setBody().constant("resource:classpath:minio-test-files/fruits.xml")
            .log(LoggingLevel.DEBUG, logName, ">>> ${header.CamelMinioObjectName} file content:\n${body}")
            .to("minio://camel-quarkus-datagrid-tester" +
                "?autoCreateBucket=true" +
                "&endpoint={{minio.endpoint}}" +
                "&secure=true" +
                "&accessKey={{minio.access-key}}" +
                "&secretKey={{minio.secret-key}}")
            .log(LoggingLevel.INFO, logName, ">>> ${header.CamelMinioObjectName} uploaded to MinIO server - DONE!")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.CREATED.getStatusCode()))
			.setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.CREATED.getReasonPhrase()))
            .to(DirectEndpointConstants.DIRECT_GENERATE_OK_MESSAGE)
        ;

    }
}