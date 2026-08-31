package fitbot.gui;

import fitbot.command.CommandResult;
import fitbot.exception.FitBotException;
import fitbot.parser.Parser;
import fitbot.service.WorkoutService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controls the FXML command panel.
 */
public class CommandPanelController {
    @FXML private TextField commandInput;
    @FXML private TextArea commandOutput;
    private WorkoutService service;
    private Runnable refreshAction = () -> { };

    /** Supplies the shared service after the view is loaded. */
    public void setService(WorkoutService service) {
        this.service = service;
    }

    /** Supplies the workout-list refresh action. */
    public void setRefreshAction(Runnable refreshAction) {
        this.refreshAction = refreshAction;
    }

    /** Executes the command entered by the user. */
    @FXML
    private void submitCommand(ActionEvent event) {
        String input = commandInput.getText().trim();
        if (input.isEmpty()) {
            return;
        }
        commandOutput.clear();

        try {
            CommandResult result = service.execute(Parser.parseCommand(input));
            commandOutput.setText(result.getMessage());
            if (result.wasDataModified()) {
                refreshAction.run();
            }
            if (result.shouldExit()) {
                commandInput.getScene().getWindow().hide();
            }
            commandInput.clear();
        } catch (FitBotException exception) {
            commandOutput.setText(exception.getMessage());
        }
    }
}
