package fitbot.command;

import java.util.List;

import fitbot.exception.FitBotException;
import fitbot.model.Workout;

/**
 * Command that deletes a workout using its position in the list.
 */
public class DeleteWorkoutCommand extends Command {
    /** Creates the delete command. */
    public DeleteWorkoutCommand() {
        super("delete", "Delete a workout by its list number.",
                "delete <workout number>", "delete 2");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts)
            throws FitBotException {
        if (arguments.size() != 1) {
            throw new FitBotException("Usage: " + getUsage() + "\n\nExample: " + getExample());
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

        workouts.remove(position - 1);

        return new CommandResult("Workout deleted successfully.", false, true);
    }
}
