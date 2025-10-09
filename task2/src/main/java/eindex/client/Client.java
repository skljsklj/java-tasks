package eindex.client;

import eindex.model.Student;
import eindex.model.Subject;
import eindex.model.Category;
import eindex.net.Action;
import eindex.net.Request;
import eindex.net.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class Client {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 5555;
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Povezano na server " + host + ":" + port);
            System.out.print("Korisnicko ime: ");
            String username = sc.nextLine().trim();
            System.out.print("Lozinka: ");
            String password = sc.nextLine().trim();
            System.out.print("Uloga (admin/student): ");
            String role = sc.nextLine().trim();
            while (!(role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("student"))) {
                System.out.print("Neispravna uloga. Unesite 'admin' ili 'student': ");
                role = sc.nextLine().trim();
            }

            Request login = new Request(Action.LOGIN);
            login.put("username", username);
            login.put("password", password);
            login.put("role", role);
            out.writeObject(login);
            out.flush();
            Response resp = (Response) in.readObject();
            if (!resp.isSuccess()) {
                System.out.println("Prijava neuspesna: " + resp.getMessage());
                return;
            }
            boolean admin = role.equalsIgnoreCase("admin");
            if (admin) runAdmin(sc, out, in); else runStudent(username, sc, out, in);
        }
    }

    private static void runAdmin(Scanner sc, ObjectOutputStream out, ObjectInputStream in) throws Exception {
        while (true) {
            System.out.println("\n--- ADMIN MENI ---");
            System.out.println("1) Dodaj administratora");
            System.out.println("2) Dodaj studenta");
            System.out.println("3) Dodaj predmet");
            System.out.println("4) Pridruzi studenta predmetu");
            System.out.println("5) Azuriraj poene studenta");
            System.out.println("6) Prikazi studente");
            System.out.println("7) Prikazi predmete");
            System.out.println("8) Sacuvaj podatke");
            System.out.println("9) Ugasi server");
            System.out.println("0) Izlaz iz klijenta");
            System.out.print("Izbor: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": {
                    System.out.print("Novi admin username: "); String u = sc.nextLine().trim();
                    System.out.print("Lozinka: "); String p = sc.nextLine().trim();
                    Request r = new Request(Action.ADD_ADMIN); r.put("username", u); r.put("password", p);
                    send(out, r); printResp(read(in));
                    break;
                }
                case "2": {
                    System.out.print("Ime: "); String first = sc.nextLine().trim();
                    System.out.print("Prezime: "); String last = sc.nextLine().trim();
                    System.out.print("Broj indeksa (npr E2-2015): "); String index = sc.nextLine().trim();
                    System.out.print("JMBG (13 cifara): "); String jmbg = sc.nextLine().trim();
                    System.out.print("Korisnicko ime: "); String un = sc.nextLine().trim();
                    System.out.print("Lozinka: "); String pw = sc.nextLine().trim();
                    Request r = new Request(Action.ADD_STUDENT);
                    r.put("firstName", first); r.put("lastName", last);
                    r.put("indexNumber", index); r.put("jmbg", jmbg);
                    r.put("username", un); r.put("password", pw);
                    send(out, r); printResp(read(in));
                    break;
                }
                case "3": {
                    System.out.print("Naziv predmeta: "); String subj = sc.nextLine().trim();
                    System.out.print("Broj kategorija: "); int k = Integer.parseInt(sc.nextLine().trim());
                    List<Map<String,Object>> cats = new ArrayList<>();
                    for (int i = 0; i < k; i++) {
                        System.out.println("Kategorija #" + (i+1));
                        System.out.print("Naziv: "); String cn = sc.nextLine().trim();
                        System.out.print("Max poena: "); int max = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Min poena: "); int min = Integer.parseInt(sc.nextLine().trim());
                        Map<String,Object> m = new HashMap<>();
                        m.put("name", cn); m.put("max", max); m.put("min", min);
                        cats.add(m);
                    }
                    Request r = new Request(Action.ADD_SUBJECT);
                    r.put("name", subj); r.put("categories", cats);
                    send(out, r); printResp(read(in));
                    break;
                }
                case "4": {
                    System.out.print("Username studenta: "); String un = sc.nextLine().trim();
                    System.out.print("Naziv predmeta: "); String subj = sc.nextLine().trim();
                    Request r = new Request(Action.ASSIGN_STUDENT);
                    r.put("username", un); r.put("subject", subj);
                    send(out, r); printResp(read(in));
                    break;
                }
                case "5": {
                    System.out.print("Username studenta: "); String un = sc.nextLine().trim();
                    System.out.print("Naziv predmeta: "); String subj = sc.nextLine().trim();
                    Map<String,Integer> pts = new HashMap<>();
                    System.out.println("Unosite parove kategorija=poeni, prazno za kraj. Npr: T1=20");
                    while (true) {
                        String line = sc.nextLine().trim();
                        if (line.isEmpty()) break;
                        String[] pp = line.split("=");
                        if (pp.length == 2) {
                            pts.put(pp[0].trim(), Integer.parseInt(pp[1].trim()));
                        } else {
                            System.out.println("Pogresan format, pokusajte ponovo");
                        }
                    }
                    Request r = new Request(Action.UPDATE_POINTS);
                    r.put("username", un); r.put("subject", subj); r.put("points", pts);
                    send(out, r); printResp(read(in));
                    break;
                }
                case "6": {
                    Request r = new Request(Action.LIST_STUDENTS);
                    send(out, r); Response resp = read(in);
                    if (resp.isSuccess() && resp.getPayload() instanceof List) {
                        @SuppressWarnings("unchecked") List<Student> list = (List<Student>) resp.getPayload();
                        for (Student s : list) System.out.println("- " + s + " [" + s.getUsername() + "]");
                    } else printResp(resp);
                    break;
                }
                case "7": {
                    Request r = new Request(Action.LIST_SUBJECTS);
                    send(out, r); Response resp = read(in);
                    if (resp.isSuccess() && resp.getPayload() instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Subject> list = (java.util.List<Subject>) resp.getPayload();
                        System.out.println("Predmeti");
                        if (list.isEmpty()) {
                            System.out.println("(nema unetih predmeta)");
                        } else {
                            for (Subject s : list) {
                                System.out.println("- " + s.getName());
                                java.util.List<Category> cats = s.getCategories();
                                for (Category c : cats) {
                                    System.out.println("    * " + c.getName() + " (max=" + c.getMaxPoints() + ", min=" + c.getMinRequired() + ")");
                                }
                            }
                        }
                    } else {
                        System.out.println((resp.isSuccess()?"":"GREsKA: ") + resp.getMessage());
                    }
                    break;
                }
                case "8": {
                    Request r = new Request(Action.SAVE);
                    send(out, r); printResp(read(in));
                    break;
                }
                case "9": {
                    Request r = new Request(Action.SHUTDOWN);
                    send(out, r); printResp(read(in));
                    return;
                }
                case "0": return;
                default: System.out.println("Nepoznata opcija");
            }
        }
    }

    private static void runStudent(String username, Scanner sc, ObjectOutputStream out, ObjectInputStream in) throws Exception {
        while (true) {
            System.out.println("\n--- STUDENT MENI ---");
            System.out.println("1) Pregled poena po kategorijama za predmet");
            System.out.println("2) Pregled polozenih/nepolozenih i ocena");
            System.out.println("0) Izlaz");
            System.out.print("Izbor: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": {
                    System.out.print("Naziv predmeta: "); String subj = sc.nextLine().trim();
                    Request r = new Request(Action.VIEW_POINTS);
                    r.put("username", username); r.put("subject", subj);
                    send(out, r); Response resp = read(in);
                    if (resp.isSuccess() && resp.getPayload() instanceof Map) {
                        @SuppressWarnings("unchecked") Map<String,Integer> m = (Map<String,Integer>) resp.getPayload();
                        System.out.println("Bodovno stanje za " + subj + ":");
                        m.forEach((k,v) -> System.out.println("- " + k + ": " + v));
                    } else printResp(resp);
                    break;
                }
                case "2": {
                    Request r = new Request(Action.VIEW_RESULTS);
                    r.put("username", username);
                    send(out, r); Response resp = read(in);
                    System.out.println(resp.getMessage());
                    System.out.println(String.valueOf(resp.getPayload()));
                    break;
                }
                case "0": return;
                default: System.out.println("Nepoznata opcija");
            }
        }
    }

    private static void send(ObjectOutputStream out, Request r) throws Exception {
        out.writeObject(r); out.flush();
    }

    private static Response read(ObjectInputStream in) throws Exception {
        return (Response) in.readObject();
    }

    private static void printResp(Response resp) {
        System.out.println((resp.isSuccess() ? "OK: " : "GRESKA: ") + resp.getMessage());
    }
}
