package io.jeannyil.routes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import io.jeannyil.constants.DirectEndpointConstants;
import io.jeannyil.models.Fruit;

/* FruitsAndLegumesService endpoints definition

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class FruitsAndLegumesServiceRoutes extends RouteBuilder {

    private static String logName = FruitsAndLegumesServiceRoutes.class.getName();
    
    @Override
    public void configure() throws Exception {

        // Catch unexpected exceptions
		onException(Exception.class)
            .handled(true)
            .maximumRedeliveries(0)
            .log(LoggingLevel.ERROR, logName, ">>> Caught exception: ${exception.stacktrace}")
            .to(DirectEndpointConstants.DIRECT_GENERATE_ERROR_MESSAGE)
        ;
        
        // Implements the getFruits operation
        from("direct:getFruits")
            .routeId("getFruits")
            .log(LoggingLevel.INFO, logName, ">>> Processing GET fruits request...")
            .to("direct:get-fruits-from-cache")
            .log(LoggingLevel.INFO, logName, ">>> Sending GET fruits response: ${body}")
        ;

        // Implements the addFruit operation
        from("direct:addFruit")
            .routeId("addFruit")
            .log(LoggingLevel.INFO, logName, ">>> Processing POST fruits request: ${body}")
            .convertBodyTo(String.class) // Stream caching is enabled on the CamelContext
            .unmarshal()
                .json(JsonLibrary.Jackson, Fruit.class)
            .setProperty("newFruit", body())
            // Get current fruits set from cache
            .to("direct:get-fruits-from-cache")
            .convertBodyTo(String.class) // Stream caching is enabled on the CamelContext
            .unmarshal()
                .json(JsonLibrary.Jackson)
            .log(LoggingLevel.DEBUG, logName, ">>> Body after cache unmarshal: ${body}")
            // Add new Fruit in current Fruits set
            .bean("fruitsAndLegumesSetHelper", "addFruit(${exchangeProperty.newFruit}, ${body})")
            // Put the new Fruits set in cache
            .marshal()
                .json(JsonLibrary.Jackson, true)
            .log(LoggingLevel.DEBUG, logName, ">>> Body after addFruits marshal: ${body}")
            .setProperty("newFruitsSet", body())
            .to("direct:put-fruits-in-cache")
            // New Fruits set as a response for the addFruit operation
            .setBody(exchangeProperty("newFruitsSet"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.CREATED.getStatusCode()))
			.setHeader(Exchange.HTTP_RESPONSE_TEXT, constant(Response.Status.CREATED.getReasonPhrase()))
            .log(LoggingLevel.INFO, logName, ">>> Processing POST fruits DONE: ${body}")
        ;
        
        // Implements the getLegumes operation
        from("direct:getLegumes")
            .routeId("getLegumes")
            .log(LoggingLevel.INFO, logName, ">>> Processing GET legumes request...")
            .to("direct:get-legumes-from-cache")
            .log(LoggingLevel.INFO, logName, ">>> Sending GET legumes response: ${body}")
        ;

    }
}
