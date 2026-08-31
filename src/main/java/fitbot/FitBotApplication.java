package fitbot;

import java.io.IOException;

import fitbot.exception.FitBotException;
import fitbot.gui.MainController;
import fitbot.service.WorkoutService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Minimal JavaFX application shell for FitBot.
 *
 * <p>The layout is loaded from FXML while application logic remains in Java
 * controllers and the shared service.</p>
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

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fitbot/gui/main-view.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setService(service);

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene.getStylesheets().add(
                    getClass().getResource("/fitbot/gui/styles.css").toExternalForm());
            scene.getStylesheets().add(
                    getClass().getResource("/fitbot/gui/styles.css").toExternalForm()
            );
            stage.setTitle("FitBot");
            stage.setMinWidth(500);
            stage.setMinHeight(400);
            stage.setScene(scene);
            stage.show();
        } catch (IOException exception) {
            showStartupError(stage, new FitBotException("Could not load GUI: "
                    + exception.getMessage()));
        }
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
