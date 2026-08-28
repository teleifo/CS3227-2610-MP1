package fitbot.command;

import java.util.List;

import fitbot.exception.FitBotException;

/**
 * Defines the common interface for commands supported by FitBot.
 *
 * <p>Each concrete command provides a keyword and description, then
 * implements the behavior performed when the command is selected.</p>
 */
public abstract class Command {
    /** The keyword used to select this command. */
    private final String keyword;

    /** A short explanation of what this command does. */
    private final String description;

    /**
     * Creates a command with the given identifying information.
     *
     * @param keyword the keyword used to select the command
     * @param description a short description of the command
     */
    protected Command(String keyword, String description) {
        this.keyword = keyword;
        this.description = description;
    }

    /**
     * Returns the keyword used to select this command.
     *
     * @return the command keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns the command description, which can be used by a help command.
     *
     * @return the command description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Runs the command.
     *
     * @param arguments arguments supplied after the command keyword
     * @return the command's output and whether the application should exit
     * @throws FitBotException if the arguments are invalid
     */
    public abstract CommandResult execute(List<String> arguments)
            throws FitBotException;
}
