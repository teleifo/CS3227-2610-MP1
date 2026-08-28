package fitbot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import fitbot.command.CommandResult;
import fitbot.command.ParsedCommand;
import fitbot.exception.FitBotException;
import fitbot.model.Workout;
import fitbot.parser.Parser;

/**
 * Entry point for the FitBot fitness tracker application.
 */
public class FitBot {
    /** ASCII banner displayed on application launch. **/
    private static final String BANNER = """
             _   _      _ _
            | | | | ___| | | ___
            | |_| |/ _ \\ | |/ _ \\
            |  _  |  __/ | | (_) |
            |_| |_|\\___|_|_|\\___/
            """;

    /** Text displayed between user input and FitBot's responses. */
    private static final String SEPARATOR = "============================================================";

    /** Workouts logged during this application run. */
    private final List<Workout> workouts = new ArrayList<>();

    /**
     * Displays FitBot's startup banner, including the current day of the week.
     */
    public void showGreeting() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayEnum = today.getDayOfWeek();
        String dayText = dayEnum.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        System.out.println(BANNER);
        System.out.println(SEPARATOR);
        System.out.printf("Happy %s! I am FitBot, your personal fitness tracker.\n", dayText);
        System.out.println("Ready to get active?");
        System.out.println(SEPARATOR);
    }

    /**
     * Starts the command-line interaction loop and executes recognised commands
     * until the input ends or a command requests that the application exit.
     */
    public void start() {
        Scanner sc = new Scanner(System.in);

        showGreeting();
        while (sc.hasNextLine()) {
            String input = sc.nextLine().trim();
            System.out.println(SEPARATOR);

            try {
                ParsedCommand parsedCommand = Parser.parseCommand(input);
                CommandResult result = parsedCommand.getCommand()
                        .execute(parsedCommand.getArguments(), workouts);
                System.out.println(result.getMessage());
                System.out.println(SEPARATOR);

                if (result.shouldExit()) {
                    break;
                }
            } catch (FitBotException exception) {
                System.out.println(exception.getMessage());
                System.out.println(SEPARATOR);
            }
        }

        sc.close();
    }
}
