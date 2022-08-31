package io.jeannyil.beans;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import io.jeannyil.models.Fruit;
import io.jeannyil.models.Fruits;
import io.jeannyil.models.Legume;
import io.jeannyil.models.Legumes;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * 
 * Helper bean for the fruits and legumes Lists
 *
 */
@ApplicationScoped
@Named("fruitsAndLegumesSetHelper")
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class FruitsAndLegumesSetHelper {
    
    public Set<Fruit> addFruit(Fruit newFruit, Set<Fruit> fruitsSet) {
        fruitsSet.add(newFruit);
        return fruitsSet;
    }

    public Set<Legume> addLegume(Legume newLegume, Set<Legume> legumesSet) {
        legumesSet.add(newLegume);
        return legumesSet;
    }
}
