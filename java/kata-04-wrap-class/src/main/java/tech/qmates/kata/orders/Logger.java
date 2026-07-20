package tech.qmates.kata.orders;

import java.time.Instant;

/**
 * A simple logger, trivially instantiated with {@code new Logger()}.
 * Inject it into the wrapper (pass {@code new Logger()} in production). To observe
 * warnings in a test, subclass it or extract an interface for it.
 */
public class Logger {

    public void warn(String message) {
        System.out.println("[WARN] " + Instant.now() + " - " + message);
    }

    public void info(String message) {
        System.out.println("[INFO] " + Instant.now() + " - " + message);
    }
}
