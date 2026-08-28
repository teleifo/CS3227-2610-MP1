package fitbot.command;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import fitbot.exception.FitBotException;
import fitbot.model.CycleWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;
import fitbot.model.WorkoutType;
import fitbot.parser.ArgumentParser;

/**
 * Command that logs a workout.
 */
public class LogWorkoutCommand extends Command {
    /** Creates the log command. */
    public LogWorkoutCommand() {
        super("log", "Log a workout.");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts)
            throws FitBotException {
        Map<String, String> options = ArgumentParser.parseOptions(arguments);

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
        double distance = ArgumentParser.parseDouble(
                ArgumentParser.getRequiredOption(options, "-distance"), "distance");
        Double elevation = options.containsKey("-elevation")
                ? ArgumentParser.parseDouble(options.get("-elevation"), "elevation") : null;

        if (duration <= 0 || distance <= 0) {
            throw new FitBotException("Duration and distance must be positive.");
        }
        if (elevation != null && elevation < 0) {
            throw new FitBotException("Elevation cannot be negative.");
        }

        Workout workout;
        switch (type) {
        case RUN:
            workout = new RunWorkout(date, duration, distance, elevation);
            break;

        case CYCLE:
            workout = new CycleWorkout(date, duration, distance, elevation);
            break;

        default:
            throw new FitBotException("Unsupported workout type.");
        }
        workouts.add(workout);

        return new CommandResult("Workout logged successfully.", false);
    }
}
