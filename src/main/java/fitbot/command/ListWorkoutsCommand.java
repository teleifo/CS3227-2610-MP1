package fitbot.command;

import java.util.List;

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
                    .append(" - ").append(workout.getDurationSeconds())
                    .append(" seconds");

            if (workout instanceof fitbot.model.RunWorkout run) {
                output.append(" - ").append(run.getDistanceKilometres()).append(" km");
                appendElevation(output, run.getElevationGainMetres());
            } else if (workout instanceof fitbot.model.CycleWorkout cycle) {
                output.append(" - ").append(cycle.getDistanceKilometres()).append(" km");
                appendElevation(output, cycle.getElevationGainMetres());
            }
            output.append("\n");
        }

        return new CommandResult(output.toString().trim(), false);
    }

    private static void appendElevation(StringBuilder output, Double elevation) {
        if (elevation != null) {
            output.append(" - ").append(elevation).append(" m elevation");
        }
    }
}
