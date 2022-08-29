package io.jeannyil.beans;

import java.util.List;

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
@Named("fruitsAndLegumesArrayHelper")
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class FruitsAndLegumesArrayHelper {
    
    public Fruits addFruit(Fruit newFruit, Fruits fruitsArray) {
        List<Fruit> currentFruits = fruitsArray.getFruits();
        currentFruits.add(newFruit);
        fruitsArray.setFruits(currentFruits);
        return fruitsArray;
    }

    public Legumes addLegume(Legume newLegume, Legumes legumesArray) {
        List<Legume> currentLegumes = legumesArray.getLegumes();
        currentLegumes.add(newLegume);
        legumesArray.setLegumes(currentLegumes);
        return legumesArray;
    }
}
