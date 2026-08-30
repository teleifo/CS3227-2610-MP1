package fitbot.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitbot.exception.FitBotException;
import fitbot.model.CycleWorkout;
import fitbot.model.GymWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;

/**
 * Reads and writes workouts in the application's local JSON file.
 */
public class WorkoutStorage {
    private static final Pattern OBJECT = Pattern.compile("\\{(.*?)}", Pattern.DOTALL);
    private static final Pattern VALUE = Pattern.compile(
            "\"(\\w+)\"\\s*:\\s*(?:\"([^\"]*)\"|(null)|([-+.0-9]+))");
    private final Path file;
    private final Map<String, Function<Map<String, String>, Workout>> loaders = new HashMap<>();

    /** Creates storage backed by the supplied file. */
    public WorkoutStorage(Path file) {
        this.file = file;
        register("RUN", WorkoutStorage::createRunWorkout);
        register("CYCLE", WorkoutStorage::createCycleWorkout);
        register("GYM", WorkoutStorage::createGymWorkout);
    }

    /** Registers how a workout type is reconstructed from JSON fields. */
    public final void register(String type, Function<Map<String, String>, Workout> loader) {
        loaders.put(type, loader);
    }

    /** Loads all saved workouts, returning an empty list for a first run. */
    public List<Workout> loadWorkouts() throws FitBotException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(file);
            List<Workout> workouts = new ArrayList<>();
            Matcher objects = OBJECT.matcher(json);
            while (objects.find()) {
                workouts.add(parseWorkout(objects.group(1)));
            }

            return workouts;
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

            StringBuilder json = new StringBuilder("[\n");
            for (int i = 0; i < workouts.size(); i++) {
                json.append(toJson(workouts.get(i)));
                if (i < workouts.size() - 1) {
                    json.append(',');
                }
                json.append('\n');
            }

            Files.writeString(file, json.append(']').toString());
        } catch (IOException exception) {
            throw new FitBotException("Could not save workouts: " + exception.getMessage());
        }
    }

    private Workout parseWorkout(String object) {
        Map<String, String> values = new HashMap<>();
        Matcher fields = VALUE.matcher(object);
        while (fields.find()) {
            if (fields.group(3) != null) {
                values.put(fields.group(1), null);
            } else {
                values.put(fields.group(1), fields.group(2));
            }

            if (fields.group(4) != null) {
                values.put(fields.group(1), fields.group(4));
            }
        }

        String type = required(values, "type");
        Function<Map<String, String>, Workout> loader = loaders.get(type);
        if (loader == null) {
            throw new IllegalArgumentException("Unknown workout type: " + type);
        }

        return loader.apply(values);
    }

    private static Workout createRunWorkout(Map<String, String> values) {
        LocalDate date = LocalDate.parse(required(values, "date"));
        long duration = Long.parseLong(required(values, "durationSeconds"));
        double distance = Double.parseDouble(required(values, "distanceKilometres"));
        String elevationText = values.get("elevationGainMetres");
        Double elevation = elevationText == null ? null : Double.valueOf(elevationText);

        return new RunWorkout(date, duration, distance, elevation);
    }

    private static Workout createCycleWorkout(Map<String, String> values) {
        LocalDate date = LocalDate.parse(required(values, "date"));
        long duration = Long.parseLong(required(values, "durationSeconds"));
        double distance = Double.parseDouble(required(values, "distanceKilometres"));
        String elevationText = values.get("elevationGainMetres");
        Double elevation = elevationText == null ? null : Double.valueOf(elevationText);
        String maxSpeedText = values.get("maxSpeedKilometresPerHour");
        Double maxSpeed = maxSpeedText == null ? null : Double.valueOf(maxSpeedText);

        return new CycleWorkout(date, duration, distance, elevation, maxSpeed);
    }

    private static Workout createGymWorkout(Map<String, String> values) {
        LocalDate date = LocalDate.parse(required(values, "date"));
        long duration = Long.parseLong(required(values, "durationSeconds"));
        try {
            return new GymWorkout(date, duration,
                    fitbot.command.LogWorkoutCommand.parseBlocks(required(values, "blocks")));
        } catch (FitBotException exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    private static String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing field: " + key);
        }

        return value;
    }

    private static String toJson(Workout workout) {
        Map<String, Object> fields = new java.util.LinkedHashMap<>();
        fields.put("type", workout.getType().name());
        fields.put("date", workout.getDate());
        fields.put("durationSeconds", workout.getDurationSeconds());
        fields.putAll(workout.getStorageFields());

        StringBuilder json = new StringBuilder("  {\n");
        int index = 0;
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            json.append(String.format(Locale.ROOT, "    \"%s\": %s", field.getKey(),
                    jsonValue(field.getValue())));
            if (++index < fields.size()) {
                json.append(',');
            }
            json.append('\n');
        }

        return json.append("  }").toString();
    }

    private static String jsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String || value instanceof LocalDate) {
            return "\"" + value + "\"";
        }

        return value.toString();
    }
}
