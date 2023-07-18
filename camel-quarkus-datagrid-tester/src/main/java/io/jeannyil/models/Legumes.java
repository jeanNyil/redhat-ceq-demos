package io.jeannyil.models;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * A REST entity representing a set of legumes.
 */
@RegisterForReflection // Lets Quarkus register this class for reflection during the native build
public class Legumes {
    
    private List<Legume> legumes;

    public Legumes() {
    }

    public Legumes(List<Legume> legumes) {
        this.legumes = legumes;
    }

    public List<Legume> getLegumes() {
        return legumes;
    }

    public void setLegumes(List<Legume> legumes) {
        this.legumes = legumes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((legumes == null) ? 0 : legumes.hashCode());
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
        Legumes other = (Legumes) obj;
        if (legumes == null) {
            if (other.legumes != null)
                return false;
        } else if (!legumes.equals(other.legumes))
            return false;
        return true;
    }
    
}
