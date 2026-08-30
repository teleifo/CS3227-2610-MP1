package fitbot.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fitbot.command.ByeCommand;
import fitbot.command.Command;
import fitbot.command.DeleteWorkoutCommand;
import fitbot.command.EditWorkoutCommand;
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
        registerCommand(new EditWorkoutCommand());
        registerCommand(new DeleteWorkoutCommand());
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

        List<String> words = tokenize(input.trim());
        Command command = commands.get(words.get(0));
        if (command == null) {
            throw new FitBotException("Unknown command: " + words.get(0));
        }

        List<String> arguments = words.subList(1, words.size());
        return new ParsedCommand(command, arguments);
    }

    /**
     * Splits input into arguments while preserving whitespace inside quotes.
     * Both single and double quotes are supported and removed from the values.
     *
     * @param input the trimmed command input
     * @return the tokenized command and arguments
     * @throws FitBotException if a quote is not closed
     */
    private static List<String> tokenize(String input) throws FitBotException {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char quote = 0;
        boolean tokenStarted = false;

        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);

            if (quote != 0) {
                if (character == quote) {
                    quote = 0;
                } else {
                    current.append(character);
                }
            } else if (character == '\'' || character == '"') {
                quote = character;
                tokenStarted = true;
            } else if (Character.isWhitespace(character)) {
                if (tokenStarted) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    tokenStarted = false;
                }
            } else {
                current.append(character);
                tokenStarted = true;
            }
        }

        if (quote != 0) {
            throw new FitBotException("Unterminated quote.");
        }

        if (tokenStarted) {
            tokens.add(current.toString());
        }

        return tokens;
    }
}
