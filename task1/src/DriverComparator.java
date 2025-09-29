import java.util.Comparator;

/**
 * Comparator for drivers.
 * - If criteriaPoints is true: sort by accumulated points descending.
 * - Otherwise: sort by accumulated time ascending (lower is better).
 */
public class DriverComparator implements Comparator<Driver> {
    private final boolean criteriaPoints;

    public DriverComparator(boolean criteriaPoints) {
        this.criteriaPoints = criteriaPoints;
    }

    @Override
    public int compare(Driver d1, Driver d2) {
        if (criteriaPoints) {
            return Integer.compare(d2.getAccumulatedPoints(), d1.getAccumulatedPoints());
        } else {
            return Integer.compare(d1.getAccumulatedTime(), d2.getAccumulatedTime());
        }
    }
}

