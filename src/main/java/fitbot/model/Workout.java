package fitbot.model;

import java.time.LocalDate;
import java.util.Map;

/**
 * Stores information shared by all workout types.
 */
public abstract class Workout {
    private LocalDate date;
    private long durationSeconds;

    /** Creates a workout with its common details. */
    protected Workout(LocalDate date, long durationSeconds) {
        this.date = date;
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

    /** Returns additional fields that this workout type stores in JSON. */
    public abstract Map<String, Object> getStorageFields();
}
