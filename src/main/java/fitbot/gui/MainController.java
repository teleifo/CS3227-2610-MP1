package fitbot.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import fitbot.command.CommandResult;
import fitbot.exception.FitBotException;
import fitbot.model.Workout;
import fitbot.model.WorkoutType;
import fitbot.parser.Parser;
import fitbot.service.WorkoutService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Coordinates the FXML main view and its child controls.
 */
public class MainController {
    @FXML private HBox mainContent;
    @FXML private ComboBox<String> workoutFilter;
    @FXML private ComboBox<String> addWorkoutType;
    @FXML private StackPane workoutListContainer;
    @FXML private StackPane workoutDetailContainer;
    @FXML private StackPane commandPanelContainer;
    private WorkoutService service;
    private WorkoutListView workoutList;

    /** Connects the service to the loaded GUI controls. */
    public void setService(WorkoutService service) throws IOException {
        this.service = service;
        configureResponsiveColumns();
        workoutList = new WorkoutListView(service);
        workoutListContainer.getChildren().setAll(workoutList);
        workoutFilter.getItems().setAll("All", "Run", "Cycle", "Gym");
        workoutFilter.getSelectionModel().selectFirst();
        addWorkoutType.getItems().setAll("Run", "Cycle", "Gym");
        addWorkoutType.getSelectionModel().selectFirst();
        workoutFilter.valueProperty().addListener((observable, oldValue, selectedValue) -> {
            WorkoutType type = "All".equals(selectedValue)
                    ? null : WorkoutType.valueOf(selectedValue.toUpperCase(Locale.ROOT));
            workoutList.filterByType(type);
        });
        WorkoutDetailView detail = new WorkoutDetailView();
        workoutDetailContainer.getChildren().setAll(detail);
        detail.setDeleteAction(position -> deleteWorkout(position, detail));
        detail.setEditAction((position, workout) -> editWorkout(position, workout, detail));
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

    /** Placeholder for the add-workout form implemented in the next increment. */
    @FXML
    private void openAddWorkout() {
        WorkoutType type = WorkoutType.valueOf(addWorkoutType.getValue().toUpperCase(Locale.ROOT));
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-workout.fxml"));
            DialogPane pane = loader.load();
            AddWorkoutController form = loader.getController();
            form.setType(type);
            pane.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            pane.lookupButton(ButtonType.OK).getStyleClass().add("save-button");
            pane.lookupButton(ButtonType.CANCEL).getStyleClass().add("cancel-button");
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add Workout");
            dialog.setDialogPane(pane);
            pane.lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                try {
                    service.execute(Parser.parseCommand(form.getCommand(type)));
                    workoutList.refresh();
                } catch (FitBotException exception) {
                    event.consume();
                    form.setError(exception.getMessage());
                }
            });
            dialog.showAndWait();
        } catch (IOException exception) {
            // The dialog cannot be opened if its FXML resource is unavailable.
        }
    }

    /** Deletes a workout through the existing command service and refreshes the GUI. */
    private void deleteWorkout(int position, WorkoutDetailView detail) {
        Dialog<ButtonType> confirmation = new Dialog<>();
        confirmation.setTitle("Delete Workout");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("delete-workout.fxml"));
            DialogPane pane = loader.load();
            confirmation.setDialogPane(pane);
            loader.<DeleteWorkoutController>getController().setPosition(position);
        } catch (IOException exception) {
            return;
        }

        confirmation.getDialogPane().getStylesheets().add(
                getClass().getResource("styles.css").toExternalForm());
        confirmation.getDialogPane().lookupButton(ButtonType.YES).getStyleClass().add("delete-button");
        confirmation.getDialogPane().lookupButton(ButtonType.NO).getStyleClass().add("cancel-button");

        if (confirmation.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            CommandResult result = service.execute(Parser.parseCommand("delete " + position));
            if (result.wasDataModified()) {
                workoutList.refresh();
                detail.showEmptyState();
                workoutList.getSelectionModel().clearSelection();
            }
        } catch (FitBotException exception) {
            return;
        }
    }

    /** Loads the edit dialog from FXML and applies its validated changes. */
    private void editWorkout(int position, Workout workout, WorkoutDetailView detail) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-workout.fxml"));
            DialogPane pane = loader.load();

            EditWorkoutController edit = loader.getController();
            edit.setWorkout(workout);

            pane.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            pane.lookupButton(ButtonType.OK).getStyleClass().add("save-button");
            pane.lookupButton(ButtonType.CANCEL).getStyleClass().add("cancel-button");

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Workout");
            dialog.setDialogPane(pane);

            pane.lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    if (service.execute(Parser.parseCommand(edit.getCommand(position, workout))).wasDataModified()) {
                        workoutList.refresh();
                        workoutList.getSelectionModel().select(position - 1);
                    }
                } catch (FitBotException exception) {
                    event.consume();
                    edit.setError(exception.getMessage());
                }
            });
            dialog.showAndWait();
        } catch (IOException exception) {
            return;
        }
    }

    /** Keeps the two main columns proportional regardless of their content. */
    private void configureResponsiveColumns() {
        workoutListContainer.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.45));
        workoutDetailContainer.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.55));
        workoutListContainer.setMaxWidth(Double.MAX_VALUE);
        workoutDetailContainer.setMaxWidth(Double.MAX_VALUE);
    }
}
