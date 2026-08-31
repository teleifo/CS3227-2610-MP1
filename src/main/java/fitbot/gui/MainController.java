package fitbot.gui;

import java.io.IOException;
import java.util.Collections;

import fitbot.service.WorkoutService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

/**
 * Coordinates the FXML main view and its child controls.
 */
public class MainController {
    @FXML private StackPane workoutListContainer;
    @FXML private StackPane commandPanelContainer;
    private WorkoutListView workoutList;

    /** Connects the service to the loaded GUI controls. */
    public void setService(WorkoutService service) throws IOException {
        workoutList = new WorkoutListView(service);
        workoutListContainer.getChildren().setAll(workoutList);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("command-panel.fxml"));
        commandPanelContainer.getChildren().setAll(Collections.singleton(loader.load()));
        CommandPanelController controller = loader.getController();
        controller.setService(service);
        controller.setRefreshAction(workoutList::refresh);
    }
}
