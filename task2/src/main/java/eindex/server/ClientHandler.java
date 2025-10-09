package eindex.server;

import eindex.model.*;
import eindex.net.Action;
import eindex.net.Request;
import eindex.net.Response;
import eindex.service.EIndexService;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final EIndexService service;

    public ClientHandler(Socket socket, EIndexService service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (!socket.isClosed()) {
                Object obj;
                try {
                    obj = in.readObject();
                } catch (EOFException eof) {
                    break;
                }
                if (!(obj instanceof Request)) break;
                Request req = (Request) obj;
                Response resp = handle(req);
                out.writeObject(resp);
                out.flush();
            }
        } catch (Exception e) {
            // log to console
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private Response handle(Request req) {
        try {
            Action a = req.getAction();
            Map<String, Object> d = req.getData();
            switch (a) {
                case LOGIN: {
                    String u = (String) d.get("username");
                    String p = (String) d.get("password");
                    String r = (String) d.get("role");
                    Role role;
                    if ("admin".equalsIgnoreCase(r)) {
                        role = Role.ADMIN;
                    } else if ("student".equalsIgnoreCase(r)) {
                        role = Role.STUDENT;
                    } else {
                        return Response.error("Neispravna uloga (dozvoljeno: admin/student)");
                    }
                    boolean ok = service.authenticate(u, p, role);
                    return ok ? Response.ok("Prijava uspesna", null) : Response.error("Neispravni kredencijali");
                }
                case ADD_ADMIN: {
                    String u = (String) d.get("username");
                    String p = (String) d.get("password");
                    service.addAdmin(u, p);
                    return Response.ok("Dodat administrator '" + u + "'", null);
                }
                case ADD_STUDENT: {
                    String first = (String) d.get("firstName");
                    String last = (String) d.get("lastName");
                    String index = (String) d.get("indexNumber");
                    String jmbg = (String) d.get("jmbg");
                    String username = (String) d.get("username");
                    String password = (String) d.get("password");
                    Student s = new Student(first, last, index, jmbg, username);
                    service.addStudent(s, password);
                    return Response.ok("Student dodat", null);
                }
                case ADD_SUBJECT: {
                    String name = (String) d.get("name");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> cats = (List<Map<String, Object>>) d.get("categories");
                    List<Category> list = new ArrayList<>();
                    for (Map<String, Object> c : cats) {
                        String cn = (String) c.get("name");
                        int max = (Integer) c.get("max");
                        int min = (Integer) c.get("min");
                        list.add(new Category(cn, max, min));
                    }
                    service.addSubject(new Subject(name, list));
                    return Response.ok("Predmet dodat", null);
                }
                case ASSIGN_STUDENT: {
                    String username = (String) d.get("username");
                    String subject = (String) d.get("subject");
                    service.assignStudentToSubject(username, subject);
                    return Response.ok("Student pridruzen predmetu", null);
                }
                case UPDATE_POINTS: {
                    String username = (String) d.get("username");
                    String subject = (String) d.get("subject");
                    @SuppressWarnings("unchecked") Map<String, Integer> pts = (Map<String, Integer>) d.get("points");
                    service.updatePoints(username, subject, pts);
                    return Response.ok("Poeni azurirani", null);
                }
                case VIEW_POINTS: {
                    String username = (String) d.get("username");
                    String subject = (String) d.get("subject");
                    Map<String, Integer> map = service.viewPoints(username, subject);
                    return Response.ok("Bodovno stanje", map);
                }
                case VIEW_RESULTS: {
                    String username = (String) d.get("username");
                    return Response.ok("Rezultati", service.viewResults(username));
                }
                case LIST_STUDENTS: {
                    return Response.ok("Studenti", new ArrayList<>(service.listStudentsCombined()));
                }
                case LIST_SUBJECTS: {
                    return Response.ok("Predmeti", new ArrayList<>(service.listSubjects()));
                }
                case LIST_ENROLLMENTS: {
                    return Response.ok("Upisi", service.listEnrollments());
                }
                case SAVE: {
                    service.save();
                    return Response.ok("Sacuvano", null);
                }
                case SHUTDOWN: {
                    service.save();
                    new Thread(() -> {
                        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                        System.exit(0);
                    }).start();
                    return Response.ok("Server se gasi...", null);
                }
            }
            return Response.error("Nepoznata akcija");
        } catch (Exception e) {
            return Response.error("Greska: " + e.getMessage());
        }
    }
}
