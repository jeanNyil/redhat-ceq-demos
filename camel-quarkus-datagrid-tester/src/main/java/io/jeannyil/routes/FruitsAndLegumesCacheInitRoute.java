package io.jeannyil.routes;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import io.jeannyil.models.Fruit;
import io.jeannyil.models.Legume;

/* FruitsAndLegumesCachesInit route definition

/!\ The @ApplicationScoped annotation is required for @Inject and @ConfigProperty to work in a RouteBuilder. 
	Note that the @ApplicationScoped beans are managed by the CDI container and their life cycle is thus a bit 
	more complex than the one of the plain RouteBuilder. 
	In other words, using @ApplicationScoped in RouteBuilder comes with some boot time penalty and you should 
	therefore only annotate your RouteBuilder with @ApplicationScoped when you really need it. */
public class FruitsAndLegumesCacheInitRoute extends RouteBuilder {

    private static String logName = FruitsAndLegumesCacheInitRoute.class.getName();

    private final Set<Fruit> fruits = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Set<Legume> legumes = Collections.synchronizedSet(new LinkedHashSet<>());

    public FruitsAndLegumesCacheInitRoute() {

        /* Let's add some initial fruits */
        this.fruits.add(new Fruit("Apple", "Winter fruit"));
        this.fruits.add(new Fruit("Pineapple", "Tropical fruit"));
        this.fruits.add(new Fruit("Mango", "Tropical fruit"));
        this.fruits.add(new Fruit("Banana", "Tropical fruit"));

        /* Let's add some initial legumes */
        this.legumes.add(new Legume("Carrot", "Root vegetable, usually orange"));
        this.legumes.add(new Legume("Zucchini", "Summer squash"));
    }

    @Override
    public void configure() throws Exception {

        // Catch unexpected exceptions
		onException(Exception.class)
            .handled(true)
            .maximumRedeliveries(0)
            .log(LoggingLevel.ERROR, logName, ">>> Caught exception: ${exception.stacktrace}")
        ;

        from("timer:once?repeatCount=1")
            .routeId("fruits-legumes-cache-init-route")
            .log(LoggingLevel.INFO, logName, ">>> Initializing the {{datagrid.caches.fruits-legumes}}...")
            // fruits
            .setBody().constant(fruits)
            .marshal().json(JsonLibrary.Jackson, true)
            .to("direct:putifabsent-fruits-in-cache")
            // legumes
            .setBody().constant(legumes)
            .marshal().json(JsonLibrary.Jackson, true)
            .to("direct:putifabsent-legumes-in-cache")
            .log(LoggingLevel.INFO, logName, ">>> {{datagrid.caches.fruits-legumes}} is initialized !")
        ;

    }
    
}
