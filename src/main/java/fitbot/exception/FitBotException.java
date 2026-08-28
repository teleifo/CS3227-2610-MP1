package fitbot.exception;

/** Represents invalid FitBot input or command syntax. */
public class FitBotException extends Exception {
    /** Creates an exception with an explanation of the input error. */
    public FitBotException(String message) {
        super(message);
    }
}
