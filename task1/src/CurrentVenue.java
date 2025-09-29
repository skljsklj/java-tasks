/**
 * Holds mutable state for the currently selected venue during a race.
 */
public class CurrentVenue extends Venue {
    private int currentLap;

    public CurrentVenue(String venueName, int numberOfLaps, int averageLapTime, double chanceOfRain) {
        super(venueName, numberOfLaps, averageLapTime, chanceOfRain);
        this.currentLap = 1;
    }

    public CurrentVenue(Venue v) {
        super(v.getVenueName(), v.getNumberOfLaps(), v.getAverageLapTime(), v.getChanceOfRain());
        this.currentLap = 1;
    }

    public int getCurrentLap() { return currentLap; }
    public void setCurrentLap(int currentLap) { this.currentLap = currentLap; }
    public void incCurrentLap() { this.currentLap += 1; }
}

