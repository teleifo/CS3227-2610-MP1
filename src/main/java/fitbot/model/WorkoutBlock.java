package fitbot.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
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
