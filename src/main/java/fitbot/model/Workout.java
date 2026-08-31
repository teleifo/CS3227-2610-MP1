package fitbot.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Stores information shared by all workout types.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = RunWorkout.class, name = "RUN"),
    @JsonSubTypes.Type(value = CycleWorkout.class, name = "CYCLE"),
    @JsonSubTypes.Type(value = GymWorkout.class, name = "GYM")
})
public abstract class Workout {
    private LocalDate date;
    private long durationSeconds;

    /** Creates a workout with its common details. */
    protected Workout(LocalDate date, long durationSeconds) {
        setDate(date);
        this.durationSeconds = durationSeconds;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    /** Changes the workout date. */
    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future.");
        }
        this.date = date;
    }

    /** Changes the workout duration. */
    public void setDurationSeconds(long durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.durationSeconds = durationSeconds;
    }

    /** Returns a display name for this workout type. */
    public abstract WorkoutType getType();
}
