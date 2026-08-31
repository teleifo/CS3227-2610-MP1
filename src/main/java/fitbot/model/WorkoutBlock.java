package fitbot.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A named exercise and the sets performed for it.
 */
public class WorkoutBlock {
    private String exerciseName;
    private List<WorkoutSet> sets;

    /** Creates a workout block **/
    @JsonCreator
    public WorkoutBlock(@JsonProperty("exerciseName") String exerciseName,
            @JsonProperty("sets") List<WorkoutSet> sets) {
        this.exerciseName = exerciseName;
        this.sets = sets;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public List<WorkoutSet> getSets() {
        return Collections.unmodifiableList(sets);
    }

    /**
     * Calculates the total weight lifted across every set in this block.
     *
     * @return the sum of repetitions multiplied by weight, in kilograms
     */
    @JsonIgnore
    public double getTotalVolumeKilograms() {
        return sets.stream()
                .mapToDouble(set -> set.getRepetitions() * set.getWeightKilograms())
                .sum();
    }

    /**
     * Calculates the best estimated one-repetition maximum across this block's sets.
     *
     * @return the largest estimated 1RM, in kilograms
     */
    @JsonIgnore
    public double getOneRepMaxKilograms() {
        return sets.stream()
                .mapToDouble(set -> set.getWeightKilograms()
                        * (1 + 0.0333 * set.getRepetitions()))
                .max()
                .orElse(0);
    }

    public void setExerciseName(String exerciseName) {
        if (exerciseName == null || exerciseName.isBlank()) {
            throw new IllegalArgumentException("A block needs an exercise name.");
        }
        this.exerciseName = exerciseName;
    }

    public void setSets(List<WorkoutSet> sets) {
        if (sets == null || sets.isEmpty()) {
            throw new IllegalArgumentException("A block needs at least one set.");
        }
        this.sets = List.copyOf(sets);
    }
}
