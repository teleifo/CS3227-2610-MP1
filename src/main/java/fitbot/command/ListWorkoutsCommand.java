package fitbot.command;

import java.util.List;
import java.util.Locale;

import fitbot.exception.FitBotException;
import fitbot.formatter.DurationFormatter;
import fitbot.model.CycleWorkout;
import fitbot.model.GymWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;

/**
 * Command that lists all workouts logged.
 */
public class ListWorkoutsCommand extends Command {
    /** Creates the list command. */
    public ListWorkoutsCommand() {
        super("list", "List logged workouts.", "list", "list");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts)
            throws FitBotException {
        if (!arguments.isEmpty()) {
            throw new FitBotException("Usage: " + getUsage());
        }

        if (workouts.isEmpty()) {
            return new CommandResult("No workouts have been logged yet.", false);
        }

        StringBuilder output = new StringBuilder();
        for (int index = 0; index < workouts.size(); index++) {
            Workout workout = workouts.get(index);
            output.append(index + 1).append(". ")
                    .append(workout.getType())
                    .append(" - ").append(workout.getDate())
                    .append(" - ").append(DurationFormatter.formatDuration(workout.getDurationSeconds()));

            if (workout instanceof RunWorkout run) {
                output.append(" - ").append(formatDistance(run.getDistanceKilometres())).append(" km");
                output.append(" - ").append(formatPace(run.getPaceSecondsPerKilometre())).append("/km");
            } else if (workout instanceof CycleWorkout cycle) {
                output.append(" - ").append(formatDistance(cycle.getDistanceKilometres())).append(" km");
                output.append(" - ").append(formatSpeed(cycle.getSpeedKilometresPerHour())).append(" km/hr");
            } else if (workout instanceof GymWorkout gym) {
                output.append(" - ").append(gym.getBlocks().size()).append(" blocks");
            }
            output.append("\n");
        }

        return new CommandResult(output.toString().trim(), false);
    }

    private static String formatDistance(double distance) {
        return String.format(Locale.ROOT, "%.2f", distance);
    }

    private static String formatPace(double pace) {
        long totalSeconds = Math.round(pace);
        return DurationFormatter.formatDuration(totalSeconds);
    }

    private static String formatSpeed(double speed) {
        return String.format(Locale.ROOT, "%.1f", speed);
    }
}
