package eindex.server;

import eindex.model.User;
import eindex.service.EIndexService;
import eindex.storage.DataStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Server {
    public static void main(String[] args) throws Exception {
        int port = 5555;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        Path wd = Paths.get(".").toAbsolutePath().normalize();
        System.out.println("Working dir: " + wd);
        DataStore store = new DataStore(Paths.get("."));
        store.ensureDefaultAdmin();
        try {
            Map<String, User> users = store.readUsers();
            System.out.println("Users loaded from users.txt: " + users.keySet());
        } catch (IOException ioe) {
            System.out.println("Failed to read users.txt: " + ioe.getMessage());
        }
        EIndexService service = new EIndexService(store);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { service.save(); } catch (IOException ignored) {}
        }));

        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("E-Index server pokrenut na portu " + port);
            while (true) {
                Socket s = ss.accept();
                System.out.println("Klijent povezan: " + s.getRemoteSocketAddress());
                new Thread(new ClientHandler(s, service)).start();
            }
        }
    }
}
