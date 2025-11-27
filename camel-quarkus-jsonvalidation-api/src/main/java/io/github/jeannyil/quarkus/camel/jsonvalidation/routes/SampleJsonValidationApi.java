package io.jeannyil.quarkus.camel.jsonvalidation.routes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.camel.CamelContext;   
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/* Exposes the Sample JSON Validation RESTful API with a Contract/API-First approach

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
@ApplicationScoped
public class SampleJsonValidationApi extends RouteBuilder {

	private static String logName = SampleJsonValidationApi.class.getName();
	
	@Inject
	CamelContext camelctx;
    
    @Override
    public void configure() throws Exception {

        // Enable Stream caching
        camelctx.setStreamCaching(true);
        // Enable use of breadcrumbId
        camelctx.setUseBreadcrumb(true);

		// Catch unexpected exceptions
		onException(Exception.class).id("handle-all-other-exceptions")
			.handled(true)
			.maximumRedeliveries(0)
			.log(LoggingLevel.ERROR, logName, ">>> ${routeId} - Caught exception: ${exception.stacktrace}").id("log-api-unexpected")
			.to("direct:common-500").id("to-common-500")
			.log(LoggingLevel.INFO, logName, ">>> ${routeId} - OUT: headers:[${headers}] - body:[${body}]").id("log-api-unexpected-response")
		;

		/**
		 * REST DSL with OpenAPI contract first approach 
         * Reference: https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.14/html/developing_applications_with_red_hat_build_of_apache_camel_for_quarkus/rest-dsl-in-camel-quarkus#camel-quarkus-extensions-rest-dsl-contract-first
		 */
		rest().openApi("classpath:META-INF/openapi.yaml");
			
	}

}
