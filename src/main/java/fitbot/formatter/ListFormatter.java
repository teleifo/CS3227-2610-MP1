package fitbot.formatter;

import java.util.Locale;

import fitbot.model.CycleWorkout;
import fitbot.model.GymWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;

/**
 * Formats workouts for the compact list and filter command output.
 */
public final class ListFormatter {
    /** Formats one workout with its list position and summary attributes. */
    public static String formatSummary(int position, Workout workout) {
        StringBuilder output = new StringBuilder();
        output.append(position).append(". ").append(workout.getType())
                .append(" - ").append(workout.getDate())
                .append(" - ").append(DurationFormatter.formatDuration(workout.getDurationSeconds()));

        if (workout instanceof RunWorkout run) {
            output.append(" - ").append(formatDistance(run.getDistanceKilometres())).append(" km")
                    .append(" - ").append(formatPace(run.getPaceSecondsPerKilometre())).append("/km");
        } else if (workout instanceof CycleWorkout cycle) {
            output.append(" - ").append(formatDistance(cycle.getDistanceKilometres())).append(" km")
                    .append(" - ").append(formatSpeed(cycle.getSpeedKilometresPerHour())).append(" km/hr");
        } else if (workout instanceof GymWorkout gym) {
            output.append(" - ").append(gym.getBlocks().size()).append(" blocks");
        }

        return output.toString();
    }

    private static String formatDistance(double distance) {
        return String.format(Locale.ROOT, "%.2f", distance);
    }

    private static String formatPace(double pace) {
        return DurationFormatter.formatDuration(Math.round(pace));
    }

    private static String formatSpeed(double speed) {
        return String.format(Locale.ROOT, "%.1f", speed);
    }
}
