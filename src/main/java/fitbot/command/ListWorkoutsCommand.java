package fitbot.command;

import java.util.List;

import fitbot.exception.FitBotException;
import fitbot.formatter.ListFormatter;
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
            throw new FitBotException("List does not accept arguments.");
        }

        if (workouts.isEmpty()) {
            return new CommandResult("No workouts have been logged yet.", false);
        }

        StringBuilder output = new StringBuilder();
        for (int index = 0; index < workouts.size(); index++) {
            output.append(ListFormatter.formatSummary(index + 1, workouts.get(index))).append("\n");
        }

        return new CommandResult(output.toString().trim(), false);
    }

}
