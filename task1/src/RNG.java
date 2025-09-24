import java.util.Random;

public class RNG{
    private int minimumValue = 0;
    private int maximumValue = 1;
    private Random rnd;

    public RNG() {
        this.rnd = new Random(); 
    }

    public int getMinimumValue() { return minimumValue; }
    public void setMinimumValue(int minimumValue) { this.minimumValue = minimumValue; }

    public int getMaximumValue() { return maximumValue; }
    public void setMaximumValue(int maximumValue) { this.maximumValue = maximumValue; }

    public int getRandomValue(){ return rnd.nextInt(minimumValue, maximumValue); }
}