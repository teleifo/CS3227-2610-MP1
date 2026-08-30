package fitbot.model;

/**
 * A single exercise set with a repetition count and weight.
 */
public record WorkoutSet(int repetitions, double weightKilograms) {
    /** Validates the values recorded for a set. */
    public WorkoutSet {
        if (repetitions <= 0) {
            throw new IllegalArgumentException("Repetitions must be positive.");
        } else if (!Double.isFinite(weightKilograms) || weightKilograms < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }
    }
}
