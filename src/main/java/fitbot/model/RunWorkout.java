package fitbot.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a running workout.
 */
public class RunWorkout extends Workout {
    /** Options accepted when logging a run. */
    private static final Set<String> SUPPORTED_OPTIONS = Set.of(
            "-type", "-date", "-duration", "-distance", "-elevation");
    private double distanceKilometres;
    private Double elevationGainMetres;

    /** Creates a running workout. */
    public RunWorkout(LocalDate date, long durationSeconds, double distanceKilometres,
            Double elevationGainMetres) {
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

    /** Changes the running distance. */
    public void setDistanceKilometres(double distanceKilometres) {
        if (distanceKilometres <= 0) {
            throw new IllegalArgumentException("Distance must be positive.");
        }
        this.distanceKilometres = distanceKilometres;
    }

    /** Changes the running elevation gain. */
    public void setElevationGainMetres(Double elevationGainMetres) {
        if (elevationGainMetres != null && elevationGainMetres < 0) {
            throw new IllegalArgumentException("Elevation cannot be negative.");
        }
        this.elevationGainMetres = elevationGainMetres;
    }

    public static Set<String> getSupportedOptions() {
        return SUPPORTED_OPTIONS;
    }

    /**
     * Calculates the average running pace in seconds per kilometre.
     *
     * @return average pace in seconds per kilometre
     */
    public double getPaceSecondsPerKilometre() {
        return getDurationSeconds() / distanceKilometres;
    }

    @Override
    public WorkoutType getType() {
        return WorkoutType.RUN;
    }

    @Override
    public Map<String, Object> getStorageFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("distanceKilometres", distanceKilometres);
        fields.put("elevationGainMetres", elevationGainMetres);
        return fields;
    }
}
