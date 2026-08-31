package fitbot.gui;

import fitbot.command.CommandResult;
import fitbot.command.ParsedCommand;
import fitbot.exception.FitBotException;
import fitbot.parser.Parser;
import fitbot.service.WorkoutService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * A collapsible panel for entering and displaying FitBot text commands.
 */
public class CommandPanel extends TitledPane {
    /** Shared service used to execute commands. */
    private final WorkoutService service;

    /** Field where the user enters a command. */
    private final TextField commandInput = new TextField();

    /** Area containing command results and errors. */
    private final TextArea commandOutput = new TextArea();

    /** Action to run after a command may have changed the workout list. */
    private final Runnable refreshAction;

    /** Creates a command panel backed by the supplied service. */
    public CommandPanel(WorkoutService service) {
        this(service, () -> { });
    }

    /** Creates a command panel with a callback for refreshing GUI data. */
    public CommandPanel(WorkoutService service, Runnable refreshAction) {
        this.service = service;
        this.refreshAction = refreshAction;
        setText("Text commands");
        setContent(createContent());
        setExpanded(true);
    }

    /** Builds the controls displayed inside the collapsible panel. */
    private BorderPane createContent() {
        commandInput.setPromptText("Enter a command, for example: list");
        commandInput.setOnAction(event -> submitCommand());

        Button submitButton = new Button("Run");
        submitButton.setDefaultButton(true);
        submitButton.setOnAction(event -> submitCommand());

        HBox inputRow = new HBox(8, new Label("Command:"), commandInput, submitButton);
        HBox.setHgrow(commandInput, Priority.ALWAYS);

        commandOutput.setEditable(false);
        commandOutput.setWrapText(true);
        commandOutput.setPromptText("Command output will appear here.");
        commandOutput.setPrefRowCount(5);

        BorderPane content = new BorderPane();
        content.setTop(inputRow);
        content.setCenter(commandOutput);
        BorderPane.setMargin(inputRow, new Insets(0, 0, 8, 0));
        return content;
    }

    /** Parses and executes the current command, then displays its result. */
    private void submitCommand() {
        String input = commandInput.getText().trim();
        if (input.isEmpty()) {
            return;
        }

        appendOutput("> " + input);
        try {
            ParsedCommand parsedCommand = Parser.parseCommand(input);
            CommandResult result = service.execute(parsedCommand);
            appendOutput(result.getMessage());
            if (result.wasDataModified()) {
                refreshAction.run();
            }
        } catch (FitBotException exception) {
            appendOutput(exception.getMessage());
        }

        commandInput.clear();
    }

    /** Adds one message to the command output history. */
    private void appendOutput(String message) {
        if (!commandOutput.getText().isEmpty()) {
            commandOutput.appendText("\n\n");
        }
        commandOutput.appendText(message);
    }
}
