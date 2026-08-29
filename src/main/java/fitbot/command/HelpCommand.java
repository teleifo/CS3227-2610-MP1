package fitbot.command;

import java.util.List;
import java.util.stream.Collectors;

import fitbot.model.Workout;
import fitbot.parser.Parser;

/**
 * Displays all commands and their descriptions.
 */
public class HelpCommand extends Command {
    /** Creates the help command. */
    public HelpCommand() {
        super("help", "Display all available commands.");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts) {
        String commandDescriptions = Parser.getCommands().stream()
                .map(command -> command.getKeyword() + " - " + command.getDescription())
                .collect(Collectors.joining("\n"));

        return new CommandResult(
                "Available commands:\n" + commandDescriptions, false);
    }
}
