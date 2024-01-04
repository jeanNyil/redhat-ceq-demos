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

import io.jeannyil.models.Fruit;

/* FruitsAndLegumesApi route definition

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
@ApplicationScoped
public class FruitsAndLegumesApiRoute extends RouteBuilder {

    private static String logName = FruitsAndLegumesApiRoute.class.getName();

    @Inject
	CamelContext camelctx;
    
    @Override
    public void configure() throws Exception {

        // Enable Stream caching
        camelctx.setStreamCaching(true);
        // Enable use of breadcrumbId
        camelctx.setUseBreadcrumb(true);

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
        
        //REST configuration with Camel Quarkus Platform HTTP component
        restConfiguration()
            .component("platform-http")
            .enableCORS(true)
            .bindingMode(RestBindingMode.off) // RESTful responses will be explicitly marshaled for logging purposes
            .dataFormatProperty("prettyPrint", "true")
            .contextPath("/")
        ;

        // REST endpoint for the FruitsAndLegumesApi OpenAPI specification
        rest()
            .produces(MediaType.APPLICATION_JSON)
            .get("/openapi.json")
                .id("get-oas-route")
                .description("Gets the OpenAPI specification for this service in JSON format")
                .to("direct:getOAS")
        ;

        // Returns the OAS
        from("direct:getOAS")
            .log(LoggingLevel.INFO, logName, ">>> IN: headers:[${headers}] - body:[${body}]")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/vnd.oai.openapi+json"))
            .setBody().constant("resource:classpath:openapi/openapi.json")
            .log(LoggingLevel.INFO, logName, ">>> OUT: headers:[${headers}] - body:[${body}]")
        ;

        // REST endpoint for the fruits API
        rest("/fruits")
            .get()
                .id("get-fruits-route")
                .produces(MediaType.APPLICATION_JSON)
                .description("Returns a list of hard-coded and added fruits")
                // Call the getFruits route
                .to("direct:getFruits")
            
            // Adds a fruit
            .post()
                .id("add-fruit-route")
                .consumes(MediaType.APPLICATION_JSON)
                .produces(MediaType.APPLICATION_JSON)
                .description("Adds a fruit")
                // Call the getFruits route
                .to("direct:addFruit")
        ;

        // REST endpoint for the legumes API
        rest("/legumes")
            .get()
                .id("get-legumes-route")
                .produces(MediaType.APPLICATION_JSON)
                .description("Returns a list of hard-coded legumes")
                // Call the getFruits route
                .to("direct:getLegumes")
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
