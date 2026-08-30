package fitbot.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A strength-training workout made up of exercise blocks.
 */
public class GymWorkout extends Workout {
    private static final Set<String> SUPPORTED_OPTIONS = Set.of(
            "-type", "-date", "-duration", "-blocks");
    private final List<WorkoutBlock> blocks;

    /** Creates a gym workout. */
    public GymWorkout(LocalDate date, long durationSeconds, List<WorkoutBlock> blocks) {
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

    @Override public WorkoutType getType() {
        return WorkoutType.GYM;
    }

    /** Stores blocks as the same compact notation accepted by the command. */
    @Override public Map<String, Object> getStorageFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("blocks", blocks.stream().map(block -> block.exerciseName() + ":"
                + block.sets().stream().map(set -> set.repetitions() + "@" + set.weightKilograms())
                .collect(Collectors.joining(",")))
                .collect(Collectors.joining(";")));
        return fields;
    }
}
