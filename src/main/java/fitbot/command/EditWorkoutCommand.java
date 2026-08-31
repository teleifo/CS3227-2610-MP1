package fitbot.command;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import fitbot.exception.FitBotException;
import fitbot.model.CycleWorkout;
import fitbot.model.GymWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;
import fitbot.model.WorkoutBlock;
import fitbot.parser.ArgumentParser;

/**
 * Command that changes only the workout options supplied by the user.
 */
public class EditWorkoutCommand extends Command {
    /** Creates the edit command. */
    public EditWorkoutCommand() {
        super("edit", "Edit a workout by its list number.",
                """
                        (Run) edit <workout number> [-date <YYYY-MM-DD>] [-duration <seconds>]
                        [-distance <kilometres>] [-elevation <metres>]
                        (Cycle) edit <workout number> [-date <YYYY-MM-DD>] [-duration <seconds>]
                        [-distance <kilometres>] [-elevation <metres>] [-max <km/hr>]
                        (Gym) edit <workout number> [-date <YYYY-MM-DD>] [-duration <seconds>]
                        [-blocks "<exercise>:<set-entry>,..."] OR
                        [-block <number> [-name <exercise>] [-sets <set-entry>,...]] OR
                        [-block <number> [-set <number> [-reps <number>] [-weight <kg>]]]""",
                """
                        edit 2 -date 2026-09-01 -distance 15
                        edit 3 -block 1 -set 2 -weight 65""");
    }

    @Override
    public CommandResult execute(List<String> arguments, List<Workout> workouts)
            throws FitBotException {
        if (arguments.isEmpty()) {
            throw new FitBotException("Usage: " + getUsage() + "\n\nExample: " + getExample());
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

        Workout workout = workouts.get(position - 1);

        Set<String> supportedOptions;
        switch (workout.getType()) {
        case RUN:
            supportedOptions = RunWorkout.getSupportedOptions();
            break;

        case CYCLE:
            supportedOptions = CycleWorkout.getSupportedOptions();
            break;

        case GYM:
            supportedOptions = GymWorkout.getSupportedEditOptions();
            break;

        default:
            throw new FitBotException("Unsupported workout type.");
        }

        List<String> optionArguments = arguments.subList(1, arguments.size());
        ArgumentParser.validateOptionNames(optionArguments, supportedOptions);
        Map<String, String> options = ArgumentParser.parseOptions(optionArguments);
        for (Map.Entry<String, String> option : options.entrySet()) {
            if (option.getValue() == null) {
                throw new FitBotException("Missing value for " + option.getKey() + ".");
            }
        }

        if (options.containsKey("-type")) {
            throw new FitBotException("Workout type cannot be edited.");
        }

        validateOptions(workout, options);

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
        } else if (workout instanceof GymWorkout gym) {
            editGym(gym, options, changes);
        }

        if (changes.isEmpty()) {
            return new CommandResult("No changes made.", false);
        }
        return new CommandResult("Workout updated: " + String.join(", ", changes), false, true);
    }

    /** Validates every requested edit before the command mutates the workout. */
    private static void validateOptions(Workout workout, Map<String, String> options)
            throws FitBotException {
        if (options.containsKey("-date")) {
            ArgumentParser.parseDate(options.get("-date"));
        }
        if (options.containsKey("-duration")) {
            long duration = ArgumentParser.parseLong(options.get("-duration"), "duration");
            if (duration <= 0) {
                throw new FitBotException("Invalid duration: must be positive.");
            }
        }

        if (workout instanceof RunWorkout || workout instanceof CycleWorkout) {
            if (options.containsKey("-distance")) {
                double distance = ArgumentParser.parseDouble(options.get("-distance"), "distance");
                if (distance <= 0) {
                    throw new FitBotException("Invalid distance: must be positive.");
                }
            }
            if (options.containsKey("-elevation")) {
                double elevation = ArgumentParser.parseDouble(options.get("-elevation"), "elevation");
                if (elevation < 0) {
                    throw new FitBotException("Invalid elevation: cannot be negative.");
                }
            }
        }

        if (workout instanceof CycleWorkout && options.containsKey("-max")) {
            double max = ArgumentParser.parseDouble(options.get("-max"), "max speed");
            if (max <= 0) {
                throw new FitBotException("Invalid max speed: must be positive.");
            }
        }

        if (workout instanceof GymWorkout gym) {
            if (options.containsKey("-blocks")) {
                LogWorkoutCommand.parseBlocks(options.get("-blocks"));
                if (options.keySet().stream().anyMatch(option ->
                        Set.of("-block", "-name", "-sets", "-set", "-reps", "-weight").contains(option))) {
                    throw new FitBotException("Use either -blocks or targeted set editing, not both.");
                }
            }

            if (options.containsKey("-name") && options.get("-name").isBlank()) {
                throw new FitBotException("Exercise name must not be blank.");
            }

            boolean targeted = options.keySet().stream().anyMatch(option ->
                    Set.of("-block", "-name", "-sets", "-set", "-reps", "-weight").contains(option));
            if (targeted) {
                if (!options.containsKey("-block")) {
                    throw new FitBotException("-block is required for targeted gym edits.");
                }

                int block = parsePositiveIndex(options.get("-block"), "block");
                if (block > gym.getBlocks().size()) {
                    throw new FitBotException("Invalid block number.");
                }

                if (options.containsKey("-sets")) {
                    if (options.keySet().stream().anyMatch(option ->
                            Set.of("-set", "-reps", "-weight").contains(option))) {
                        throw new FitBotException("Use either -sets or individual set editing, not both.");
                    }
                    LogWorkoutCommand.parseBlocks("temporary:" + options.get("-sets"));
                }

                boolean setEdit = options.containsKey("-set") || options.containsKey("-reps")
                        || options.containsKey("-weight");
                if (setEdit) {
                    if (!options.containsKey("-set")) {
                        throw new FitBotException("-set is required when editing repetitions or weight.");
                    }

                    int set = parsePositiveIndex(options.get("-set"), "set");
                    if (set > gym.getBlocks().get(block - 1).getSets().size()) {
                        throw new FitBotException("Invalid set number.");
                    }

                    if (options.containsKey("-reps")) {
                        parsePositiveIndex(options.get("-reps"), "repetitions");
                    }

                    if (options.containsKey("-weight")) {
                        double weight = ArgumentParser.parseDouble(options.get("-weight"), "weight");
                        if (weight <= 0) {
                            throw new FitBotException("Invalid weight: must be positive.");
                        }
                    }
                }
            }
        }
    }

    /** Applies the running-specific options supplied by the user. */
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
            if (!Objects.equals(old, value)) {
                changes.add("elevation: " + old + " -> " + value);
            }
        }
    }

    /** Applies the cycling-specific options supplied by the user. */
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

    /** Applies full-block replacement or targeted set-weight edits to a gym workout. */
    private static void editGym(GymWorkout gym, Map<String, String> options, List<String> changes)
            throws FitBotException {
        if (options.containsKey("-blocks")) {
            if (options.keySet().stream().anyMatch(
                    option -> Set.of("-block", "-name", "-set", "-reps", "-weight").contains(option))) {
                throw new FitBotException("Use either -blocks or targeted set editing, not both.");
            }

            gym.replaceBlocks(LogWorkoutCommand.parseBlocks(options.get("-blocks")));

            changes.add("blocks replaced");
        } else if (options.keySet().stream().anyMatch(
                    option -> Set.of("-block", "-name", "-set", "-reps", "-weight").contains(option))) {
            if (!options.containsKey("-block")) {
                throw new FitBotException("-block is required for targeted gym edits.");
            }

            int blockNumber = parsePositiveIndex(options.get("-block"), "block");
            if (blockNumber > gym.getBlocks().size()) {
                throw new FitBotException("Invalid block number.");
            }

            WorkoutBlock block = gym.getBlocks().get(blockNumber - 1);
            if (options.containsKey("-sets")) {
                List<WorkoutBlock> parsed = LogWorkoutCommand.parseBlocks(
                        "temporary:" + options.get("-sets"));
                block.setSets(parsed.get(0).getSets());
                changes.add("sets replaced");
            }
            if (options.containsKey("-name")) {
                String name = options.get("-name");
                if (name.isBlank()) {
                    throw new FitBotException("Exercise name must not be blank.");
                }
                block.setExerciseName(name);
                changes.add("name changed to " + block.getExerciseName());
            }

            boolean setEdit = options.containsKey("-set") || options.containsKey("-reps")
                    || options.containsKey("-weight");
            if (setEdit) {
                if (!options.containsKey("-set")) {
                    throw new FitBotException("-set is required when editing repetitions or weight.");
                }

                int setNumber = parsePositiveIndex(options.get("-set"), "set");
                if (setNumber > block.getSets().size()) {
                    throw new FitBotException("Invalid set number.");
                }

                if (options.containsKey("-reps")) {
                    int old = block.getSets().get(setNumber - 1).getRepetitions();
                    int reps = parsePositiveIndex(options.get("-reps"), "repetitions");
                    block.getSets().get(setNumber - 1).setRepetitions(reps);
                    changes.add("repetitions: " + old + " -> " + reps);
                }
                if (options.containsKey("-weight")) {
                    double old = block.getSets().get(setNumber - 1).getWeightKilograms();
                    double weight = ArgumentParser.parseDouble(options.get("-weight"), "weight");
                    if (weight <= 0) {
                        throw new FitBotException("Invalid weight: must be positive.");
                    }

                    block.getSets().get(setNumber - 1).setWeightKilograms(weight);
                    changes.add("weight: " + old + " -> " + weight);
                }
            }
        }
    }

    private static int parsePositiveIndex(String value, String name) throws FitBotException {
        long number = ArgumentParser.parseLong(value, name);
        if (number < 1 || number > Integer.MAX_VALUE) {
            throw new FitBotException("Invalid " + name + ": must be positive.");
        }

        return (int) number;
    }
}
