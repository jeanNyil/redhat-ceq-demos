package io.jeannyil.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/* MinioConsumer route definition

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class MinioConsumerRoute extends RouteBuilder {

    private static String logName = MinioConsumerRoute.class.getName();

    @Override
    public void configure() throws Exception {

        // Catch unexpected exceptions
		onException(Exception.class)
            .handled(true)
            .maximumRedeliveries(0)
            .log(LoggingLevel.ERROR, ">>> Caught exception: ${exception.stacktrace}")
        ;

        // "&readLock=idempotent" +
        // "&idempotent=true&idempotentRepository=#infinispanRepo&inProgressRepository=#infinispanRepo" +
        // "&idempotentKey=${file:name}" +
        from("minio://camel-quarkus-datagrid-tester" +
             "?autoCreateBucket=true" +
             "&endpoint={{minio.endpoint}}" +
             "&secure=true" +
             "&accessKey={{minio.access-key}}" +
             "&secretKey={{minio.secret-key}}" +
             "&deleteAfterRead=true")
            .routeId("minio-consumer-route")
            .log(LoggingLevel.INFO, logName, ">>> ${hostname} processed ${file:name}: headers[${headers}] - body[${body}]")
        ;

    }
    
}
