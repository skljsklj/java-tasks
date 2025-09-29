import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core championship logic: handles race lap simulation, sorting and scoring.
 */
public class Championship {
    private final List<Driver> drivers;
    private final List<Venue> venues;

    // Probabilities for faults (in percent out of 100)
    private static final int MINOR_MECHANICAL_FAULT = 5;  // +20s
    private static final int MAJOR_MECHANICAL_FAULT = 3;  // +120s
    private static final int UNRECOVERABLE_FAULT   = 1;  // DNF

    public Championship(List<Driver> drivers, List<Venue> venues) {
        this.drivers = drivers;
        this.venues = venues;
    }

    public List<Driver> getDrivers() { return drivers; }
    public List<Venue> getVenues() { return venues; }
    public void addVenue(Venue v) { venues.add(v); }

    // Initialization helpers
    public void resetAccumulatedTime() {
        for (Driver d : drivers) d.setAccumulatedTime(0);
    }

    /**
     * Before each race: set all base lap times and add initial penalties by current ranking.
     */
    public void prepareForTheRace(Venue v) {
        resetAccumulatedTime();
        driveAverageLapTime(v);
        // initial grid penalties based on ranking (smaller rank -> smaller penalty)
        // 1st: +0s, 2nd: +3s, 3rd: +5s, 4th: +7s, others: +10s
        List<Driver> sortedByRank = new ArrayList<>(drivers);
        sortedByRank.sort((a, b) -> Integer.compare(a.getRanking(), b.getRanking()));
        for (int i = 0; i < sortedByRank.size(); i++) {
            Driver d = sortedByRank.get(i);
            if (i == 0) d.addToAccumulatedTime(0);
            else if (i == 1) d.addToAccumulatedTime(3);
            else if (i == 2) d.addToAccumulatedTime(5);
            else if (i == 3) d.addToAccumulatedTime(7);
            else d.addToAccumulatedTime(10);
            d.setEligibleToRace(true);
            d.setWetTyres(false);
        }
    }

    /**
     * Adds average lap time for all drivers that are still eligible.
     */
    public void driveAverageLapTime(Venue v) {
        int avg = v.getAverageLapTime();
        for (Driver d : drivers) if (d.isEligibleToRace()) d.addToAccumulatedTime(avg);
    }

    /**
     * Apply driver skills:
     * - Braking/Cornering: small advantage every lap [1,8)
     * - Overtaking: larger advantage every 3rd lap [10,20)
     */
    public void applySpecialSkills(CurrentVenue current) {
        for (Driver d : drivers) {
            if (!d.isEligibleToRace()) continue;
            if ("Overtaking".equalsIgnoreCase(d.getSpecialSkill())) {
                if (current.getCurrentLap() % 3 == 0) {
                    d.useSpecialSkill(new RNG(10, 20));
                }
            } else if ("Braking".equalsIgnoreCase(d.getSpecialSkill()) ||
                       "Cornering".equalsIgnoreCase(d.getSpecialSkill())) {
                d.useSpecialSkill(new RNG(1, 8));
            }
        }
    }

    /**
     * Check mechanical problems with given probabilities.
     */
    public void checkMechanicalProblem() {
        RNG rng = new RNG(0, 100); // 0..99
        for (Driver d : drivers) {
            if (!d.isEligibleToRace()) continue;
            int roll = rng.getRandomValue();
            // Use cumulative logic: first check most severe
            if (roll < UNRECOVERABLE_FAULT) {
                System.out.println("!!! Driver " + d.getName() + " is out due to unrecoverable fault!");
                d.markUnrecoverableFault();
            } else if (roll < UNRECOVERABLE_FAULT + MAJOR_MECHANICAL_FAULT) {
                System.out.println("!!! Major mechanical fault for " + d.getName() + " (+120s)");
                d.addToAccumulatedTime(120);
            } else if (roll < UNRECOVERABLE_FAULT + MAJOR_MECHANICAL_FAULT + MINOR_MECHANICAL_FAULT) {
                System.out.println("!!! Minor mechanical fault for " + d.getName() + " (+20s)");
                d.addToAccumulatedTime(20);
            }
        }
    }

    public void sortAfterLap() {
        Collections.sort(drivers, new DriverComparator(false));
    }

    /**
     * Score the race and update rankings.
     * Points: 1st=8, 2nd=5, 3rd=3, 4th=1.
     */
    public void sortAfterRace() {
        // Ensure finishing order by time
        sortAfterLap();

        // Award points to first 4 eligible finishers, regardless of their index
        int[] pts = {8, 5, 3, 1};
        int awarded = 0;
        for (Driver d : drivers) {
            if (!d.isEligibleToRace()) continue;
            if (awarded < pts.length) {
                d.addPoints(pts[awarded]);
                awarded++;
            } else {
                break;
            }
        }

        // Sort by accumulated points descending for the standings
        Collections.sort(drivers, new DriverComparator(true));
        // Update rankings: 1..4 then 5 for the rest
        for (int i = 0; i < drivers.size(); i++) {
            drivers.get(i).setRanking(i < 4 ? i + 1 : 5);
        }
    }

    public void printLeader(int lap) {
        Driver leader = null;
        for (Driver d : drivers) {
            if (d.isEligibleToRace()) { leader = d; break; }
        }
        if (leader != null) {
            System.out.println("\tLeader of lap " + lap + ": " + leader);
        } else {
            System.out.println("\tNo eligible drivers at lap " + lap + ".");
        }
        System.out.println("----------------------------------------------------------------------");
    }

    public void printWinnersAfterRace(String venueName) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Results of the race at " + venueName + ":");
        int shown = 0;
        for (Driver d : drivers) {
            if (!d.isEligibleToRace()) continue;
            System.out.println("\t" + (shown + 1) + ". " + d);
            shown++;
            if (shown == 4) break;
        }
        System.out.println("----------------------------------------------------------------------");
    }

    public void printChampion(int numOfRaces) {
        System.out.println("-= Championship Winner =-");
        Driver d = drivers.get(0);
        System.out.println("Champion: " + d.getName() + " with " + d.getAccumulatedPoints() + " points.");
        System.out.println("Races driven: " + (numOfRaces - d.getRacesNotDriven()));
        System.out.println("----------------------------------------------------------------------");
    }
}
