package fitbot.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A strength-training workout made up of exercise blocks.
 */
@JsonPropertyOrder({
    "type",
    "date",
    "durationSeconds",
    "blocks"
})
public class GymWorkout extends Workout {
    private static final Set<String> SUPPORTED_LOG_OPTIONS = Set.of(
            "-type", "-date", "-duration", "-blocks");
    private static final Set<String> SUPPORTED_EDIT_OPTIONS = Set.of(
            "-type", "-date", "-duration", "-blocks", "-block", "-name", "-sets", "-set", "-reps", "-weight");
    private List<WorkoutBlock> blocks;

    /** Creates a gym workout. */
    @JsonCreator
    public GymWorkout(@JsonProperty("date") LocalDate date,
            @JsonProperty("durationSeconds") long durationSeconds,
            @JsonProperty("blocks") List<WorkoutBlock> blocks) {
        super(date, durationSeconds);
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("A gym workout needs at least one block.");
        }
        this.blocks = List.copyOf(blocks);
    }

    public List<WorkoutBlock> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    /**
     * Calculates the total weight lifted across every set in this workout.
     *
     * @return the sum of repetitions multiplied by weight for every set, in kilograms
     */
    @JsonIgnore
    public double getTotalVolumeKilograms() {
        return blocks.stream()
                .flatMap(block -> block.getSets().stream())
                .mapToDouble(set -> set.getRepetitions() * set.getWeightKilograms())
                .sum();
    }

    /** Replaces every exercise block while preserving the workout metadata. */
    public void replaceBlocks(List<WorkoutBlock> value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("A gym workout needs at least one block.");
        }
        blocks = List.copyOf(value);
    }

    public static Set<String> getSupportedLogOptions() {
        return SUPPORTED_LOG_OPTIONS;
    }

    public static Set<String> getSupportedEditOptions() {
        return SUPPORTED_EDIT_OPTIONS;
    }

    @Override
    public WorkoutType getType() {
        return WorkoutType.GYM;
    }
}
