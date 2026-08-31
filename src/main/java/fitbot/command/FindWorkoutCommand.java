package fitbot.command;

import java.util.List;

import fitbot.exception.FitBotException;
import fitbot.formatter.DurationFormatter;
import fitbot.model.CycleWorkout;
import fitbot.model.GymWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;
import fitbot.model.WorkoutBlock;
import fitbot.model.WorkoutSet;

/**
 * Displays every stored and calculated attribute of one workout.
 */
public class FindWorkoutCommand extends Command {
    /** Creates the find command. */
    public FindWorkoutCommand() {
        super("find", "Display a workout by its list number.",
                "find <workout number>", "find 2");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts) throws FitBotException {
        if (arguments.isEmpty()) {
            throw new FitBotException("Usage: " + getUsage() + "\n\nExample: " + getExample());
        }
        if (arguments.size() != 1) {
            throw new FitBotException("Find requires exactly one workout number.");
        }

        int position;
        try {
            position = Integer.parseInt(arguments.get(0));
        } catch (NumberFormatException exception) {
            throw new FitBotException("Workout number must be an integer.");
        }
        if (position < 1 || position > workouts.size()) {
            throw new FitBotException("Invalid workout number.");
        }
        return new CommandResult(formatDetails(position, workouts.get(position - 1)), false);
    }

    private static String formatDetails(int position, Workout workout) {
        StringBuilder result = new StringBuilder("Workout ").append(position)
                .append("\nType: ").append(workout.getType())
                .append("\nDate: ").append(workout.getDate())
                .append("\nDuration: ").append(DurationFormatter.formatDuration(workout.getDurationSeconds()));

        if (workout instanceof RunWorkout run) {
            result.append("\nDistance: ").append(run.getDistanceKilometres()).append(" km")
                    .append("\nPace: ")
                    .append(DurationFormatter.formatDuration(Math.round(run.getPaceSecondsPerKilometre())))
                    .append("/km")
                    .append("\nElevation gain: ").append(value(run.getElevationGainMetres())).append(" metres");
        } else if (workout instanceof CycleWorkout cycle) {
            result.append("\nDistance: ").append(cycle.getDistanceKilometres()).append(" km")
                    .append("\nSpeed: ").append(String.format(java.util.Locale.ROOT, "%.1f",
                            cycle.getSpeedKilometresPerHour()))
                    .append(" km/hr")
                    .append("\nElevation gain: ").append(value(cycle.getElevationGainMetres())).append(" metres")
                    .append("\nMaximum speed: ").append(value(cycle.getMaxSpeedKilometresPerHour())).append(" km/hr");
        } else if (workout instanceof GymWorkout gym) {
            result.append("\nBlocks: ").append(gym.getBlocks().size())
                    .append("\nTotal volume: ").append(String.format("%.1f kg",
                            gym.getTotalVolumeKilograms()));
            for (int blockIndex = 0; blockIndex < gym.getBlocks().size(); blockIndex++) {
                WorkoutBlock block = gym.getBlocks().get(blockIndex);
                result.append("\nBlock ").append(blockIndex + 1).append(": ")
                        .append(block.getExerciseName())
                        .append("\n  Volume: ").append(String.format("%.1f kg",
                                block.getTotalVolumeKilograms()))
                        .append("\n  1 Rep Max: ").append(String.format("%.1f kg",
                                block.getOneRepMaxKilograms()));
                for (WorkoutSet set : block.getSets()) {
                    result.append("\n  Set: ").append(set.getRepetitions())
                            .append(" reps @ ").append(set.getWeightKilograms()).append(" kg");
                }
            }
        }

        return result.toString();
    }

    private static String value(Object value) {
        return value == null ? "N/A" : value.toString();
    }
}
