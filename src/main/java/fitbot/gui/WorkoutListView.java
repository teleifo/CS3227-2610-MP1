package fitbot.gui;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import fitbot.formatter.ListFormatter;
import fitbot.model.Workout;
import fitbot.service.WorkoutService;

/** Displays the workouts currently stored by the application. */
public class WorkoutListView extends ListView<String> {
    /** Shared service providing the current workouts. */
    private final WorkoutService service;

    /** Creates a workout list backed by the supplied service. */
    public WorkoutListView(WorkoutService service) {
        this.service = service;
        setPlaceholder(new Label("No workouts have been logged yet."));
        refresh();
    }

    /** Reloads the displayed summaries from the service. */
    public final void refresh() {
        java.util.List<String> summaries = service.getWorkouts().stream()
                .map(this::formatWorkout)
                .toList();
        setItems(FXCollections.observableArrayList(summaries));
    }

    /** Formats one workout using its one-based position in the list. */
    private String formatWorkout(Workout workout) {
        int position = service.getWorkouts().indexOf(workout) + 1;
        return ListFormatter.formatSummary(position, workout);
    }
}
