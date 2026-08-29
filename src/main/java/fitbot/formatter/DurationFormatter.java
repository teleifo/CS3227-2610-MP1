package fitbot.formatter;

/**
 * Formats durations stored as a number of seconds for display.
 */
public final class DurationFormatter {
    private static final long SECONDS_PER_MINUTE = 60;
    private static final long SECONDS_PER_HOUR = 3600;
    private static final long SECONDS_PER_DAY = 86400;

    /**
     * Formats seconds as mm:ss, hh:mm:ss, or dd:hh:mm:ss.
     *
     * @param durationSeconds non-negative duration in seconds
     * @return formatted duration
     * @throws IllegalArgumentException if the duration is negative
     */
    public static String formatDuration(long durationSeconds) {
        if (durationSeconds < 0) {
            throw new IllegalArgumentException("Duration cannot be negative.");
        }

        long days = durationSeconds / SECONDS_PER_DAY;
        long remainder = durationSeconds % SECONDS_PER_DAY;
        long hours = remainder / SECONDS_PER_HOUR;
        remainder %= SECONDS_PER_HOUR;
        long minutes = remainder / SECONDS_PER_MINUTE;
        long seconds = remainder % SECONDS_PER_MINUTE;

        if (days > 0) {
            return String.format("%02d:%02d:%02d:%02d",
                    days, hours, minutes, seconds);
        }
        if (hours > 0) {
            return String.format("%02d:%02d:%02d",
                    hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}
