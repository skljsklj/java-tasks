import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Simulation {

    public static CurrentVenue currentVenue;

    private static int askNumRaces(Scanner sc) {
        System.out.println("Welcome to F1 Grand Prix! Enter number of races (3-5)");
        return Utility.boundedInput(sc, 3, 5);
    }

    private static Venue askVenue(Scanner sc, List<Venue> available) {
        while (true) {
            System.out.println("Choose the venue:");
            for (int i = 0; i < available.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + available.get(i).getVenueName());
            }
            System.out.print("(type the ordinal number)\n");
            int sel = Utility.boundedInput(sc, 1, available.size());
            return available.remove(sel - 1);
        }
    }

    public static void main(String[] args) {
        List<Venue> availableVenues = new ArrayList<>();
        List<Driver> drivers = new ArrayList<>();
        List<Venue> usedVenues = new ArrayList<>();

        try {
            Utility.readInputFiles(availableVenues, drivers);
        } catch (IOException e) {
            System.err.println("Failed to read input files: " + e.getMessage());
            return;
        }

        Championship champ = new Championship(drivers, usedVenues);

        try (Scanner sc = new Scanner(System.in)) {
            int numOfRaces = askNumRaces(sc);
            System.out.println("\n*** You chose to watch " + numOfRaces + " races. ***\n");

            for (int raceIdx = 1; raceIdx <= numOfRaces; raceIdx++) {
                System.out.println("----------------------------------------------------------------------");
                System.out.println("Race #" + raceIdx);
                Venue chosen = askVenue(sc, availableVenues);
                currentVenue = new CurrentVenue(chosen);
                champ.addVenue(currentVenue);
                System.out.println("Selected: " + currentVenue.getVenueName() + ", laps: " + currentVenue.getNumberOfLaps());

                // Preparation
                champ.prepareForTheRace(currentVenue);

                // Race laps
                int totalLaps = currentVenue.getNumberOfLaps();
                for (int lap = 1; lap <= totalLaps; lap++) {
                    System.out.println("----------------------------------------------------------------------");
                    System.out.println("* Race at " + currentVenue.getVenueName() + ", lap " + lap + ".");

                    if (lap > 1) {
                        champ.driveAverageLapTime(currentVenue);
                    }

                    if (lap == 2) {
                        // Optional tyre change decision per driver
                        RNG rng = new RNG(0, 100);
                        for (Driver d : drivers) {
                            if (!d.isEligibleToRace()) continue;
                            boolean change = rng.getRandomValue() < 50; // 50% chance
                            d.setTireChanged(change);
                            if (change) {
                                System.out.println("- Driver " + d.getName() + " changed tyres.");
                            }
                        }
                    }

                    // Mechanical problems
                    champ.checkMechanicalProblem();

                    // Special skills
                    champ.applySpecialSkills(currentVenue);

                    // Weather (rain)
                    RNG rainRng = new RNG();
                    boolean rain = rainRng.getRandomValueDouble() < currentVenue.getChanceOfRain();
                    System.out.println(rain ? "!!! It is raining" : "- No rain");
                    if (rain) {
                        for (Driver d : drivers) {
                            if (d.isEligibleToRace() && !d.isTireChanged()) {
                                d.addToAccumulatedTime(5);
                                System.out.println("!!! Driver " + d.getName() + " slides (no wet tyres) (+5s)");
                            }
                        }
                    }

                    champ.sortAfterLap();
                    champ.printLeader(lap);
                    currentVenue.incCurrentLap();
                }

                System.out.println("----------------------------------------------------------------------");
                System.out.println("End of race at: " + currentVenue.getVenueName());
                champ.printWinnersAfterRace(currentVenue.getVenueName());
                champ.sortAfterRace();
            }

            System.out.println("Final standings:");
            for (Driver d : champ.getDrivers()) System.out.println("\t" + d);
            champ.printChampion(numOfRaces);
        }
    }
}
