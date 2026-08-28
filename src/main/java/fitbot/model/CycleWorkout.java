package fitbot.model;

import java.time.LocalDate;
import java.util.Set;

/**
 * Represents a cycling workout.
 */
public class CycleWorkout extends Workout {
    /** Options accepted when logging a cycle. */
    private static final Set<String> SUPPORTED_OPTIONS = Set.of(
            "-type", "-date", "-duration", "-distance", "-elevation");
    private final double distanceKilometres;
    private final Double elevationGainMetres;

    /** Creates a cycling workout. */
    public CycleWorkout(LocalDate date, long durationSeconds,
            double distanceKilometres, Double elevationGainMetres) {
        super(date, durationSeconds);
        this.distanceKilometres = distanceKilometres;
        this.elevationGainMetres = elevationGainMetres;
    }

    public double getDistanceKilometres() {
        return distanceKilometres;
    }

    public Double getElevationGainMetres() {
        return elevationGainMetres;
    }

    public static Set<String> getSupportedOptions() {
        return SUPPORTED_OPTIONS;
    }

    @Override
    public String getType() {
        return "CYCLE";
    }
}
