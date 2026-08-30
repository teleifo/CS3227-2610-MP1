package fitbot.command;

import java.util.List;

import fitbot.model.Workout;

/**
 * Command that ends the FitBot application.
 */
public class ByeCommand extends Command {
    /** Creates the goodbye command. */
    public ByeCommand() {
        super("bye", "Exit FitBot.", "bye", "bye");
    }

    /**
     * Produces the goodbye message and signals that FitBot should exit.
     *
     * @param arguments command arguments; they are not needed by this command
     * @param workouts the application's in-memory workout list
     * @return a result that requests application termination
     */
    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts) {
        return new CommandResult("See you soon! Until then, stay fit!", true);
    }
}
