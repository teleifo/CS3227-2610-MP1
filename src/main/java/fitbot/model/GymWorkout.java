package fitbot.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
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
    private static final Set<String> SUPPORTED_OPTIONS = Set.of(
            "-type", "-date", "-duration", "-blocks");
    private final List<WorkoutBlock> blocks;

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
        return blocks;
    }

    public static Set<String> getSupportedOptions() {
        return SUPPORTED_OPTIONS;
    }

    @Override
    public WorkoutType getType() {
        return WorkoutType.GYM;
    }
}
