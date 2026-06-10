package tech.qmates.kata.orders;

import java.time.Instant;

/**
 * A simple logger that is trivially instantiated with {@code new Logger()}.
 * The wrapper you build in this kata should use this to emit warnings.
 */
public class Logger {

    public void warn(String message) {
        System.out.println("[WARN] " + Instant.now() + " - " + message);
    }

    public void info(String message) {
        System.out.println("[INFO] " + Instant.now() + " - " + message);
    }
}
