package io.jeannyil.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.infinispan.InfinispanConstants;
import org.apache.camel.component.infinispan.InfinispanOperation;

/* DataGridOperations routes definitions

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class DataGridOperationsRoutes extends RouteBuilder {

    private static String logName = DataGridOperationsRoutes.class.getName();
    
    @Override
    public void configure() throws Exception {

        // PUT fruits in cache - expects the payload in the message body
        from("direct:put-fruits-in-cache")
            .routeId("put-fruits-in-cache-route")
            .log(LoggingLevel.INFO, logName, ">>> Putting fruits in the {{datagrid.caches.fruits-legumes}}...")
            .removeHeaders("*", "breadcrumbId")
            .setHeader(InfinispanConstants.OPERATION, constant(InfinispanOperation.PUT))
            .setHeader(InfinispanConstants.KEY, constant("fruits"))
            .convertBodyTo(String.class) // Stream caching is enabled on the CamelContext
            .setHeader(InfinispanConstants.VALUE, simple("${body}"))
            .to("infinispan:{{datagrid.caches.fruits-legumes}}")
            .log(LoggingLevel.INFO, logName, ">>> Fruits put in the {{datagrid.caches.fruits-legumes}}: headers[${headers}]")
        ;

        // PUT (IF ABSENT) fruits in cachen - expects the payload in the message body
        from("direct:putifabsent-fruits-in-cache")
            .routeId("putifabsent-fruits-in-cache-route")
            .log(LoggingLevel.INFO, logName, ">>> Putting fruits (IF ABSENT) in the {{datagrid.caches.fruits-legumes}}...")
            .removeHeaders("*", "breadcrumbId")
            .setHeader(InfinispanConstants.OPERATION, constant(InfinispanOperation.PUTIFABSENT))
            .setHeader(InfinispanConstants.KEY, constant("fruits"))
            .convertBodyTo(String.class) // Stream caching is enabled on the CamelContext
            .setHeader(InfinispanConstants.VALUE, simple("${body}"))
            .to("infinispan:{{datagrid.caches.fruits-legumes}}")
            .log(LoggingLevel.INFO, logName, ">>> Fruits put in the {{datagrid.caches.fruits-legumes}}: headers[${headers}]")
        ;

        // GET fruits from cache
        from("direct:get-fruits-from-cache")
            .routeId("get-fruits-from-cache-route")
            .log(LoggingLevel.INFO, logName, "Getting fruits from the {{datagrid.caches.fruits-legumes}}...")
            .removeHeaders("*", "breadcrumbId")
            .setHeader(InfinispanConstants.OPERATION, constant(InfinispanOperation.GET))
            .setHeader(InfinispanConstants.KEY, constant("fruits"))
            .to("infinispan:{{datagrid.caches.fruits-legumes}}")
            .log(LoggingLevel.INFO, logName, "Fruits from the {{datagrid.caches.fruits-legumes}}: headers[${headers}] - body[${body}]")
        ;

        // PUT legumes in cache - expects the payload in the message body
        from("direct:put-legumes-in-cache")
            .routeId("put-legumes-in-cache-route")
            .log(LoggingLevel.INFO, logName, ">>> Putting legumes in the {{datagrid.caches.fruits-legumes}}...")
            .removeHeaders("*", "breadcrumbId")
            .setHeader(InfinispanConstants.OPERATION, constant(InfinispanOperation.PUT))
            .setHeader(InfinispanConstants.KEY, constant("legumes"))
            .convertBodyTo(String.class) // Stream caching is enabled on the CamelContext
            .setHeader(InfinispanConstants.VALUE, simple("${body}"))
            .to("infinispan:{{datagrid.caches.fruits-legumes}}")
            .log(LoggingLevel.INFO, logName, ">>> Legumes put in the {{datagrid.caches.fruits-legumes}}: headers[${headers}]")
        ;

        // PUT (IF ABSENT) legumes in cache - expects the payload in the message body
        from("direct:putifabsent-legumes-in-cache")
            .routeId("putifabsent-legumes-in-cache-route")
            .log(LoggingLevel.INFO, logName, ">>> Putting legumes (IF ABSENT) in the {{datagrid.caches.fruits-legumes}}...")
            .removeHeaders("*", "breadcrumbId")
            .setHeader(InfinispanConstants.OPERATION, constant(InfinispanOperation.PUTIFABSENT))
            .setHeader(InfinispanConstants.KEY, constant("legumes"))
            .convertBodyTo(String.class) // Stream caching is enabled on the CamelContext
            .setHeader(InfinispanConstants.VALUE, simple("${body}"))
            .to("infinispan:{{datagrid.caches.fruits-legumes}}")
            .log(LoggingLevel.INFO, logName, ">>> Legumes put in the {{datagrid.caches.fruits-legumes}}: headers[${headers}]")
        ;

        // GET legumes from cache
        from("direct:get-legumes-from-cache")
            .routeId("get-legumes-from-cache-route")
            .log(LoggingLevel.INFO, logName, ">>> Getting legumes from the {{datagrid.caches.fruits-legumes}}...")
            .removeHeaders("*", "breadcrumbId")
            .setHeader(InfinispanConstants.OPERATION, constant(InfinispanOperation.GET))
            .setHeader(InfinispanConstants.KEY, constant("legumes"))
            .to("infinispan:{{datagrid.caches.fruits-legumes}}")
            .log(LoggingLevel.INFO, logName, ">>> Legumes from the {{datagrid.caches.fruits-legumes}}: headers[${headers}] - body[${body}]")
        ;
    }
    
}
