package eindex.storage;

import eindex.model.Role;
import eindex.model.Student;
import eindex.model.Subject;
import eindex.model.User;
import eindex.model.Enrollment;
import eindex.model.Category;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DataStore {
    private final Path usersPath;
    private final Path stateSerPath;
    private final Path stateXmlPath;

    public static class State implements Serializable {
        public Map<String, Student> students = new HashMap<>(); // key: username
        public Map<String, Subject> subjects = new HashMap<>(); // key: subject name
        public Map<String, Map<String, Enrollment>> enrollments = new HashMap<>(); // user -> subj -> enrollment
    }

    public DataStore(Path baseDir) {
        this.usersPath = baseDir.resolve("users.txt");
        this.stateSerPath = baseDir.resolve("state.ser");
        this.stateXmlPath = baseDir.resolve("state.xml");
    }

    // USERS.TXT MANAGEMENT
    public void ensureDefaultAdmin() throws IOException {
        if (!Files.exists(usersPath)) {
            Files.createDirectories(usersPath.getParent());
            Files.write(usersPath, Arrays.asList("admin:admin:admin"), StandardCharsets.UTF_8);
        } else if (Files.size(usersPath) == 0) {
            Files.write(usersPath, Arrays.asList("admin:admin:admin"), StandardCharsets.UTF_8);
        }
    }

    public Map<String, User> readUsers() throws IOException {
        ensureDefaultAdmin();
        Map<String, User> map = new HashMap<>();
        for (String line : Files.readAllLines(usersPath, StandardCharsets.UTF_8)) {
            line = line.replace("\uFEFF", ""); // strip BOM if present
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split(":");
            if (parts.length != 3) continue;
            String u = parts[0].trim();
            String p = parts[1].trim();
            String r = parts[2].trim();
            Role role = r.equalsIgnoreCase("admin") ? Role.ADMIN : Role.STUDENT;
            map.put(u, new User(u, p, role));
        }
        return map;
    }

    public synchronized void addOrUpdateUser(String username, String password, Role role) throws IOException {
        Map<String, User> users = readUsers();
        users.put(username, new User(username, password, role));
        List<String> lines = new ArrayList<>();
        for (User u : users.values()) {
            String r = u.getRole() == Role.ADMIN ? "admin" : "student";
            lines.add(u.getUsername() + ":" + u.getPassword() + ":" + r);
        }
        lines.sort(Comparator.naturalOrder());
        Files.write(usersPath, lines, StandardCharsets.UTF_8);
    }

    public boolean validateCredentials(String username, String password, Role role) throws IOException {
        Map<String, User> users = readUsers();
        User u = users.get(username);
        return u != null && Objects.equals(u.getPassword(), password) && u.getRole() == role;
    }

    // STATE PERSISTENCE (XML primary, with migration)
    public synchronized void save(State state) throws IOException {
        try {
            writeXml(state);
        } catch (Exception e) {
            throw new IOException("XML save failed: " + e.getMessage(), e);
        }
    }

    public synchronized State load() {
        // Primary: XML
        if (Files.exists(stateXmlPath)) {
            try { return readXml(stateXmlPath); } catch (Exception ignored) {}
        }
        // Migration from legacy .ser
        if (Files.exists(stateSerPath)) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(stateSerPath))) {
                Object obj = ois.readObject();
                if (obj instanceof State) {
                    State s = (State) obj;
                    try { writeXml(s); } catch (Exception ignored) {}
                    return s;
                }
            } catch (Exception ignored) {}
        }
        return new State();
    }

    // XML helpers
    private void writeXml(State state) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("state");
        doc.appendChild(root);

        // students
        Element studentsEl = doc.createElement("students");
        root.appendChild(studentsEl);
        for (Student s : state.students.values()) {
            Element st = doc.createElement("student");
            st.setAttribute("username", nullSafe(s.getUsername()));
            appendText(doc, st, "firstName", s.getFirstName());
            appendText(doc, st, "lastName", s.getLastName());
            appendText(doc, st, "indexNumber", s.getIndexNumber());
            appendText(doc, st, "jmbg", s.getJmbg());
            studentsEl.appendChild(st);
        }

        // subjects
        Element subjectsEl = doc.createElement("subjects");
        root.appendChild(subjectsEl);
        for (Subject subj : state.subjects.values()) {
            Element su = doc.createElement("subject");
            su.setAttribute("name", nullSafe(subj.getName()));
            for (Category c : subj.getCategories()) {
                Element cat = doc.createElement("category");
                cat.setAttribute("name", nullSafe(c.getName()));
                cat.setAttribute("maxPoints", String.valueOf(c.getMaxPoints()));
                cat.setAttribute("minRequired", String.valueOf(c.getMinRequired()));
                su.appendChild(cat);
            }
            subjectsEl.appendChild(su);
        }

        // enrollments
        Element enrollEl = doc.createElement("enrollments");
        root.appendChild(enrollEl);
        for (Map.Entry<String, Map<String, Enrollment>> e : state.enrollments.entrySet()) {
            String user = e.getKey();
            for (Enrollment en : e.getValue().values()) {
                Element enr = doc.createElement("enrollment");
                enr.setAttribute("studentUsername", nullSafe(user));
                enr.setAttribute("subjectName", nullSafe(en.getSubjectName()));
                Element pts = doc.createElement("points");
                for (Map.Entry<String,Integer> pe : en.getPointsByCategory().entrySet()) {
                    Element pc = doc.createElement("category");
                    pc.setAttribute("name", pe.getKey());
                    pc.setAttribute("points", String.valueOf(pe.getValue()));
                    pts.appendChild(pc);
                }
                enr.appendChild(pts);
                enrollEl.appendChild(enr);
            }
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        try (OutputStream os = Files.newOutputStream(stateXmlPath)) {
            t.transform(new DOMSource(doc), new StreamResult(os));
        }
    }

    private State readXml(Path path) throws Exception {
        State state = new State();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        try (InputStream is = Files.newInputStream(path)) {
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();

            // students
            NodeList students = doc.getElementsByTagName("student");
            for (int i = 0; i < students.getLength(); i++) {
                Node n = students.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) continue;
                Element el = (Element) n;
                String un = el.getAttribute("username");
                String fn = textContent(el, "firstName");
                String ln = textContent(el, "lastName");
                String idx = textContent(el, "indexNumber");
                String jmbg = textContent(el, "jmbg");
                if (un != null && !un.isEmpty()) {
                    state.students.put(un, new Student(fn, ln, idx, jmbg, un));
                }
            }

            // subjects
            NodeList subjects = doc.getElementsByTagName("subject");
            for (int i = 0; i < subjects.getLength(); i++) {
                Node n = subjects.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) continue;
                Element el = (Element) n;
                String name = el.getAttribute("name");
                List<Category> cats = new ArrayList<>();
                NodeList catNodes = el.getElementsByTagName("category");
                for (int j = 0; j < catNodes.getLength(); j++) {
                    Node cn = catNodes.item(j);
                    if (cn.getNodeType() != Node.ELEMENT_NODE) continue;
                    Element cel = (Element) cn;
                    String cnm = cel.getAttribute("name");
                    int max = parseIntSafe(cel.getAttribute("maxPoints"));
                    int min = parseIntSafe(cel.getAttribute("minRequired"));
                    cats.add(new Category(cnm, max, min));
                }
                if (name != null && !name.isEmpty()) {
                    state.subjects.put(name, new Subject(name, cats));
                }
            }

            // enrollments
            NodeList enrolls = doc.getElementsByTagName("enrollment");
            for (int i = 0; i < enrolls.getLength(); i++) {
                Node n = enrolls.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) continue;
                Element el = (Element) n;
                String user = el.getAttribute("studentUsername");
                String subj = el.getAttribute("subjectName");
                Enrollment en = new Enrollment(user, subj);
                NodeList points = el.getElementsByTagName("category");
                for (int j = 0; j < points.getLength(); j++) {
                    Node pn = points.item(j);
                    if (pn.getNodeType() != Node.ELEMENT_NODE) continue;
                    Element pel = (Element) pn;
                    String cat = pel.getAttribute("name");
                    int pts = parseIntSafe(pel.getAttribute("points"));
                    if (cat != null && !cat.isEmpty()) en.setPoints(cat, pts);
                }
                state.enrollments.computeIfAbsent(user, k -> new HashMap<>()).put(subj, en);
            }
        }
        return state;
    }

    private static void appendText(Document doc, Element parent, String name, String value) {
        Element el = doc.createElement(name);
        el.setTextContent(value == null ? "" : value);
        parent.appendChild(el);
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
    private static String textContent(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() == 0) return null;
        return nl.item(0).getTextContent();
    }
    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
