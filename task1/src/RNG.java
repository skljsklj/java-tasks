import java.util.Random;

/**
 * Random number helper for the simulation.
 * - Supports configurable integer ranges and simple double values.
 */
public class RNG {
    private int minimumValue = 0;
    private int maximumValue = 1;
    private final Random rnd;

    public RNG() {
        this.rnd = new Random();
    }

    public RNG(int minimumValue, int maximumValue) {
        this();
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    public int getMinimumValue() { return minimumValue; }
    public void setMinimumValue(int minimumValue) { this.minimumValue = minimumValue; }

    public int getMaximumValue() { return maximumValue; }
    public void setMaximumValue(int maximumValue) { this.maximumValue = maximumValue; }

    /**
     * Returns a random int in [minimumValue, maximumValue) (max is exclusive),
     * matching Java's Random#nextInt(origin, bound) behavior.
     */
    public int getRandomValue() {
        return rnd.nextInt(minimumValue, maximumValue);
    }

    /**
     * Returns a random double in [0.0, 1.0).
     */
    public double getRandomValueDouble() {
        return rnd.nextDouble();
    }
}
