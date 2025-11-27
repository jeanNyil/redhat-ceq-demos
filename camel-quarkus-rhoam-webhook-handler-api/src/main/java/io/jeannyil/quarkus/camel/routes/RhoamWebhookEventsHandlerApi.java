package io.jeannyil.quarkus.camel.routes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.camel.CamelContext;   
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import io.jeannyil.quarkus.camel.constants.DirectEndpointConstants;

/* Exposes the RHOAM Webhook Events Handler API with a Contract/API-First approach

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
@ApplicationScoped
public class RhoamWebhookEventsHandlerApi extends RouteBuilder {

	private static String logName = RhoamWebhookEventsHandlerApi.class.getName();

	@Inject
	CamelContext camelctx;
	
	@Override
	public void configure() throws Exception {

		// Enable Stream caching
        camelctx.setStreamCaching(true);
        // Enable use of breadcrumbId
        camelctx.setUseBreadcrumb(true);
		
		/**
		 * Catch unexpected exceptions
		 */
		onException(Exception.class)
			.handled(true)
			.maximumRedeliveries(0)
			.log(LoggingLevel.ERROR, logName, ">>> ${routeId} - Caught exception: ${exception.stacktrace}").id("log-api-unexpected")
			.to(DirectEndpointConstants.DIRECT_GENERATE_ERROR_MESSAGE).id("generate-api-500-errorresponse")
			.log(LoggingLevel.INFO, logName, ">>> ${routeId} - OUT: headers:[${headers}] - body:[${body}]").id("log-api-unexpected-response")
		;
		
		/**
		 * REST DSL with OpenAPI contract first approach 
         * Reference: https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.14/html/developing_applications_with_red_hat_build_of_apache_camel_for_quarkus/rest-dsl-in-camel-quarkus#camel-quarkus-extensions-rest-dsl-contract-first
		 */
		rest().openApi("classpath:META-INF/openapi.yaml");
			
	}

}
