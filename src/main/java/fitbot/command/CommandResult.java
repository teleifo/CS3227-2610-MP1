package fitbot.command;

/**
 * Represents the outcome of executing a FitBot command.
 */
public final class CommandResult {
    /** Text that should be shown to the user. */
    private final String message;

    /** Whether FitBot should terminate after displaying the message. */
    private final boolean shouldExit;

    /** Whether the command changed data that should be persisted. */
    private final boolean dataModified;

    /**
     * Creates a command result.
     *
     * @param message text that should be shown to the user
     * @param shouldExit whether the application should terminate
     */
    public CommandResult(String message, boolean shouldExit) {
        this(message, shouldExit, false);
    }

    /**
     * Creates a result with an explicit persistence flag.
     *
     * @param message text that should be shown to the user
     * @param shouldExit whether the application should terminate
     * @param dataModified whether data that should be persisted was changed
     */
    public CommandResult(String message, boolean shouldExit, boolean dataModified) {
        this.message = message;
        this.shouldExit = shouldExit;
        this.dataModified = dataModified;
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

    /** Indicates whether the command changed application data. */
    public boolean wasDataModified() {
        return dataModified;
    }
}
