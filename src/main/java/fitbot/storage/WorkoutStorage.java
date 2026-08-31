package fitbot.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fitbot.exception.FitBotException;
import fitbot.model.Workout;

/**
 * Reads and writes workouts in the application's local JSON file.
 */
public class WorkoutStorage {
    private final Path file;
    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    /** Creates storage backed by the supplied file. */
    public WorkoutStorage(Path file) {
        this.file = file;
    }

    /** Loads all saved workouts, returning an empty list for a first run. */
    public List<Workout> loadWorkouts() throws FitBotException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        try {
            return mapper.readValue(Files.readString(file), new TypeReference<>() {});
        } catch (IOException | RuntimeException exception) {
            throw new FitBotException("Could not load workouts: " + exception.getMessage());
        }
    }

    /** Saves all workouts, creating the data directory when necessary. */
    public void saveWorkouts(List<Workout> workouts) throws FitBotException {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(file, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(workouts));
        } catch (IOException | RuntimeException exception) {
            throw new FitBotException("Could not save workouts: " + exception.getMessage());
        }
    }
}
