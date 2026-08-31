package fitbot.gui;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import fitbot.formatter.DurationFormatter;
import fitbot.model.CycleWorkout;
import fitbot.model.GymWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;
import fitbot.model.WorkoutBlock;
import fitbot.model.WorkoutSet;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Displays the selected workout as a detailed card. */
public class WorkoutDetailView extends ScrollPane {
    private static final String BACKGROUND = "#353b48";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);
    private static final String TEXT = "-fx-text-fill: #ffffff;";
    private static final String MUTED = "-fx-text-fill: #dfe4ea;";
    private IntConsumer deleteAction = position -> { };
    private BiConsumer<Integer, Workout> editAction = (position, workout) -> { };

    /** Creates a detail view with an empty-state message. */
    public WorkoutDetailView() {
        setMinWidth(0);
        setMaxWidth(Double.MAX_VALUE);
        setFitToWidth(true);
        setStyle("-fx-background: " + BACKGROUND + "; -fx-background-color: " + BACKGROUND + ";");
        showEmptyState();
    }

    /** Replaces the empty state with details for the selected workout. */
    public void showWorkout(int position, Workout workout) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + BACKGROUND + ";");
        card.getChildren().addAll(
                label("Workout " + position, "-fx-font-size: 11px; " + MUTED),
                label(workout.getType().toString(), "-fx-font-size: 22px; -fx-font-weight: bold; " + TEXT),
                metrics(workout));

        if (workout instanceof GymWorkout gym) {
            card.getChildren().add(label("Exercises", "-fx-font-size: 16px; -fx-font-weight: bold; "
                    + "-fx-padding: 5 0 0 0; " + TEXT));

            for (int i = 0; i < gym.getBlocks().size(); i++) {
                WorkoutBlock block = gym.getBlocks().get(i);

                VBox exercise = new VBox(5);
                exercise.getChildren().add(label((i + 1) + ". " + block.getExerciseName(),
                        "-fx-font-size: 14px; -fx-font-weight: bold; " + TEXT));
                exercise.getChildren().add(label(String.format("Volume: %.1f kg    1 Rep Max: %.1f kg",
                        block.getTotalVolumeKilograms(), block.getOneRepMaxKilograms()), MUTED));

                for (int j = 0; j < block.getSets().size(); j++) {
                    WorkoutSet set = block.getSets().get(j);
                    exercise.getChildren().add(label("Set " + (j + 1) + "    "
                            + set.getRepetitions() + " reps @ " + set.getWeightKilograms() + " kg", MUTED));
                }
                card.getChildren().add(exercise);
            }
        }
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #487eb0; -fx-text-fill: #ffffff;"
                + " -fx-font-weight: bold;");
        editButton.setOnAction(event -> editAction.accept(position, workout));
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #e84118; -fx-text-fill: #ffffff;"
                + " -fx-font-weight: bold;");
        deleteButton.setOnAction(event -> deleteAction.accept(position));
        card.getChildren().add(new HBox(8, editButton, deleteButton));
        setContent(card);
    }

    /** Supplies the action invoked by the Delete button. */
    public void setDeleteAction(IntConsumer deleteAction) {
        this.deleteAction = deleteAction;
    }

    /** Supplies the action invoked by the Edit button. */
    public void setEditAction(BiConsumer<Integer, Workout> editAction) {
        this.editAction = editAction;
    }

    private VBox metrics(Workout workout) {
        java.util.List<VBox> attributes = new java.util.ArrayList<>();
        attributes.add(metric("Date", workout.getDate().format(DATE_FORMAT)));
        attributes.add(metric("Time", DurationFormatter.formatDuration(workout.getDurationSeconds())));

        if (workout instanceof RunWorkout run) {
            attributes.add(metric("Distance", withUnit(run.getDistanceKilometres(), "%.2f", " km")));
            attributes.add(metric("Pace",
                    DurationFormatter.formatDuration(Math.round(run.getPaceSecondsPerKilometre())) + "/km"));
            attributes.add(metric("Elevation Gain", optional(run.getElevationGainMetres(), " metres")));
        } else if (workout instanceof CycleWorkout cycle) {
            attributes.add(metric("Distance", withUnit(cycle.getDistanceKilometres(), "%.2f", " km")));
            attributes.add(metric("Speed", withUnit(cycle.getSpeedKilometresPerHour(), "%.1f", " km/hr")));
            attributes.add(metric("Elevation Gain", optional(cycle.getElevationGainMetres(), " metres")));
            attributes.add(metric("Maximum Speed", optional(cycle.getMaxSpeedKilometresPerHour(), " km/hr")));
        } else if (workout instanceof GymWorkout gym) {
            attributes.add(metric("Blocks", String.valueOf(gym.getBlocks().size())));
            attributes.add(metric("Total Volume", String.format("%.1f kg", gym.getTotalVolumeKilograms())));
        }

        VBox metrics = new VBox(12);
        for (int index = 0; index < attributes.size(); index += 3) {
            HBox row = new HBox(20);
            row.getChildren().addAll(attributes.subList(index, Math.min(index + 3, attributes.size())));
            metrics.getChildren().add(row);
        }

        return metrics;
    }

    private VBox metric(String name, String value) {
        return new VBox(2, label(name, "-fx-font-size: 11px; " + MUTED),
                label(value, "-fx-font-size: 15px; -fx-font-weight: bold; " + TEXT));
    }

    private String withUnit(double value, String format, String unit) {
        return String.format(format, value) + unit;
    }

    private String optional(Object value, String unit) {
        return value == null ? "-" : value + unit;
    }

    private Label label(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        label.setWrapText(false);
        return label;
    }

    /** Restores the message shown when no workout is selected. */
    public void showEmptyState() {
        setContent(label("Select a workout to view its details.", "-fx-padding: 20px; " + MUTED));
    }
}
