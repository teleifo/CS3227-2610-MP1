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
                output.append(" - ").append(formatDistance(run.getDistanceKilometres())).append(" km");
                output.append(" - ").append(formatPace(run.getPaceSecondsPerKilometre())).append("/km");
                if (run.getElevationGainMetres() != null) {
                    output.append(" - ").append(formatElevation(run.getElevationGainMetres()))
                            .append(" m elevation gain");
                }
            } else if (workout instanceof CycleWorkout cycle) {
                output.append(" - ").append(formatDistance(cycle.getDistanceKilometres())).append(" km");
                output.append(" - ").append(formatSpeed(cycle.getSpeedKilometresPerHour())).append(" km/hr");
                if (cycle.getElevationGainMetres() != null) {
                    output.append(" - ").append(formatElevation(cycle.getElevationGainMetres()))
                            .append(" m elevation gain");
                }
                if (cycle.getMaxSpeedKilometresPerHour() != null) {
                    output.append(" - max ").append(formatSpeed(cycle.getMaxSpeedKilometresPerHour()))
                            .append(" km/hr");
                }
            }
            output.append("\n");
        }

        return new CommandResult(output.toString().trim(), false);
    }

    private static String formatDistance(double distance) {
        return String.format(Locale.ROOT, "%.2f", distance);
    }

    private static String formatElevation(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String formatPace(double pace) {
        long totalSeconds = Math.round(pace);
        return DurationFormatter.formatDuration(totalSeconds);
    }

    private static String formatSpeed(double speed) {
        return String.format(Locale.ROOT, "%.1f", speed);
    }
}
