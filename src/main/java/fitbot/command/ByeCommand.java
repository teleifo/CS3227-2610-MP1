package fitbot.command;

import java.util.List;

/**
 * Command that ends the FitBot application.
 */
public class ByeCommand extends Command {
    /** Keyword used to select the goodbye command. */
    private static final String KEYWORD = "bye";

    /** Message displayed before FitBot terminates. */
    private static final String GOODBYE_MESSAGE =
            "See you soon! Until then, stay fit!";

    /** Creates the goodbye command. */
    public ByeCommand() {
        super(KEYWORD, "Exit FitBot.");
    }

    /**
     * Produces the goodbye message and signals that FitBot should exit.
     *
     * @param arguments command arguments; they are not needed by this command
     * @return a result that requests application termination
     */
    @Override
    public CommandResult execute(List<String> arguments) {
        return new CommandResult(GOODBYE_MESSAGE, true);
    }
}
