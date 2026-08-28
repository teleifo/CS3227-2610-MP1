package fitbot;

/**
 * A launcher class to workaround classpath issues.
 */
public class Launcher {
    public static void main(String[] args) {
        FitBot fitbot = new FitBot();
        fitbot.start();
    }
}
