package io.jeannyil.routes;

import jakarta.ws.rs.core.Response;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;

import io.jeannyil.constants.DirectEndpointConstants;

/* File uploader service routes: POST sample fruits files into the configured S3 bucket
   (NooBaa / Multi-Cloud Object Gateway on OpenShift). REST paths are
   /api/v1/s3-file-uploader-service/*.

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder.
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit
	more complex than the one of the plain RouteBuilder.
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class S3FileUploaderServiceRoutes extends RouteBuilder {

    private static String logName = S3FileUploaderServiceRoutes.class.getName();

    private static final String S3_PRODUCER_URI =
            "aws2-s3://{{s3.bucket-name}}" +
            "?overrideEndpoint=true" +
            "&uriEndpointOverride={{s3.endpoint}}" +
            "&forcePathStyle=true" +
            "&region={{s3.region}}" +
            "&accessKey={{s3.access-key}}" +
            "&secretKey={{s3.secret-key}}" +
            "&autoCreateBucket=false" +
            "&trustAllCertificates=true";

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
            .setHeader(AWS2S3Constants.KEY, simple("fruits-${date:now:yyyyMMdd-HHmmss}.csv"))
            .log(LoggingLevel.INFO, logName, ">>> Uploading ${header.CamelAwsS3Key} to S3...")
            .setBody().constant("resource:classpath:s3-test-files/fruits.csv")
            .log(LoggingLevel.DEBUG, logName, ">>> ${header.CamelAwsS3Key} file content:\n${body}")
            .to(S3_PRODUCER_URI)
            .log(LoggingLevel.INFO, logName, ">>> ${header.CamelAwsS3Key} uploaded to S3 - DONE!")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.CREATED.getStatusCode()))
			.setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.CREATED.getReasonPhrase()))
            .to(DirectEndpointConstants.DIRECT_GENERATE_OK_MESSAGE)
        ;

        // Implements the uploadJsonFile operation
        from("direct:uploadJsonFile")
            .routeId("uploadJsonFile")
            .setHeader(AWS2S3Constants.KEY, simple("fruits-${date:now:yyyyMMdd-HHmmss}.json"))
            .log(LoggingLevel.INFO, logName, ">>> Uploading ${header.CamelAwsS3Key} to S3...")
            .setBody().constant("resource:classpath:s3-test-files/fruits.json")
            .log(LoggingLevel.DEBUG, logName, ">>> ${header.CamelAwsS3Key} file content:\n${body}")
            .to(S3_PRODUCER_URI)
            .log(LoggingLevel.INFO, logName, ">>> ${header.CamelAwsS3Key} uploaded to S3 - DONE!")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.CREATED.getStatusCode()))
			.setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.CREATED.getReasonPhrase()))
            .to(DirectEndpointConstants.DIRECT_GENERATE_OK_MESSAGE)
        ;

        // Implements the uploadXmlFile operation
        from("direct:uploadXmlFile")
            .routeId("uploadXmlFile")
            .setHeader(AWS2S3Constants.KEY, simple("fruits-${date:now:yyyyMMdd-HHmmss}.xml"))
            .log(LoggingLevel.INFO, logName, ">>> Uploading ${header.CamelAwsS3Key} to S3...")
            .setBody().constant("resource:classpath:s3-test-files/fruits.xml")
            .log(LoggingLevel.DEBUG, logName, ">>> ${header.CamelAwsS3Key} file content:\n${body}")
            .to(S3_PRODUCER_URI)
            .log(LoggingLevel.INFO, logName, ">>> ${header.CamelAwsS3Key} uploaded to S3 - DONE!")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.CREATED.getStatusCode()))
			.setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.CREATED.getReasonPhrase()))
            .to(DirectEndpointConstants.DIRECT_GENERATE_OK_MESSAGE)
        ;

    }
}
