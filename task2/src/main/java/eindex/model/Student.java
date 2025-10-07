package eindex.model;

import java.io.Serializable;
import java.util.Objects;

public class Student implements Serializable {
    private String firstName;
    private String lastName;
    private String indexNumber; // e.g., E2-2015 or e1/2019
    private String jmbg; // 13 digits
    private String username; // must exist in users.txt with role STUDENT

    public Student() {}

    public Student(String firstName, String lastName, String indexNumber, String jmbg, String username) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.indexNumber = indexNumber;
        this.jmbg = jmbg;
        this.username = username;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getIndexNumber() { return indexNumber; }
    public void setIndexNumber(String indexNumber) { this.indexNumber = indexNumber; }

    public String getJmbg() { return jmbg; }
    public void setJmbg(String jmbg) { this.jmbg = jmbg; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student student = (Student) o;
        return Objects.equals(username, student.username);
    }

    @Override
    public int hashCode() { return Objects.hash(username); }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + indexNumber + ")";
    }
}

