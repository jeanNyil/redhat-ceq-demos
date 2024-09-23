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

/* FtpFileUploaderServiceRoute route definition

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class FtpFileUploaderServiceRoute extends RouteBuilder {
    
    private static String logName = FtpFileUploaderServiceRoute.class.getName();
    
    @Override
    public void configure() throws Exception {

        // Catch unexpected exceptions
		onException(Exception.class)
            .handled(true)
            .maximumRedeliveries(0)
            .log(LoggingLevel.ERROR, logName, ">>> Caught exception: ${exception.stacktrace}")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
            .setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()))
            .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN))
            .setBody(simple("${exception}"))
            .log(LoggingLevel.INFO, logName, ">>> OUT: headers:[${headers}] - body:[${body}]")
        ;

        // REST endpoint for the FruitsAndLegumesService
        rest("/ftp-file-uploader-service")
            .produces(MediaType.APPLICATION_JSON)
            .get("/openapi.json")
                .id("get-ftp-file-uploader-service-oas-rest")
                .description("Gets the OpenAPI specification for this service in JSON format")
                .to("direct:get-ftp-file-uploader-service-oas")
            // Upload the fruits.csv file to FTP server
            .post("csv")
                .id("upload-fruits-csv-rest")
                .produces(MediaType.APPLICATION_JSON)
                .description("Upload the fruits.csv file to FTP server")
                // Call the uploadCsvFile route
                .to("direct:uploadCsvFile")
        ;

        // Returns the FtpFileUploaderService OAS
        from("direct:get-ftp-file-uploader-service-oas")
            .routeId("get-ftp-file-uploader-service-oas-route")
            .log(LoggingLevel.INFO, logName, ">>> IN: headers:[${headers}] - body:[${body}]")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/vnd.oai.openapi+json"))
            .setBody().constant("resource:classpath:openapi/ftp-file-uploader-service.json")
            .log(LoggingLevel.INFO, logName, ">>> OUT: headers:[${headers}] - body:[${body}]")
        ;

        // Implements the uploadCsvFile operation
        from("direct:uploadCsvFile")
            .routeId("uploadCsvFile")
            .log(LoggingLevel.INFO, logName, ">>> Uploading the fruits.csv file to FTP server...")
            .setBody().constant("resource:classpath:ftp-test-files/fruits.csv")
            .to("ftp://{{ftp-server.username}}@{{ftp-server.host}}/{{ftp-server.directory}}" +
                "?password={{ftp-server.password}}" +
                "&ftpClient.dataTimeout={{ftp-server.data-timeout-inms}}" +
                "&charset=utf-8" +
                "&passiveMode=true" +
                "&fileName=fruits.csv" +
                "&doneFileName=fruits.csv.done")
            .log(LoggingLevel.INFO, logName, ">>> fruits.csv file uploaded to FTP server - DONE!")
            // TO COMPLETE
        ;

    }
}