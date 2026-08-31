package fitbot.command;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import fitbot.exception.FitBotException;
import fitbot.formatter.ListFormatter;
import fitbot.model.Workout;
import fitbot.model.WorkoutType;
import fitbot.parser.ArgumentParser;

/**
 * Lists workouts whose type matches the requested type.
 */
public class FilterWorkoutsCommand extends Command {
    /** Creates the filter command. */
    public FilterWorkoutsCommand() {
        super("filter", "List workouts filtered by type.",
                "filter -type <run|cycle|gym>", "filter -type run");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts) throws FitBotException {
        if (arguments.isEmpty()) {
            throw new FitBotException("Usage: " + getUsage() + "\n\nExample: " + getExample());
        }

        ArgumentParser.validateOptionNames(arguments, Set.of("-type"));
        Map<String, String> options = ArgumentParser.parseOptions(arguments);
        for (Map.Entry<String, String> option : options.entrySet()) {
            if (option.getValue() == null) {
                throw new FitBotException("Missing value for " + option.getKey() + ".");
            }
        }

        if (workouts.isEmpty()) {
            return new CommandResult("No workouts have been logged yet.", false);
        }

        final WorkoutType type;
        try {
            type = WorkoutType.valueOf(arguments.get(1).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FitBotException("Unsupported workout type.");
        }

        StringBuilder output = new StringBuilder();
        for (int index = 0; index < workouts.size(); index++) {
            if (workouts.get(index).getType() == type) {
                output.append(ListFormatter.formatSummary(index + 1, workouts.get(index))).append("\n");
            }
        }

        if (output.isEmpty()) {
            return new CommandResult("No workouts of type "
                    + type.toString().toLowerCase(Locale.ROOT) + " found.", false);
        }

        return new CommandResult(output.toString().trim(), false);
    }
}
