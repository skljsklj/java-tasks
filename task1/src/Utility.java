import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
//import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Helper utilities for file IO and user input.
 */
public class Utility {

    /**
     * Reads venues from `staze.txt` and drivers from `vozaci.txt` in the working directory.
     */
    public static void readInputFiles(List<Venue> venuesOut, List<Driver> driversOut) throws IOException {
        String sep = System.getProperty("file.separator");
        File venuesFile = new File("." + sep + "staze.txt");
        File driversFile = new File("." + sep + "vozaci.txt");

        if (venuesFile.exists()) {
            try (BufferedReader in = new BufferedReader(new FileReader(venuesFile))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] t = line.split(",");
                    if (t.length >= 4) {
                        venuesOut.add(new Venue(t[0], Integer.parseInt(t[1].trim()), Integer.parseInt(t[2].trim()), Double.parseDouble(t[3].trim())));
                    }
                }
            }
        } else {
            throw new IOException("Missing file: " + venuesFile.getName());
        }

        if (driversFile.exists()) {
            try (BufferedReader in = new BufferedReader(new FileReader(driversFile))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] t = line.split(",");
                    if (t.length >= 3) {
                        driversOut.add(new Driver(t[0], Integer.parseInt(t[1].trim()), t[2].trim()));
                    }
                }
            }
        } else {
            throw new IOException("Missing file: " + driversFile.getName());
        }
    }

    /**
     * Reads an integer in [min, max] from stdin.
     */
    public static int boundedInput(Scanner sc, int min, int max) {
        int val;
        while (true) {
            System.out.print("  [" + min + "," + max + "]: ");
            String s = sc.nextLine().trim();
            try {
                val = Integer.parseInt(s);
                if (val >= min && val <= max) return val;
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid input. Try again.\n");
        }
    }
}

