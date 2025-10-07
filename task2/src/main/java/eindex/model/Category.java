package eindex.model;

import java.io.Serializable;
import java.util.Objects;

public class Category implements Serializable {
    private String name;
    private int maxPoints;
    private int minRequired;

    public Category() {}

    public Category(String name, int maxPoints, int minRequired) {
        this.name = name;
        this.maxPoints = maxPoints;
        this.minRequired = minRequired;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxPoints() { return maxPoints; }
    public void setMaxPoints(int maxPoints) { this.maxPoints = maxPoints; }

    public int getMinRequired() { return minRequired; }
    public void setMinRequired(int minRequired) { this.minRequired = minRequired; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category that = (Category) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }
}

