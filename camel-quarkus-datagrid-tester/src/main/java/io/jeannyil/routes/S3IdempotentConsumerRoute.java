package io.jeannyil.routes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.infinispan.remote.InfinispanRemoteIdempotentRepository;

/* S3 consumer route for the Idempotent Consumer EIP demo against NooBaa / Multi-Cloud Object Gateway
   (or any S3-compatible endpoint configured via s3.* properties).

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder.
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit
	more complex than the one of the plain RouteBuilder.
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
@ApplicationScoped
public class S3IdempotentConsumerRoute extends RouteBuilder {

    private static String logName = S3IdempotentConsumerRoute.class.getName();

    @Inject
    InfinispanRemoteIdempotentRepository repo;

    @Override
    public void configure() throws Exception {

        // Catch unexpected exceptions
		onException(Exception.class)
            .handled(true)
            .maximumRedeliveries(0)
            .log(LoggingLevel.ERROR, ">>> Caught exception: ${exception.stacktrace}")
        ;

        from("aws2-s3://{{s3.bucket-name}}" +
             "?amazonS3Client=#s3Client" +
             "&autoCreateBucket=false" +
             "&deleteAfterRead={{s3.delete-after-read:false}}" + // deactivated for the Idempotent Consumer EIP using RHDG demo purposes. Default is usually `true`.
             "&delay={{s3.next-poll-delay-in-ms:30000}}") // Milliseconds before the next poll. Demo default 30000ms; component default is usually `500`.
            .routeId("s3-idempotent-consumer-route")
            .idempotentConsumer(header(AWS2S3Constants.KEY), repo) // Idempotent Consumer EIP
                .log(LoggingLevel.INFO, logName, ">>> [${hostname}] - Processed ${header.CamelAwsS3Key}:\n${body}")
                .stop() // Stops routing the message at the end of the Idempotent Consumer EIP
            .end() // end Idempotent Consumer EIP
            .log(LoggingLevel.WARN, logName, ">>> [${hostname}] - Ignored ${header.CamelAwsS3Key} because it was already processed!")
        ;

    }

}
