package fitbot.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a cycling workout.
 */
public class CycleWorkout extends Workout {
    /** Options accepted when logging a cycle. */
    private static final Set<String> SUPPORTED_OPTIONS = Set.of(
            "-type", "-date", "-duration", "-distance", "-elevation", "-max");
    private double distanceKilometres;
    private Double elevationGainMetres;
    private Double maxSpeedKilometresPerHour;

    /** Creates a cycling workout. */
    public CycleWorkout(LocalDate date, long durationSeconds, double distanceKilometres,
            Double elevationGainMetres, Double maxSpeedKilometresPerHour) {
        super(date, durationSeconds);
        this.distanceKilometres = distanceKilometres;
        this.elevationGainMetres = elevationGainMetres;
        this.maxSpeedKilometresPerHour = maxSpeedKilometresPerHour;
    }

    public double getDistanceKilometres() {
        return distanceKilometres;
    }

    public Double getElevationGainMetres() {
        return elevationGainMetres;
    }

    public Double getMaxSpeedKilometresPerHour() {
        return maxSpeedKilometresPerHour;
    }

    /** Changes the cycling distance. */
    public void setDistanceKilometres(double distanceKilometres) {
        if (distanceKilometres <= 0) {
            throw new IllegalArgumentException("Distance must be positive.");
        }
        this.distanceKilometres = distanceKilometres;
    }

    /** Changes the cycling elevation gain. */
    public void setElevationGainMetres(Double elevationGainMetres) {
        if (elevationGainMetres != null && elevationGainMetres < 0) {
            throw new IllegalArgumentException("Elevation cannot be negative.");
        }
        this.elevationGainMetres = elevationGainMetres;
    }

    /** Changes the maximum cycling speed. */
    public void setMaxSpeedKilometresPerHour(Double maxSpeedKilometresPerHour) {
        if (maxSpeedKilometresPerHour != null && maxSpeedKilometresPerHour <= 0) {
            throw new IllegalArgumentException("Maximum speed must be positive.");
        }
        this.maxSpeedKilometresPerHour = maxSpeedKilometresPerHour;
    }

    public static Set<String> getSupportedOptions() {
        return SUPPORTED_OPTIONS;
    }

    /**
     * Calculates the average cycling speed in kilometres per hour.
     *
     * @return average speed in kilometres per hour
     */
    public double getSpeedKilometresPerHour() {
        return distanceKilometres / (getDurationSeconds() / 3600.0);
    }

    @Override
    public WorkoutType getType() {
        return WorkoutType.CYCLE;
    }

    @Override
    public Map<String, Object> getStorageFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("distanceKilometres", distanceKilometres);
        fields.put("elevationGainMetres", elevationGainMetres);
        fields.put("maxSpeedKilometresPerHour", maxSpeedKilometresPerHour);
        return fields;
    }
}
