package fitbot.service;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import fitbot.command.CommandResult;
import fitbot.command.ParsedCommand;
import fitbot.exception.FitBotException;
import fitbot.model.Workout;
import fitbot.storage.WorkoutStorage;

/**
 * Coordinates workout operations shared by FitBot user interfaces.
 *
 * <p>The service owns the in-memory workout list and persistence. Interfaces
 * such as the current command-line interface and the future GUI can therefore
 * use the same command execution and saving behaviour.</p>
 */
public class WorkoutService {
    /** Workouts currently loaded by the application. */
    private final List<Workout> workouts;

    /** Storage used to load and save workouts. */
    private final WorkoutStorage storage;

    /** Creates a service using the application's default data file. */
    public WorkoutService() throws FitBotException {
        this(Path.of("data", "workouts.json"));
    }

    /**
     * Creates a service backed by the supplied file.
     *
     * @param storagePath path of the JSON workout file
     * @throws FitBotException if existing workouts cannot be loaded
     */
    public WorkoutService(Path storagePath) throws FitBotException {
        storage = new WorkoutStorage(storagePath);
        workouts = storage.loadWorkouts();
    }

    /**
     * Executes a parsed command and persists changes made by that command.
     *
     * @param parsedCommand command and its arguments
     * @return the command result
     * @throws FitBotException if the command or persistence operation fails
     */
    public CommandResult execute(ParsedCommand parsedCommand) throws FitBotException {
        CommandResult result = parsedCommand.getCommand().execute(parsedCommand.getArguments(), workouts);
        if (result.wasDataModified()) {
            storage.saveWorkouts(workouts);
        }
        return result;
    }

    /** Returns a read-only view of the currently loaded workouts. */
    public List<Workout> getWorkouts() {
        return Collections.unmodifiableList(workouts);
    }
}
