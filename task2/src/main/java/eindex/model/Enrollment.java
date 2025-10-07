package eindex.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Enrollment implements Serializable {
    private String studentUsername;
    private String subjectName;
    private Map<String, Integer> pointsByCategory = new HashMap<>();

    public Enrollment() {}

    public Enrollment(String studentUsername, String subjectName) {
        this.studentUsername = studentUsername;
        this.subjectName = subjectName;
    }

    public String getStudentUsername() { return studentUsername; }
    public String getSubjectName() { return subjectName; }

    public Map<String, Integer> getPointsByCategory() { return pointsByCategory; }

    public void setPoints(String categoryName, int points) {
        pointsByCategory.put(categoryName, points);
    }

    public int totalPoints() {
        return pointsByCategory.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment)) return false;
        Enrollment that = (Enrollment) o;
        return Objects.equals(studentUsername, that.studentUsername) &&
               Objects.equals(subjectName, that.subjectName);
    }

    @Override
    public int hashCode() { return Objects.hash(studentUsername, subjectName); }
}

