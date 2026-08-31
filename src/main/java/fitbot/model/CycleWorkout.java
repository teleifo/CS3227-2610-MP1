package fitbot.model;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents a cycling workout.
 */
@JsonPropertyOrder({
    "type",
    "date",
    "durationSeconds",
    "distanceKilometres",
    "elevationGainMetres",
    "maxSpeedKilometresPerHour"
})
public class CycleWorkout extends Workout {
    /** Options accepted when logging a cycle. */
    private static final Set<String> SUPPORTED_OPTIONS = Set.of(
            "-type", "-date", "-duration", "-distance", "-elevation", "-max");
    private double distanceKilometres;
    private Double elevationGainMetres;
    private Double maxSpeedKilometresPerHour;

    /** Creates a cycling workout. */
    @JsonCreator
    public CycleWorkout(@JsonProperty("date") LocalDate date,
            @JsonProperty("durationSeconds") long durationSeconds,
            @JsonProperty("distanceKilometres") double distanceKilometres,
            @JsonProperty("elevationGainMetres") Double elevationGainMetres,
            @JsonProperty("maxSpeedKilometresPerHour") Double maxSpeedKilometresPerHour) {
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

    public void setDistanceKilometres(double distanceKilometres) {
        if (distanceKilometres <= 0) {
            throw new IllegalArgumentException("Distance must be positive.");
        }
        this.distanceKilometres = distanceKilometres;
    }

    public void setElevationGainMetres(Double elevationGainMetres) {
        if (elevationGainMetres != null && elevationGainMetres < 0) {
            throw new IllegalArgumentException("Elevation cannot be negative.");
        }
        this.elevationGainMetres = elevationGainMetres;
    }

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
    @JsonIgnore
    public double getSpeedKilometresPerHour() {
        return distanceKilometres / (getDurationSeconds() / 3600.0);
    }

    @Override
    public WorkoutType getType() {
        return WorkoutType.CYCLE;
    }
}
