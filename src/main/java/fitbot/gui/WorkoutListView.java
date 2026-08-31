package fitbot.gui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import fitbot.formatter.DurationFormatter;
import fitbot.model.CycleWorkout;
import fitbot.model.GymWorkout;
import fitbot.model.RunWorkout;
import fitbot.model.Workout;
import fitbot.model.WorkoutType;
import fitbot.service.WorkoutService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Displays workouts as readable summary cards. */
public class WorkoutListView extends ListView<Workout> {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);
    /** Shared service providing the current workouts. */
    private final WorkoutService service;

    /** Creates a workout list backed by the supplied service. */
    public WorkoutListView(WorkoutService service) {
        this.service = service;
        Label emptyMessage = new Label("No workouts have been logged yet.");
        emptyMessage.setStyle("-fx-text-fill: #ffffff;");
        setPlaceholder(emptyMessage);
        setCellFactory(list -> new WorkoutCardCell());
        refresh();
    }

    /** Reloads the displayed workouts from the service. */
    public final void refresh() {
        setItems(FXCollections.observableArrayList(service.getWorkouts()));
    }

    /** Displays only workouts of the selected type, or all workouts when null. */
    public void filterByType(WorkoutType type) {
        if (type == null) {
            refresh();
            return;
        }
        setItems(FXCollections.observableArrayList(service.getWorkouts().stream()
                .filter(workout -> workout.getType() == type)
                .toList()));
    }

    /** Returns the workout's position in the complete, unfiltered list. */
    public int getOriginalPosition(Workout workout) {
        return service.getWorkouts().indexOf(workout) + 1;
    }

    /** Renders one workout as a card in the list. */
    private static class WorkoutCardCell extends ListCell<Workout> {
        @Override
        protected void updateItem(Workout workout, boolean empty) {
            super.updateItem(workout, empty);
            setStyle("-fx-background-color: transparent;");
            if (empty || workout == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            VBox card = new VBox(8);
            card.setPadding(new Insets(14));
            card.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"
                    + " -fx-border-radius: 8; -fx-background-radius: 8;");

            Label date = new Label(workout.getDate().format(DATE_FORMAT));
            date.setStyle("-fx-font-size: 11px; -fx-text-fill: #dfe4ea;");

            Label type = new Label(workout.getType().toString());
            type.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;"
                    + " -fx-text-fill: #ffffff;");

            HBox metrics = new HBox(28);
            metrics.getChildren().addAll(createMetrics(workout));
            card.getChildren().addAll(date, type, metrics);
            setGraphic(card);
        }

        /** Creates the metrics in the required order for the workout type. */
        private List<VBox> createMetrics(Workout workout) {
            if (workout instanceof RunWorkout run) {
                return List.of(
                        metric("Distance", String.format("%.2f km", run.getDistanceKilometres())),
                        metric("Pace", DurationFormatter.formatDuration(
                                Math.round(run.getPaceSecondsPerKilometre())) + "/km"),
                        metric("Time", DurationFormatter.formatDuration(run.getDurationSeconds())));
            }
            if (workout instanceof CycleWorkout cycle) {
                return List.of(
                        metric("Distance", String.format("%.2f km", cycle.getDistanceKilometres())),
                        metric("Speed", String.format("%.1f km/hr",
                                cycle.getSpeedKilometresPerHour())),
                        metric("Time", DurationFormatter.formatDuration(cycle.getDurationSeconds())));
            }
            if (workout instanceof GymWorkout gym) {
                return List.of(
                        metric("Blocks", String.valueOf(gym.getBlocks().size())),
                        metric("Total Volume", String.format("%.1f kg", gym.getTotalVolumeKilograms())),
                        metric("Time", DurationFormatter.formatDuration(gym.getDurationSeconds())));
            }
            return List.of();
        }

        /** Creates one metric with its label above its value. */
        private VBox metric(String name, String value) {
            Label nameLabel = new Label(name);
            nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #dfe4ea;");

            Label valueLabel = new Label(value);
            valueLabel.setWrapText(false);
            valueLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;"
                    + " -fx-text-fill: #ffffff;");

            return new VBox(2, nameLabel, valueLabel);
        }
    }
}
