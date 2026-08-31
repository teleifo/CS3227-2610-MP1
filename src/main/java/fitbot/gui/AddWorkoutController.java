package fitbot.gui;

import java.time.LocalDate;

import fitbot.model.WorkoutBlock;
import fitbot.model.WorkoutSet;
import fitbot.model.WorkoutType;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controls the Run and Cycle add-workout form.
 */
public class AddWorkoutController {
    @FXML private TextField date;
    @FXML private TextField duration;
    @FXML private TextField distance;
    @FXML private TextField elevation;
    @FXML private TextField max;
    @FXML private Label maxLabel;
    @FXML private Label distanceLabel;
    @FXML private Label elevationLabel;
    @FXML private TextArea errorArea;
    @FXML private VBox blocksBox;

    /** Configures the form for the selected workout type. */
    public void setType(WorkoutType type) {
        date.setText(LocalDate.now().toString());
        boolean distanceVisible = type != WorkoutType.GYM;
        distance.setVisible(distanceVisible);
        distance.setManaged(distanceVisible);
        distanceLabel.setVisible(distanceVisible);
        distanceLabel.setManaged(distanceVisible);
        elevation.setVisible(distanceVisible);
        elevation.setManaged(distanceVisible);
        elevationLabel.setVisible(distanceVisible);
        elevationLabel.setManaged(distanceVisible);
        max.setVisible(type == WorkoutType.CYCLE);
        max.setManaged(type == WorkoutType.CYCLE);
        maxLabel.setVisible(type == WorkoutType.CYCLE);
        maxLabel.setManaged(type == WorkoutType.CYCLE);
        blocksBox.setManaged(type == WorkoutType.GYM);
        blocksBox.setVisible(type == WorkoutType.GYM);

        if (type == WorkoutType.GYM) {
            blocksBox.getChildren().clear();
            addBlockEditor(new WorkoutBlock("New Exercise", java.util.List.of(new WorkoutSet(1, 1))));
            Button addBlock = new Button("+ Add Block");
            addBlock.setOnAction(event -> addBlockEditor(new WorkoutBlock("New Exercise",
                    java.util.List.of(new WorkoutSet(1, 1)))));
            blocksBox.getChildren().add(addBlock);
        }
    }

    /** Builds a log command from the form values. */
    public String getCommand(WorkoutType type) {
        StringBuilder command = new StringBuilder("log -type ").append(type.toString().toLowerCase())
                .append(" -date ").append(date.getText()).append(" -duration ").append(duration.getText());
        if (type != WorkoutType.GYM) {
            command.append(" -distance ").append(distance.getText());
        }
        if (!elevation.getText().isBlank()) {
            command.append(" -elevation ").append(elevation.getText());
        }
        if (type == WorkoutType.CYCLE && !max.getText().isBlank()) {
            command.append(" -max ").append(max.getText());
        }
        if (type == WorkoutType.GYM) {
            command.append(" -blocks \"").append(getBlocksSpec()).append("\"");
        }
        return command.toString();
    }

    private String getBlocksSpec() {
        StringBuilder result = new StringBuilder();
        for (javafx.scene.Node node : blocksBox.getChildren()) {
            if (!(node instanceof VBox block)) {
                continue;
            }

            TextField name = (TextField) block.getProperties().get("name");
            VBox sets = (VBox) block.getProperties().get("sets");
            if (!result.isEmpty()) {
                result.append(";");
            }
            result.append(name.getText()).append(":");

            for (int i = 0; i < sets.getChildren().size(); i++) {
                HBox row = (HBox) sets.getChildren().get(i);
                if (i > 0) {
                    result.append(",");
                }

                result.append(((TextField) row.getProperties().get("reps")).getText())
                        .append("@").append(((TextField) row.getProperties().get("weight")).getText());
            }
        }

        return result.toString();
    }

    private void addBlockEditor(WorkoutBlock block) {
        VBox box = new VBox(5);
        TextField name = new TextField(block.getExerciseName());
        VBox sets = new VBox(4);
        box.getProperties().put("name", name);
        box.getProperties().put("sets", sets);
        HBox header = new HBox(6, name);

        Button remove = new Button("-");
        remove.setOnAction(event -> {
            if (blocksBox.getChildren().size() > 2) {
                blocksBox.getChildren().remove(box);
            }
        });

        header.getChildren().add(remove);
        box.getChildren().addAll(header, sets);
        block.getSets().forEach(set -> addSetEditor(sets, set));

        Button addSet = new Button("+ Add Set");
        addSet.setOnAction(event -> addSetEditor(sets, new WorkoutSet(1, 1)));
        box.getChildren().add(addSet);
        blocksBox.getChildren().add(Math.max(0, blocksBox.getChildren().size() - 1), box);
    }

    private void addSetEditor(VBox sets, WorkoutSet set) {
        TextField reps = new TextField(String.valueOf(set.getRepetitions()));
        TextField weight = new TextField(String.valueOf(set.getWeightKilograms()));
        reps.setPrefWidth(70);
        weight.setPrefWidth(70);

        HBox row = new HBox(5, reps, dialogLabel("reps"), weight, dialogLabel("kg"));
        row.setAlignment(Pos.CENTER_LEFT);

        Button remove = new Button("-");
        remove.setOnAction(event -> {
            if (sets.getChildren().size() > 1) {
                sets.getChildren().remove(row);
            }
        });

        row.getChildren().add(remove);
        row.getProperties().put("reps", reps);
        row.getProperties().put("weight", weight);
        sets.getChildren().add(row);
    }

    private Label dialogLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        return label;
    }

    /** Displays an inline validation error without closing the dialog. */
    public void setError(String message) {
        errorArea.setText(message);
    }
}
