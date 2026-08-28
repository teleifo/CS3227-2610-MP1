package fitbot.model;

import java.time.LocalDate;

/** Stores information shared by all workout types. */
public abstract class Workout {
    private final LocalDate date;
    private final long durationSeconds;

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

    /** Returns a display name for this workout type. */
    public abstract String getType();
}
