package fitbot.model;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents a running workout.
 */
@JsonPropertyOrder({
    "type",
    "date",
    "durationSeconds",
    "distanceKilometres",
    "elevationGainMetres"
})
public class RunWorkout extends Workout {
    /** Options accepted when logging a run. */
    private static final Set<String> SUPPORTED_OPTIONS = Set.of(
            "-type", "-date", "-duration", "-distance", "-elevation");
    private double distanceKilometres;
    private Double elevationGainMetres;

    /** Creates a running workout. */
    @JsonCreator
    public RunWorkout(@JsonProperty("date") LocalDate date,
            @JsonProperty("durationSeconds") long durationSeconds,
            @JsonProperty("distanceKilometres") double distanceKilometres,
            @JsonProperty("elevationGainMetres") Double elevationGainMetres) {
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

    public static Set<String> getSupportedOptions() {
        return SUPPORTED_OPTIONS;
    }

    /**
     * Calculates the average running pace in seconds per kilometre.
     *
     * @return average pace in seconds per kilometre
     */
    @JsonIgnore
    public double getPaceSecondsPerKilometre() {
        return getDurationSeconds() / distanceKilometres;
    }

    @Override
    public WorkoutType getType() {
        return WorkoutType.RUN;
    }
}
