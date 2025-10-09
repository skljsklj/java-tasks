package eindex.service;

import eindex.model.Category;
import eindex.model.Enrollment;
import eindex.model.Role;
import eindex.model.Student;
import eindex.model.Subject;
import eindex.storage.DataStore;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class EIndexService {
    private final DataStore store;
    private final DataStore.State state;

    private static final Pattern INDEX_PATTERN = Pattern.compile("^[Ee][123][/-](200[0-9]|201[0-9]|202[0-3])$");

    public EIndexService(DataStore store) {
        this.store = store;
        this.state = store.load();
    }

    public void save() throws IOException { store.save(state); }

    // AUTH
    public boolean authenticate(String username, String password, Role role) throws IOException {
        return store.validateCredentials(username, password, role);
    }

    public void addAdmin(String username, String password) throws IOException {
        store.addOrUpdateUser(username, password, Role.ADMIN);
    }

    // STUDENTS
    public void addStudent(Student s, String password) throws IllegalArgumentException, IOException {
        validateStudent(s);
        if (state.students.containsKey(s.getUsername()))
            throw new IllegalArgumentException("Student sa datim username vec postoji.");
        state.students.put(s.getUsername(), s);
        store.addOrUpdateUser(s.getUsername(), password, Role.STUDENT);
    }

    private void validateStudent(Student s) {
        if (s.getFirstName() == null || s.getFirstName().isBlank())
            throw new IllegalArgumentException("Ime je obavezno");
        if (s.getLastName() == null || s.getLastName().isBlank())
            throw new IllegalArgumentException("Prezime je obavezno");
        if (s.getUsername() == null || s.getUsername().isBlank())
            throw new IllegalArgumentException("Username je obavezan");
        if (s.getIndexNumber() == null || !INDEX_PATTERN.matcher(s.getIndexNumber()).matches())
            throw new IllegalArgumentException("Broj indeksa nije validan (npr. E2-2015 ili e1/2019)");
        if (!isValidJMBG(s.getJmbg()))
            throw new IllegalArgumentException("JMBG nije validan (13 cifara, validan dan i mesec)");
    }

    private boolean isValidJMBG(String j) {
        if (j == null || !j.matches("\\d{13}")) return false;
        int day = Integer.parseInt(j.substring(0, 2));
        int month = Integer.parseInt(j.substring(2, 4));
        if (day < 1 || day > 31) return false;
        if (month < 1 || month > 12) return false;
        return true; // basic check per spec
    }

    public Collection<Student> listStudents() { return Collections.unmodifiableCollection(state.students.values()); }

    public Student getStudentByUsername(String username) { return state.students.get(username); }

    // Combined list: include users from users.txt with role STUDENT even if not yet in state
    public List<Student> listStudentsCombined() {
        List<Student> result = new ArrayList<>();
        // existing students
        result.addAll(state.students.values());
        try {
            Map<String, eindex.model.User> users = store.readUsers();
            for (eindex.model.User u : users.values()) {
                if (u.getRole() == Role.STUDENT && !state.students.containsKey(u.getUsername())) {
                    // placeholder student with only username filled
                    Student s = new Student(null, null, null, null, u.getUsername());
                    result.add(s);
                }
            }
        } catch (IOException ignored) { }
        return result;
    }

    // SUBJECTS
    public void addSubject(Subject subject) {
        if (subject.getName() == null || subject.getName().isBlank())
            throw new IllegalArgumentException("Naziv predmeta je obavezan");
        int sum = subject.totalMaxPoints();
        if (sum != 100) throw new IllegalArgumentException("Zbir maksimalnih poena mora biti tacno 100");
        for (Category c : subject.getCategories()) {
            if (c.getMinRequired() < 0 || c.getMinRequired() > c.getMaxPoints())
                throw new IllegalArgumentException("Min poeni za kategoriju '" + c.getName() + "' nisu validni");
        }
        state.subjects.put(subject.getName(), subject);
    }

    public Collection<Subject> listSubjects() { return Collections.unmodifiableCollection(state.subjects.values()); }

    public Subject getSubject(String name) { return state.subjects.get(name); }

    // ENROLLMENTS
    public void assignStudentToSubject(String studentUsername, String subjectName) {
        if (!state.students.containsKey(studentUsername))
            throw new IllegalArgumentException("Nepostojeci student");
        if (!state.subjects.containsKey(subjectName))
            throw new IllegalArgumentException("Nepostojeci predmet");

        state.enrollments.computeIfAbsent(studentUsername, k -> new HashMap<>())
                .computeIfAbsent(subjectName, k -> new Enrollment(studentUsername, subjectName));
    }

    public void updatePoints(String studentUsername, String subjectName, Map<String, Integer> updates) {
        Subject subj = getSubject(subjectName);
        if (subj == null) throw new IllegalArgumentException("Nepostojeci predmet");
        Map<String, Enrollment> bySubj = state.enrollments.computeIfAbsent(studentUsername, k -> new HashMap<>());
        Enrollment enr = bySubj.computeIfAbsent(subjectName, k -> new Enrollment(studentUsername, subjectName));
        for (Map.Entry<String, Integer> e : updates.entrySet()) {
            String cat = e.getKey();
            Integer pts = e.getValue();
            Category c = subj.findCategory(cat);
            if (c == null) throw new IllegalArgumentException("Nepostojeca kategorija: " + cat);
            if (pts < 0 || pts > c.getMaxPoints())
                throw new IllegalArgumentException("Poeni za '" + cat + "' moraju biti u [0," + c.getMaxPoints() + "]");
            enr.setPoints(cat, pts);
        }
    }

    public Map<String, Integer> viewPoints(String studentUsername, String subjectName) {
        Subject subj = state.subjects.get(subjectName);
        if (subj == null) throw new IllegalArgumentException("Nepostojeci predmet");
        Enrollment e = state.enrollments.getOrDefault(studentUsername, Collections.emptyMap()).get(subjectName);
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Category c : subj.getCategories()) {
            result.put(c.getName(), e == null ? 0 : e.getPointsByCategory().getOrDefault(c.getName(), 0));
        }
        return result;
    }

    public static class Result implements java.io.Serializable {
        public String subjectName;
        public int total;
        public boolean passed;
        public int grade;
        public Result(String subjectName, int total, boolean passed, int grade) {
            this.subjectName = subjectName; this.total = total; this.passed = passed; this.grade = grade;
        }
        @Override public String toString() {
            return subjectName + ": " + total + "p, ocena " + grade + (passed ? " (polozen)" : " (nije polozen)");
        }
    }

    public List<Result> viewResults(String studentUsername) {
        List<Result> res = new ArrayList<>();
        Map<String, Enrollment> map = state.enrollments.getOrDefault(studentUsername, Collections.emptyMap());
        for (Map.Entry<String, Enrollment> e : map.entrySet()) {
            Subject subj = state.subjects.get(e.getKey());
            if (subj == null) continue;
            Enrollment enr = e.getValue();
            int total = 0;
            boolean perCatMinOk = true;
            for (Category c : subj.getCategories()) {
                int pts = enr.getPointsByCategory().getOrDefault(c.getName(), 0);
                total += pts;
                if (pts < c.getMinRequired()) perCatMinOk = false;
            }
            boolean passed = perCatMinOk && total >= 51;
            int grade = gradeFor(total);
            res.add(new Result(subj.getName(), total, passed, grade));
        }
        return res;
    }

    public static int gradeFor(int total) {
        if (total <= 50) return 5;
        if (total <= 60) return 6;
        if (total <= 70) return 7;
        if (total <= 80) return 8;
        if (total <= 90) return 9;
        return 10;
    }

    public Map<String, Map<String, Enrollment>> listEnrollments() { return state.enrollments; }
}
