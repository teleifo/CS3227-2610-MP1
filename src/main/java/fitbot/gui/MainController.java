package fitbot.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import fitbot.model.WorkoutType;
import fitbot.service.WorkoutService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Coordinates the FXML main view and its child controls.
 */
public class MainController {
    @FXML private HBox mainContent;
    @FXML private ComboBox<String> workoutFilter;
    @FXML private StackPane workoutListContainer;
    @FXML private StackPane workoutDetailContainer;
    @FXML private StackPane commandPanelContainer;
    private WorkoutListView workoutList;

    /** Connects the service to the loaded GUI controls. */
    public void setService(WorkoutService service) throws IOException {
        configureResponsiveColumns();
        workoutList = new WorkoutListView(service);
        workoutListContainer.getChildren().setAll(workoutList);
        workoutFilter.getItems().setAll("All", "Run", "Cycle", "Gym");
        workoutFilter.getSelectionModel().selectFirst();
        workoutFilter.valueProperty().addListener((observable, oldValue, selectedValue) -> {
            WorkoutType type = "All".equals(selectedValue)
                    ? null : WorkoutType.valueOf(selectedValue.toUpperCase(Locale.ROOT));
            workoutList.filterByType(type);
        });
        WorkoutDetailView detail = new WorkoutDetailView();
        workoutDetailContainer.getChildren().setAll(detail);
        workoutList.getSelectionModel().selectedItemProperty().addListener((
                observable, oldWorkout, selectedWorkout) -> {
                    if (selectedWorkout == null) {
                        detail.showEmptyState();
                    } else {
                        int position = workoutList.getOriginalPosition(selectedWorkout);
                        detail.showWorkout(position, selectedWorkout);
                    }
                });

        FXMLLoader loader = new FXMLLoader(getClass().getResource("command-panel.fxml"));
        commandPanelContainer.getChildren().setAll(Collections.singleton(loader.load()));
        CommandPanelController controller = loader.getController();
        controller.setService(service);
        controller.setRefreshAction(workoutList::refresh);
    }

    /** Keeps the two main columns proportional regardless of their content. */
    private void configureResponsiveColumns() {
        workoutListContainer.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.45));
        workoutDetailContainer.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.55));
        workoutListContainer.setMaxWidth(Double.MAX_VALUE);
        workoutDetailContainer.setMaxWidth(Double.MAX_VALUE);
    }
}
