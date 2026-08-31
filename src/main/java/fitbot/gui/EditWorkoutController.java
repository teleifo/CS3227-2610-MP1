package fitbot.gui;

import fitbot.model.CycleWorkout;
import fitbot.model.GymWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;
import fitbot.model.WorkoutBlock;
import fitbot.model.WorkoutSet;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controls the common fields of the edit-workout view.
 */
public class EditWorkoutController {
    @FXML private TextField dateField;
    @FXML private TextField durationField;
    @FXML private TextField distanceField;
    @FXML private TextField elevationField;
    @FXML private TextField maxField;
    @FXML private Label distanceLabel;
    @FXML private Label elevationLabel;
    @FXML private Label maxLabel;
    @FXML private TextArea errorArea;
    @FXML private VBox blocksBox;

    /** Populates the form with the selected workout's common fields. */
    public void setWorkout(Workout workout) {
        dateField.setText(workout.getDate().toString());
        durationField.setText(String.valueOf(workout.getDurationSeconds()));
        distanceLabel.setVisible(false);
        distanceLabel.setManaged(false);
        distanceField.setVisible(false);
        distanceField.setManaged(false);
        elevationLabel.setVisible(false);
        elevationLabel.setManaged(false);
        elevationField.setVisible(false);
        elevationField.setManaged(false);
        maxLabel.setVisible(false);
        maxLabel.setManaged(false);
        maxField.setVisible(false);
        maxField.setManaged(false);

        if (workout instanceof RunWorkout run) {
            show(distanceLabel, distanceField);
            distanceField.setText(String.valueOf(run.getDistanceKilometres()));
            show(elevationLabel, elevationField);
            elevationField.setText(value(run.getElevationGainMetres()));
        } else if (workout instanceof CycleWorkout cycle) {
            show(distanceLabel, distanceField);
            distanceField.setText(String.valueOf(cycle.getDistanceKilometres()));
            show(elevationLabel, elevationField);
            elevationField.setText(value(cycle.getElevationGainMetres()));
            show(maxLabel, maxField);
            maxField.setText(value(cycle.getMaxSpeedKilometresPerHour()));
        } else if (workout instanceof GymWorkout gym) {
            blocksBox.getChildren().clear();
            for (int index = 0; index < gym.getBlocks().size(); index++) {
                addBlockEditor(gym.getBlocks().get(index));
            }
            Button addBlock = new Button("+ Add Block");
            addBlock.setOnAction(event -> addBlockEditor(new WorkoutBlock("New Exercise",
                    java.util.List.of(new WorkoutSet(1, 1)))));
            blocksBox.getChildren().add(addBlock);
        }
    }

    /** Returns the edited date. */
    public String getDate() {
        return dateField.getText();
    }

    /** Returns the edited duration. */
    public String getDuration() {
        return durationField.getText();
    }

    /** Builds the edit command from the current form values. */
    public String getCommand(int position, Workout workout) {
        StringBuilder command = new StringBuilder("edit ").append(position)
                .append(" -date ").append(getDate()).append(" -duration ").append(getDuration());
        add(command, "-distance", distanceField);
        add(command, "-elevation", elevationField);
        add(command, "-max", maxField);
        if (workout instanceof GymWorkout) {
            command.append(" -blocks \"").append(getBlocksSpec()).append("\"");
        }
        return command.toString();
    }

    /** Serializes the current block and set controls for the edit command. */
    public String getBlocksSpec() {
        StringBuilder result = new StringBuilder();
        for (javafx.scene.Node node : blocksBox.getChildren()) {
            if (!(node instanceof VBox blockBox)) {
                continue;
            }

            TextField name = (TextField) blockBox.getProperties().get("name");
            VBox setsBox = (VBox) blockBox.getProperties().get("setsBox");

            if (!result.isEmpty()) {
                result.append(";");
            }
            result.append(name.getText()).append(":");

            for (int i = 0; i < setsBox.getChildren().size(); i++) {
                HBox row = (HBox) setsBox.getChildren().get(i);

                if (i > 0) {
                    result.append(",");
                }
                result.append(((TextField) row.getProperties().get("reps")).getText())
                        .append("@").append(((TextField) row.getProperties().get("weight")).getText());
            }
        }

        return result.toString();
    }

    /** Displays an inline validation error. */
    public void setError(String message) {
        errorArea.setText(message);
    }

    private void show(Label label, TextField field) {
        label.setVisible(true);
        label.setManaged(true);
        field.setVisible(true);
        field.setManaged(true);
    }

    private void addBlockEditor(WorkoutBlock block) {
        VBox blockBox = new VBox(5);
        TextField name = new TextField(block.getExerciseName());
        VBox setsBox = new VBox(4);
        blockBox.getProperties().put("name", name);
        blockBox.getProperties().put("setsBox", setsBox);
        HBox header = new HBox(6, name);
        Button removeBlock = new Button("-");

        removeBlock.setOnAction(event -> {
            if (blocksBox.getChildren().size() > 2) {
                blocksBox.getChildren().remove(blockBox);
            }
        });
        header.getChildren().add(removeBlock);
        blockBox.getChildren().addAll(header, setsBox);

        for (WorkoutSet set : block.getSets()) {
            addSetEditor(setsBox, set);
        }

        Button addSet = new Button("+ Add Set");
        addSet.setOnAction(event -> addSetEditor(setsBox, new WorkoutSet(1, 1)));
        blockBox.getChildren().add(addSet);
        int addBlockIndex = blocksBox.getChildren().isEmpty()
                || !(blocksBox.getChildren().get(blocksBox.getChildren().size() - 1) instanceof Button)
                ? blocksBox.getChildren().size()
                : blocksBox.getChildren().size() - 1;
        blocksBox.getChildren().add(addBlockIndex, blockBox);
    }

    private void addSetEditor(VBox setsBox, WorkoutSet set) {
        TextField reps = new TextField(String.valueOf(set.getRepetitions()));
        TextField weight = new TextField(String.valueOf(set.getWeightKilograms()));
        reps.setPrefWidth(70);
        weight.setPrefWidth(70);

        Button removeSet = new Button("-");
        Label repsUnit = new Label("reps");
        Label weightUnit = new Label("kg");
        repsUnit.setStyle("-fx-text-fill: #ffffff;");
        weightUnit.setStyle("-fx-text-fill: #ffffff;");
        HBox row = new HBox(5, reps, repsUnit, weight, weightUnit, removeSet);

        row.setAlignment(Pos.CENTER_LEFT);
        row.getProperties().put("reps", reps);
        row.getProperties().put("weight", weight);

        removeSet.setOnAction(event -> {
            if (setsBox.getChildren().size() > 1) {
                setsBox.getChildren().remove(row);
            }
        });
        setsBox.getChildren().add(row);
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private void add(StringBuilder command, String option, TextField field) {
        if (field.isManaged()) {
            command.append(" ").append(option).append(" ").append(field.getText().isBlank()
                    ? "null" : field.getText());
        }
    }
}
