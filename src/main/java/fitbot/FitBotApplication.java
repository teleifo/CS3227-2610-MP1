package fitbot;

import fitbot.exception.FitBotException;
import fitbot.gui.CommandPanel;
import fitbot.gui.WorkoutListView;
import fitbot.service.WorkoutService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Minimal JavaFX application shell for FitBot.
 *
 * <p>Later increments will replace the placeholder content with the workout
 * list and workout forms.</p>
 */
public class FitBotApplication extends Application {
    /** Default window width in pixels. */
    private static final double WINDOW_WIDTH = 900;

    /** Default window height in pixels. */
    private static final double WINDOW_HEIGHT = 600;

    @Override
    public void start(Stage stage) {
        WorkoutService service;
        try {
            service = new WorkoutService();
        } catch (FitBotException exception) {
            showStartupError(stage, exception);
            return;
        }

        BorderPane root = new BorderPane();
        WorkoutListView workoutList = new WorkoutListView(service);
        root.setCenter(workoutList);
        root.setBottom(new CommandPanel(service, workoutList::refresh));

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle("FitBot");
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        stage.setScene(scene);
        stage.show();
    }

    /** Shows a startup error when saved workout data cannot be loaded. */
    private void showStartupError(Stage stage, FitBotException exception) {
        BorderPane root = new BorderPane();
        root.setCenter(new Label("Could not start FitBot: " + exception.getMessage()));
        stage.setTitle("FitBot");
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }
}
