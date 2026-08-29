package fitbot.command;

import java.util.List;
import java.util.Locale;

import fitbot.formatter.DurationFormatter;
import fitbot.model.CycleWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;

/**
 * Command that lists all workouts logged.
 */
public class ListWorkoutsCommand extends Command {
    /** Creates the list command. */
    public ListWorkoutsCommand() {
        super("list", "List logged workouts.");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts) {
        if (workouts.isEmpty()) {
            return new CommandResult("No workouts have been logged yet.", false);
        }

        StringBuilder output = new StringBuilder();
        for (Workout workout : workouts) {
            output.append(workout.getType())
                    .append(" - ").append(workout.getDate())
                    .append(" - ").append(DurationFormatter.formatDuration(workout.getDurationSeconds()));

            if (workout instanceof RunWorkout run) {
                output.append(" - ").append(run.getDistanceKilometres()).append(" km");
                output.append(" - ").append(formatPace(run)).append(" min/km");
                appendElevation(output, run.getElevationGainMetres());
            } else if (workout instanceof CycleWorkout cycle) {
                output.append(" - ").append(cycle.getDistanceKilometres()).append(" km");
                output.append(" - ").append(formatSpeed(cycle)).append(" km/hr");
                appendElevation(output, cycle.getElevationGainMetres());
            }
            output.append("\n");
        }

        return new CommandResult(output.toString().trim(), false);
    }

    private static void appendElevation(StringBuilder output, Double elevation) {
        if (elevation != null) {
            output.append(" - ").append(elevation).append(" m elevation gain");
        }
    }

    private static String formatPace(RunWorkout workout) {
        long totalSeconds = Math.round(workout.getPaceSecondsPerKilometre());
        return DurationFormatter.formatDuration(totalSeconds);
    }

    private static String formatSpeed(CycleWorkout workout) {
        return String.format(Locale.ROOT, "%.1f", workout.getSpeedKilometresPerHour());
    }
}
