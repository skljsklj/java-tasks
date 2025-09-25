import java.io.IOException;
import java.util.*;

public class Simulation {
    private static int askNumRaces(Scanner sc){
        int n = -1;
        while (true){
            System.out.print("Write the number of laps (3-5): ");
            String s = sc.nextLine().trim();
            try{
                n = Integer.parseInt(s);
                if (n >= 3 && n <= 5) return n;
            } catch (NumberFormatException ignored) {}
            System.out.println("Try again.\n");
        }
    }

    private static Venue askVenue(Scanner sc, List<Venue> available){
        while (true) {
            System.out.println("Available traces: ");
            for (int i = 0; i < available.size(); i++){
                System.out.println("(%2d) %s%n", n + 1, available.get(i));
            }
            System.out.println("Choose trace by inputing its serial number: ");
            String s = sc.nextLine().trim();
            try {
                int idx = Integer.parseInt(s) - 1;
                if(idx >= 0 && idx < available.size()){
                    return available.remove(idx);
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Try again.\n");
        }
    }

    public static void main (String[] args){
        try (Scanner sc = new Scanner(System.in)){
            Championship champ;
            try{
                champ = new Championship("vozaci.txt", "staze.txt");
            } catch (IOException e){
                System.err.println("Error while reading files" + e.getMessage());
                return;
            }
        }

        int numOfRaces = askNumRaces(sc);

        List<Venue> remainingVenues = new ArrayList<>(champ.getVenues());
    }
}
