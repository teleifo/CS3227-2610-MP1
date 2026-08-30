package fitbot.command;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fitbot.exception.FitBotException;
import fitbot.model.CycleWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;
import fitbot.parser.ArgumentParser;

/**
 * Command that changes only the workout options supplied by the user.
 */
public class EditWorkoutCommand extends Command {
    /** Creates the edit command. */
    public EditWorkoutCommand() {
        super("edit", "Edit a workout by its list number.",
                "edit <workout number> [-date <YYYY-MM-DD>] [-duration <seconds>]\n"
                        + "[-distance <kilometres>] [-elevation <metres>] [-max <km/hr>]",
                "edit 2 -date 2026-09-01 -distance 15");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts)
            throws FitBotException {
        if (arguments.size() < 2) {
            throw new FitBotException("Usage: " + getUsage() + "\nExample: " + getExample());
        }

        int position;
        try {
            position = Integer.parseInt(arguments.get(0));
        } catch (NumberFormatException exception) {
            throw new FitBotException("Workout number must be an integer.");
        }
        if (position < 1 || position > workouts.size()) {
            throw new FitBotException("Invalid workout number.");
        }

        Map<String, String> options = ArgumentParser.parseOptions(arguments.subList(1, arguments.size()));
        Workout workout = workouts.get(position - 1);

        Set<String> supportedOptions;
        switch (workout.getType()) {
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
        for (Map.Entry<String, String> option : options.entrySet()) {
            if (option.getValue() == null) {
                throw new FitBotException("Missing value for " + option.getKey() + ".");
            }
        }
        if (options.containsKey("-type")) {
            throw new FitBotException("Workout type cannot be edited.");
        }

        List<String> changes = new ArrayList<>();
        LocalDate oldDate = workout.getDate();
        long oldDuration = workout.getDurationSeconds();

        if (options.containsKey("-date")) {
            LocalDate value = ArgumentParser.parseDate(options.get("-date"));
            workout.setDate(value);
            if (!oldDate.equals(value)) {
                changes.add("date: " + oldDate + " -> " + value);
            }
        }
        if (options.containsKey("-duration")) {
            long value = ArgumentParser.parseLong(options.get("-duration"), "duration");
            workout.setDurationSeconds(value);
            if (oldDuration != value) {
                changes.add("duration: " + oldDuration + " -> " + value);
            }
        }

        if (workout instanceof RunWorkout run) {
            editRun(run, options, changes);
        } else if (workout instanceof CycleWorkout cycle) {
            editCycle(cycle, options, changes);
        }

        if (changes.isEmpty()) {
            return new CommandResult("No changes made.", false);
        }
        return new CommandResult("Workout updated: " + String.join(", ", changes), false, true);
    }

    private static void editRun(RunWorkout run, Map<String, String> options, List<String> changes)
            throws FitBotException {
        if (options.containsKey("-distance")) {
            double old = run.getDistanceKilometres();
            double value = ArgumentParser.parseDouble(options.get("-distance"), "distance");
            run.setDistanceKilometres(value);
            if (old != value) {
                changes.add("distance: " + old + " -> " + value);
            }
        }
        if (options.containsKey("-elevation")) {
            Double old = run.getElevationGainMetres();
            Double value = ArgumentParser.parseDouble(options.get("-elevation"), "elevation");
            run.setElevationGainMetres(value);
            if (!java.util.Objects.equals(old, value)) {
                changes.add("elevation: " + old + " -> " + value);
            }
        }
    }

    private static void editCycle(CycleWorkout cycle, Map<String, String> options, List<String> changes)
            throws FitBotException {
        if (options.containsKey("-distance")) {
            double old = cycle.getDistanceKilometres();
            double value = ArgumentParser.parseDouble(options.get("-distance"), "distance");
            cycle.setDistanceKilometres(value);
            if (old != value) {
                changes.add("distance: " + old + " -> " + value);
            }
        }
        if (options.containsKey("-elevation")) {
            Double old = cycle.getElevationGainMetres();
            Double value = ArgumentParser.parseDouble(options.get("-elevation"), "elevation");
            cycle.setElevationGainMetres(value);
            if (!java.util.Objects.equals(old, value)) {
                changes.add("elevation: " + old + " -> " + value);
            }
        }
        if (options.containsKey("-max")) {
            Double old = cycle.getMaxSpeedKilometresPerHour();
            Double value = ArgumentParser.parseDouble(options.get("-max"), "max speed");
            cycle.setMaxSpeedKilometresPerHour(value);
            if (!java.util.Objects.equals(old, value)) {
                changes.add("max speed: " + old + " -> " + value);
            }
        }
    }
}
