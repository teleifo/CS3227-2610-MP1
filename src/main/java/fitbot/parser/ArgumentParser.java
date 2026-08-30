package fitbot.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fitbot.exception.FitBotException;

/**
 * Parses and validates option values used by commands.
 */
public final class ArgumentParser {
    /** Converts alternating option names and values into a map. */
    public static Map<String, String> parseOptions(List<String> arguments) throws FitBotException {
        Map<String, String> options = new LinkedHashMap<>();
        int index = 0;
        while (index < arguments.size()) {
            String option = arguments.get(index);
            if (!option.startsWith("-")) {
                throw new FitBotException(
                        "Invalid option '" + option + "'. Options must start with '-'.");
            }
            if (options.containsKey(option)) {
                throw new FitBotException("Duplicate option: " + option + ".");
            }
            if (index + 1 >= arguments.size()) {
                options.put(option, null);
                index++;
                continue;
            }

            String value = arguments.get(index + 1);
            if (isOptionToken(value)) {
                options.put(option, null);
                index++;
                continue;
            }
            options.put(option, value);
            index += 2;
        }

        return options;
    }

    /** Determines whether a token is an option rather than a negative number. */
    private static boolean isOptionToken(String token) {
        return token.startsWith("-") && !token.matches("-\\d+(\\.\\d+)?");
    }

    /** Returns a required option value. */
    public static String getRequiredOption(Map<String, String> options, String name)
            throws FitBotException {
        String value = options.get(name);
        if (value == null) {
            throw new FitBotException("Missing required option: " + name);
        }

        return value;
    }

    /** Parses a whole-number option. */
    public static long parseLong(String value, String name) throws FitBotException {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new FitBotException("Invalid " + name + ".");
        }
    }

    /** Parses a decimal-number option. */
    public static double parseDouble(String value, String name) throws FitBotException {
        try {
            double number = Double.parseDouble(value);
            if (!Double.isFinite(number)) {
                throw new FitBotException(
                        "Invalid " + name + ": must be a finite number.");
            }
            return number;
        } catch (NumberFormatException exception) {
            throw new FitBotException("Invalid " + name + ".");
        }
    }

    /** Parses a date in ISO format, such as 2026-08-28. */
    public static LocalDate parseDate(String value) throws FitBotException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new FitBotException("Invalid date. Use YYYY-MM-DD.");
        }
    }
}
