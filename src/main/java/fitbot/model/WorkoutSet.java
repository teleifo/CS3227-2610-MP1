package fitbot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single exercise set with a repetition count and weight.
 */
public class WorkoutSet {
    private int repetitions;
    private double weightKilograms;

    /** Creates a workout set **/
    @JsonCreator
    public WorkoutSet(@JsonProperty("repetitions") int repetitions,
            @JsonProperty("weightKilograms") double weightKilograms) {
        setRepetitions(repetitions);
        setWeightKilograms(weightKilograms);
    }

    public int getRepetitions() {
        return repetitions;
    }

    public double getWeightKilograms() {
        return weightKilograms;
    }

    public void setRepetitions(int repetitions) {
        if (repetitions <= 0) {
            throw new IllegalArgumentException("Repetitions must be positive.");
        }
        this.repetitions = repetitions;
    }

    public void setWeightKilograms(double weightKilograms) {
        if (!Double.isFinite(weightKilograms) || weightKilograms <= 0) {
            throw new IllegalArgumentException("Weight must be positive.");
        }
        this.weightKilograms = weightKilograms;
    }
}
