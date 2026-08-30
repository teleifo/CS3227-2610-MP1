package fitbot.model;

import java.util.List;

/**
 * A named exercise and the sets performed for it.
 */
public record WorkoutBlock(String exerciseName, List<WorkoutSet> sets) {
    /** Validates and defensively copies a block's data. */
    public WorkoutBlock {
        if (exerciseName == null || exerciseName.isBlank()) {
            throw new IllegalArgumentException("A block needs an exercise name.");
        }
        if (sets == null || sets.isEmpty()) {
            throw new IllegalArgumentException("A block needs at least one set.");
        }
        sets = List.copyOf(sets);
    }
}
