package io.jeannyil.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * A REST entity representing a set of fruits.
 */
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class Fruits {

    private List<Fruit> fruits;

    public Fruits() {
    }

    public Fruits(List<Fruit> fruits) {
        this.fruits = fruits;
    }

    public List<Fruit> getFruits() {
        return fruits;
    }

    public void setFruits(List<Fruit> fruits) {
        this.fruits = fruits;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fruits == null) ? 0 : fruits.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Fruits other = (Fruits) obj;
        if (fruits == null) {
            if (other.fruits != null)
                return false;
        } else if (!fruits.equals(other.fruits))
            return false;
        return true;
    }
    
}
