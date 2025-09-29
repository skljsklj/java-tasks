//import java.util.Objects;

public class Driver {

    //attributes
    private String name;
    private int ranking;
    private String specialSkill;
    private boolean eligibleToRace;
    private int accumulatedTime;
    private int accumulatedPoints;
    private boolean wetTyres; // whether driver has wet tyres mounted
    private int racesNotDriven; // number of races missed due to unrecoverable fault

    public Driver(String name, int ranking, String specialSkill){
        this.name = name;
        this.ranking = ranking;
        this.specialSkill = specialSkill;
        this.eligibleToRace = true;
        this.accumulatedTime = 0;
        this.accumulatedPoints = 0;
        this.wetTyres = false;
        this.racesNotDriven = 0;
    }

    public void useSpecialSkill(RNG rng){
        int cut_off_time = rng.getRandomValue();
        accumulatedTime -= cut_off_time;
        System.out.println("\t-> Driver " + this.getName() + " used skill: " + this.getSpecialSkill() +
                " and gained " + cut_off_time + " s.");
    }

    //getters/setters
    public String getName() { return name; }
    public int getRanking() { return ranking; }
    public void setRanking(int ranking) { this.ranking = ranking; }
    public String getSpecialSkill() { return specialSkill; }
    public void setSpecialSkill(String specialSkill) { this.specialSkill = specialSkill; }
    public boolean isEligibleToRace() { return eligibleToRace; }
    public void setEligibleToRace(boolean eligibleToRace) { this.eligibleToRace = eligibleToRace; }
    public int getAccumulatedTime() { return accumulatedTime; }
    public void setAccumulatedTime(int accumulatedTime) { this.accumulatedTime = accumulatedTime; }
    public void addTime(int seconds) { this.accumulatedTime += seconds; }
    public int getAccumulatedPoints() { return accumulatedPoints; }
    public void addPoints(int pts) { this.accumulatedPoints += pts; }
    public boolean hasWetTyres() { return wetTyres; }
    public void setWetTyres(boolean wetTyres) { this.wetTyres = wetTyres; }
    public int getRacesNotDriven() { return racesNotDriven; }

    // convenience aliases used across code
    public void addToAccumulatedTime(int seconds) { addTime(seconds); }
    public void setTireChanged(boolean changed) { setWetTyres(changed); }
    public boolean isTireChanged() { return hasWetTyres(); }

    // when a driver becomes ineligible due to unrecoverable fault, count the missed race
    public void markUnrecoverableFault() {
        setEligibleToRace(false);
        this.racesNotDriven++;
    }

    @Override
    public String toString() {
        return name + " (rank " + ranking + ", skill " + specialSkill + ", pts " + accumulatedPoints + ")";
    }

}
