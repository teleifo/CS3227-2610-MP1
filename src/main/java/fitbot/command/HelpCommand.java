package fitbot.command;

import java.util.List;
import java.util.stream.Collectors;

import fitbot.exception.FitBotException;
import fitbot.model.Workout;
import fitbot.parser.Parser;

/**
 * Command that displays all commands and their descriptions.
 */
public class HelpCommand extends Command {
    /** Creates the help command. */
    public HelpCommand() {
        super("help", "Display all available commands / command usage.",
                "help [command]", "help log");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts)
            throws FitBotException {
        if (!arguments.isEmpty()) {
            String keyword = arguments.get(0);
            Command command = Parser.getCommands().stream()
                    .filter(item -> item.getKeyword().equals(keyword))
                    .findFirst()
                    .orElseThrow(() -> new FitBotException(
                            "Unknown command: " + keyword + "."));
            return new CommandResult(command.getKeyword() + "\n" + command.getDescription()
                    + "\nUsage: " + command.getUsage() + "\nExample: " + command.getExample(), false);
        }

        String commandDescriptions = Parser.getCommands().stream()
                .map(command -> command.getKeyword() + " - " + command.getDescription())
                .collect(Collectors.joining("\n"));

        return new CommandResult(
                "Available commands:\n" + commandDescriptions, false);
    }
}
