package fitbot.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import fitbot.command.CommandResult;
import fitbot.exception.FitBotException;
import fitbot.model.WorkoutType;
import fitbot.parser.Parser;
import fitbot.service.WorkoutService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Coordinates the FXML main view and its child controls.
 */
public class MainController {
    @FXML private HBox mainContent;
    @FXML private ComboBox<String> workoutFilter;
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

    /** Deletes a workout through the existing command service and refreshes the GUI. */
    private void deleteWorkout(int position, WorkoutDetailView detail) {
        Dialog<ButtonType> confirmation = new Dialog<>();
        confirmation.setTitle("Delete Workout");
        confirmation.setHeaderText(null);
        confirmation.getDialogPane().setStyle("-fx-background-color: #2f3640;");

        VBox content = new VBox(10);
        content.setPadding(new Insets(8));

        Label title = new Label("Delete workout " + position + "?");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label message = new Label("This action cannot be undone.");
        message.setStyle("-fx-text-fill: #dfe4ea; -fx-font-size: 13px;");

        content.getChildren().addAll(title, message);
        confirmation.getDialogPane().setContent(content);
        confirmation.getDialogPane().getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirmation.getDialogPane().lookupButton(ButtonType.YES).setStyle(
                "-fx-background-color: #e84118; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        confirmation.getDialogPane().lookupButton(ButtonType.NO).setStyle(
                "-fx-background-color: #487eb0; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

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
            // The selected item came from the service list, so this should not occur.
        }
    }

    /** Opens a small type-aware form and applies edits through the existing command. */
    private void editWorkout(int position, fitbot.model.Workout workout, WorkoutDetailView detail) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Workout");
        dialog.getDialogPane().setStyle("-fx-background-color: #2f3640;");

        javafx.scene.layout.GridPane form = new javafx.scene.layout.GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(12));
        form.setPrefWidth(360);
        form.setStyle("-fx-background-color: #2f3640;");

        TextField date = field(workout.getDate().toString());
        TextField duration = field(String.valueOf(workout.getDurationSeconds()));
        form.addRow(0, dialogLabel("Date"), date);
        form.addRow(1, dialogLabel("Duration (seconds)"), duration);

        List<String> options = new ArrayList<>();
        List<TextField> fields = new ArrayList<>();
        if (workout instanceof fitbot.model.RunWorkout run) {
            addField(form, options, fields, 2, "Distance (km)", "-distance",
                    String.valueOf(run.getDistanceKilometres()));
            addField(form, options, fields, 3, "Elevation Gain (m)", "-elevation",
                    value(run.getElevationGainMetres()));
        } else if (workout instanceof fitbot.model.CycleWorkout cycle) {
            addField(form, options, fields, 2, "Distance (km)", "-distance",
                    String.valueOf(cycle.getDistanceKilometres()));
            addField(form, options, fields, 3, "Elevation Gain (m)", "-elevation",
                    value(cycle.getElevationGainMetres()));
            addField(form, options, fields, 4, "Maximum Speed", "-max",
                    value(cycle.getMaxSpeedKilometresPerHour()));
        }

        TextArea errorBanner = new TextArea();
        errorBanner.setEditable(false);
        errorBanner.setWrapText(true);
        errorBanner.setPrefRowCount(2);
        errorBanner.setPrefWidth(360);
        errorBanner.setMaxWidth(Double.MAX_VALUE);
        errorBanner.setFocusTraversable(false);
        errorBanner.setStyle("-fx-control-inner-background: transparent; -fx-background-color: transparent;"
                + " -fx-text-fill: #e84118; -fx-border-color: transparent; -fx-font-weight: bold;");

        VBox dialogContent = new VBox(8, errorBanner, form);
        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #487eb0; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #718093; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {
                    StringBuilder command = new StringBuilder("edit ").append(position)
                            .append(" -date ").append(date.getText())
                            .append(" -duration ").append(duration.getText());
                    for (int i = 0; i < fields.size(); i++) {
                        command.append(" ").append(options.get(i)).append(" ")
                                .append(fields.get(i).getText().isBlank() ? "null" : fields.get(i).getText());
                    }

                    try {
                        if (service.execute(Parser.parseCommand(command.toString())).wasDataModified()) {
                            workoutList.refresh();
                            workoutList.getSelectionModel().select(position - 1);
                        }
                    } catch (FitBotException exception) {
                        event.consume();
                        errorBanner.setText(exception.getMessage());
                    }
                });

        dialog.showAndWait();
    }

    private TextField field(String value) {
        return new TextField(value);
    }

    private void addField(GridPane form, List<String> options, List<TextField> fields,
            int row, String label, String option, String value) {
        TextField field = field(value);
        form.addRow(row, dialogLabel(label), field);
        options.add(option);
        fields.add(field);
    }

    private Label dialogLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #ffffff;");
        return label;
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }

    /** Keeps the two main columns proportional regardless of their content. */
    private void configureResponsiveColumns() {
        workoutListContainer.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.45));
        workoutDetailContainer.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.55));
        workoutListContainer.setMaxWidth(Double.MAX_VALUE);
        workoutDetailContainer.setMaxWidth(Double.MAX_VALUE);
    }
}
