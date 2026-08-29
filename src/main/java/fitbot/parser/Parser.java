package fitbot.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fitbot.command.ByeCommand;
import fitbot.command.Command;
import fitbot.command.HelpCommand;
import fitbot.command.ListWorkoutsCommand;
import fitbot.command.LogWorkoutCommand;
import fitbot.command.ParsedCommand;
import fitbot.exception.FitBotException;

/**
 * Converts user input strings into executable commands and arguments.
 */
public class Parser {
    /** Commands indexed by their keywords. */
    private static final Map<String, Command> commands = new LinkedHashMap<>();

    static {
        registerCommand(new HelpCommand());
        registerCommand(new LogWorkoutCommand());
        registerCommand(new ListWorkoutsCommand());
        registerCommand(new ByeCommand());
    }

    /** Adds a command to the parser's command registry. */
    private static void registerCommand(Command command) {
        commands.put(command.getKeyword(), command);
    }

    /** Returns the registered commands without exposing the mutable registry. */
    public static Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    /**
     * Parses an input string into a command and its arguments.
     *
     * @throws FitBotException if input is empty or the command is unknown
     */
    public static ParsedCommand parseCommand(String input) throws FitBotException {
        if (input == null || input.trim().isEmpty()) {
            throw new FitBotException("Input cannot be empty.");
        }

        String[] words = input.trim().split("\\s+");
        Command command = commands.get(words[0]);
        if (command == null) {
            throw new FitBotException("Unknown command: " + words[0]);
        }

        List<String> arguments = Arrays.asList(words).subList(1, words.length);
        return new ParsedCommand(command, arguments);
    }
}
