import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

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

    public String getDayOfWeek() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayEnum = today.getDayOfWeek();
        return dayEnum.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public void start() {
        System.out.println(BANNER);
        System.out.println(SEPARATOR);
        System.out.printf("Happy %s! I am FitBot, your personal fitness tracker.\n", getDayOfWeek());
        System.out.println("Ready to get active?");
        System.out.println(SEPARATOR);
    }
}
