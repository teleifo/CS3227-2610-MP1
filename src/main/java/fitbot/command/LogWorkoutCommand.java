package fitbot.command;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import fitbot.exception.FitBotException;
import fitbot.model.CycleWorkout;
import fitbot.model.GymWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;
import fitbot.model.WorkoutBlock;
import fitbot.model.WorkoutSet;
import fitbot.model.WorkoutType;
import fitbot.parser.ArgumentParser;

/**
 * Command that logs a workout.
 */
public class LogWorkoutCommand extends Command {
    /** Creates the log command. */
    public LogWorkoutCommand() {
        super("log", "Log a workout.",
                """
                        (Run) log -type run -date <YYYY-MM-DD> -duration <seconds> \
                        -distance <kilometres> [-elevation <metres>]
                        (Cycle) log -type cycle -date <YYYY-MM-DD> -duration <seconds> \
                        -distance <kilometres> [-elevation <metres>] [-max <km/hr>]
                        (Gym) log -type gym -date <YYYY-MM-DD> -duration <seconds> \
                        -blocks "<exercise>:<set-entry>,...;..."
                        - Set entries use positive <reps>@<kg> or <sets>x<reps>@<kg>.
                        - Use commas for multiple sets and semicolons for multiple blocks.""",
                """
                        (Run) log -type run -date 2026-09-01 -duration 1800 -distance 5
                        (Gym) log -type gym -date 2026-09-01 -duration 3600 -blocks "Curls:10@12,2x10@10\"""");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts)
            throws FitBotException {
        if (arguments.isEmpty()) {
            throw new FitBotException("Usage: " + getUsage() + "\n\nExample: " + getExample());
        }

        Map<String, String> options;
        Set<String> allSupportedOptions = new HashSet<>(RunWorkout.getSupportedOptions());
        allSupportedOptions.addAll(CycleWorkout.getSupportedOptions());
        allSupportedOptions.addAll(GymWorkout.getSupportedLogOptions());

        ArgumentParser.validateOptionNames(arguments, allSupportedOptions);
        options = ArgumentParser.parseOptions(arguments);
        for (Map.Entry<String, String> option : options.entrySet()) {
            if (option.getValue() == null) {
                throw new FitBotException("Missing value for " + option.getKey() + ".");
            }
        }

        String typeText = ArgumentParser.getRequiredOption(options, "-type");
        WorkoutType type;
        try {
            type = WorkoutType.valueOf(typeText.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FitBotException("Unsupported workout type.");
        }

        Set<String> supportedOptions;
        switch (type) {
        case RUN:
            supportedOptions = RunWorkout.getSupportedOptions();
            break;

        case CYCLE:
            supportedOptions = CycleWorkout.getSupportedOptions();
            break;

        case GYM:
            supportedOptions = GymWorkout.getSupportedLogOptions();
            break;

        default:
            throw new FitBotException("Unsupported workout type.");
        }

        for (String option : options.keySet()) {
            if (!supportedOptions.contains(option)) {
                throw new FitBotException("Unknown option: " + option + ".");
            }
        }
        LocalDate date = ArgumentParser.parseDate(
                ArgumentParser.getRequiredOption(options, "-date"));
        long duration = ArgumentParser.parseLong(
                ArgumentParser.getRequiredOption(options, "-duration"), "duration");
        if (duration <= 0) {
            throw new FitBotException("Duration must be positive.");
        }

        Workout workout;
        if (type == WorkoutType.RUN || type == WorkoutType.CYCLE) {
            double distance = ArgumentParser.parseDouble(
                    ArgumentParser.getRequiredOption(options, "-distance"), "distance");
            Double elevation = options.containsKey("-elevation")
                    ? ArgumentParser.parseDouble(options.get("-elevation"), "elevation") : null;
            Double maxSpeed = options.containsKey("-max")
                    ? ArgumentParser.parseDouble(options.get("-max"), "max speed") : null;

            if (distance <= 0) {
                throw new FitBotException("Distance must be positive.");
            }
            if (elevation != null && elevation < 0) {
                throw new FitBotException("Elevation cannot be negative.");
            }
            if (maxSpeed != null && maxSpeed <= 0) {
                throw new FitBotException("Maximum speed must be positive.");
            }

            workout = (type == WorkoutType.RUN) ? new RunWorkout(date, duration, distance, elevation)
                    : new CycleWorkout(date, duration, distance, elevation, maxSpeed);
        } else {
            workout = new GymWorkout(date, duration, parseBlocks(
                    ArgumentParser.getRequiredOption(options, "-blocks")));
        }
        workouts.add(workout);

        return new CommandResult("Workout logged successfully.", false, true);
    }

    /**
     * Parses a semicolon-separated list of exercise blocks.
     *
     * <p>Each block has the form {@code Exercise:set-entry,set-entry,...}.
     * A set entry such as {@code 8@70} records one set, while {@code 3x8@60}
     * expands to three sets of eight repetitions at 60 kg. Repetition counts,
     * set counts, and weights must be positive. Mixed entries preserve their
     * input order.</p>
     *
     * @param text compact block notation supplied by the user
     * @return parsed exercise blocks in input order
     * @throws FitBotException if a block or set entry is malformed
     */
    public static List<WorkoutBlock> parseBlocks(String text) throws FitBotException {
        List<WorkoutBlock> blocks = new ArrayList<>();

        for (String blockText : text.split(";", -1)) {
            String[] blockParts = blockText.split(":", 2);
            if (blockParts.length != 2 || blockParts[0].isBlank()) {
                throw new FitBotException("Invalid block.");
            }

            String[] sets = blockParts[1].split(",", -1);
            List<WorkoutSet> parsedSets = new ArrayList<>();
            String currentSet = "";
            try {
                for (String set : sets) {
                    currentSet = set;
                    String[] values = set.split("@", 2);
                    if (values.length != 2) {
                        throw new NumberFormatException();
                    }

                    String[] reps = values[0].split("x", 2);
                    if (reps.length == 2) {
                        int count = Integer.parseInt(reps[0]);
                        int repetitions = Integer.parseInt(reps[1]);
                        if (count <= 0 || repetitions <= 0) {
                            throw new NumberFormatException();
                        }

                        for (int i = 0; i < count; i++) {
                            parsedSets.add(new WorkoutSet(repetitions, Double.parseDouble(values[1])));
                        }
                    } else {
                        int repetitions = Integer.parseInt(values[0]);
                        if (repetitions <= 0) {
                            throw new NumberFormatException();
                        }

                        parsedSets.add(new WorkoutSet(repetitions, Double.parseDouble(values[1])));
                    }
                }

                blocks.add(new WorkoutBlock(blockParts[0].trim(), parsedSets));
            } catch (IllegalArgumentException exception) {
                throw new FitBotException("Invalid set entry in block '" + blockParts[0].trim() + "'");
            }
        }

        return blocks;
    }
}
