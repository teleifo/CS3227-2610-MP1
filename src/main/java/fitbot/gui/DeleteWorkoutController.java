package fitbot.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controls the custom delete-workout confirmation view.
 */
public class DeleteWorkoutController {
    @FXML private Label titleLabel;

    /** Sets the workout number shown in the confirmation message. */
    public void setPosition(int position) {
        titleLabel.setText("Delete workout " + position + "?");
    }
}
