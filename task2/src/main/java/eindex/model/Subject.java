package eindex.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Subject implements Serializable {
    private String name;
    private List<Category> categories = new ArrayList<>();

    public Subject() {}

    public Subject(String name, List<Category> categories) {
        this.name = name;
        if (categories != null) this.categories.addAll(categories);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Category> getCategories() { return Collections.unmodifiableList(categories); }

    public void setCategories(List<Category> categories) {
        this.categories.clear();
        if (categories != null) this.categories.addAll(categories);
    }

    public Category findCategory(String catName) {
        for (Category c : categories) if (c.getName().equalsIgnoreCase(catName)) return c;
        return null;
    }

    public int totalMaxPoints() {
        return categories.stream().mapToInt(Category::getMaxPoints).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subject)) return false;
        Subject subject = (Subject) o;
        return Objects.equals(name, subject.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }
}

