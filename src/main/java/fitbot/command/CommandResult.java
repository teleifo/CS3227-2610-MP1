package fitbot.command;

/**
 * Represents the outcome of executing a FitBot command.
 */
public final class CommandResult {
    /** Text that should be shown to the user. */
    private final String message;

    /** Whether FitBot should terminate after displaying the message. */
    private final boolean shouldExit;

    /**
     * Creates a command result.
     *
     * @param message text that should be shown to the user
     * @param shouldExit whether the application should terminate
     */
    public CommandResult(String message, boolean shouldExit) {
        this.message = message;
        this.shouldExit = shouldExit;
    }

    /**
     * Returns the message produced by the command.
     *
     * @return the command message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Indicates whether the application should terminate.
     *
     * @return true when the application should exit
     */
    public boolean shouldExit() {
        return shouldExit;
    }
}
