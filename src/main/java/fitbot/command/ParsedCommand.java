package fitbot.command;

import java.util.List;

/** Stores a command together with the arguments supplied by the user. */
public final class ParsedCommand {
    /** The command identified by the parser. */
    private final Command command;

    /** Arguments supplied after the command keyword. */
    private final List<String> arguments;

    /** Creates a parsed command with an immutable argument list. */
    public ParsedCommand(Command command, List<String> arguments) {
        this.command = command;
        this.arguments = List.copyOf(arguments);
    }

    /** @return the identified command */
    public Command getCommand() {
        return command;
    }

    /** @return an immutable list of command arguments */
    public List<String> getArguments() {
        return arguments;
    }
}
