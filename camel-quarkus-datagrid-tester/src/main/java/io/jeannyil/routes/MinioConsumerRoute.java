package io.jeannyil.routes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.minio.MinioConstants;
import org.apache.camel.component.infinispan.remote.InfinispanRemoteIdempotentRepository;

/* MinioConsumer route definition

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
@ApplicationScoped
public class MinioConsumerRoute extends RouteBuilder {

    private static String logName = MinioConsumerRoute.class.getName();

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

        from("minio://{{minio.bucket-name}}" +
             "?autoCreateBucket=true" +
             "&endpoint={{minio.endpoint}}" +
             "&secure=true" +
             "&accessKey={{minio.access-key}}" +
             "&secretKey={{minio.secret-key}}" +
             "&deleteAfterRead={{minio.delete-after-read:false}}" + // I deactivated the delete after object retrieval for the Idempotent Consumer EIP using RHDG demo purposes. Default is usually `true`.
             "&delay={{minio.next-poll-delay-in-ms:30000}}") // Milliseconds before the next poll. I set it to 30000ms for demo purposes. Default is usually `500`.
            .routeId("minio-consumer-route")
            .idempotentConsumer(header(MinioConstants.OBJECT_NAME), repo) // Idempotent Consumer EIP
                .log(LoggingLevel.INFO, logName, ">>> [${hostname}] - Processed ${header.CamelMinioObjectName}:\n${body}")
                .stop() // Stops routing the message at the end of the Idempotent Consumer EIP
            .end() // end Idempotent Consumer EIP
            .log(LoggingLevel.WARN, logName, ">>> [${hostname}] - Ignored ${header.CamelMinioObjectName} because it was already processed!")
        ;

    }
    
}
