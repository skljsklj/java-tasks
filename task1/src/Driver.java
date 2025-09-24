import java.util.Objects;

public class Driver {

    //attributes
    private String name;
    private int ranking;
    private String specialSkill;
    private boolean eligibleToRace;
    private int accumulatedTime;
    private int accumulatedPoints;
    private boolean wetTyres;

    public Driver(String name, int ranking, String specialSkill){
        this.name = name;
        this.ranking = ranking;
        this.specialSkill = specialSkill;
        this.eligibleToRace = true;
        this.accumulatedTime = 0;
        this.accumulatedPoints = 0;
        this.wetTyres = false;
    }

    public void useSpecialSkill(RNG rng){
        if(!eligibleToRace)
        switch(specialSkill){
            case "braking":
            case "cornering":
                rng.setMinimumValue(1);
                rng.setMaximumValue(8);
                return rng.getRandomValue();
            case "overtaking":
                if (rng.lapIndex % 3 == 0){
                    rng.setMinimumValue(10);
                    rng.setMaximumValue(10);
                    return rng.getRandomValue();
                }
        }
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

    @Override
    public String toString() {
        return name + " (rank " + ranking + ", skill " + specialSkill + ", pts " + accumulatedPoints + ")";
    }

}