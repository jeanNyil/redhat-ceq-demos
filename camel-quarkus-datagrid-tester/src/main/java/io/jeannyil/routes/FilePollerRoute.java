package io.jeannyil.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/* FilePoller route definition

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class FilePollerRoute extends RouteBuilder {

    private static String logName = FilePollerRoute.class.getName();

    @Override
    public void configure() throws Exception {

        // Catch unexpected exceptions
		onException(Exception.class)
            .handled(true)
            .maximumRedeliveries(0)
            .log(LoggingLevel.ERROR, ">>> Caught exception: ${exception.stacktrace}")
        ;

        from("file://target/input" +
            "?charset=utf-8" +
            "&readLock=idempotent" +
            "&idempotent=true&idempotentRepository=#infinispanRepo&inProgressRepository=#infinispanRepo" +
            "&idempotentKey=${file:name}" +
            "&delete=true&maxMessagesPerPoll=1" +
            "&doneFileName=${file:name}.done")
            .routeId("file-poller-route")
            .log(LoggingLevel.INFO, logName, ">>> ${hostname} processed ${file:name}: headers[${headers}] - body[${body}]")
        ;

    }
    
}
