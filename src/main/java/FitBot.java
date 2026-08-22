import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Scanner;

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

    public void start() {
        Scanner sc = new Scanner(System.in);

        showGreeting();
        while (sc.hasNextLine()) {
            String command = sc.nextLine();
            System.out.println(SEPARATOR);

            if (command.equals("bye")) {
                System.out.println("See you soon! Until then, stay fit!");
                System.out.println(SEPARATOR);
                break;
            } else {
                System.out.println("FitBot says: " + command);
                System.out.println(SEPARATOR);
            }
        }

        sc.close();
    }
}
