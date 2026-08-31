package fitbot;

import javafx.application.Application;

/**
 * Starts the FitBot graphical user interface.
 */
public final class Launcher {
    /** Prevents instantiation of this entry-point class. */
    private Launcher() {
    }

    /** Starts JavaFX and opens the main application window. */
    public static void main(String[] args) {
        Application.launch(FitBotApplication.class, args);
    }
}
