package tech.qmates.kata.hr;

/**
 * Publishes domain events. There are two implementations: {@link RealEventBus}
 * for production (not wired up in this kata, so it throws) and, under
 * {@code src/test}, an {@code ObservableEventBus} that merely records what was
 * published so tests can assert on it — no database, no mocking framework.
 *
 * <p>Inject a {@link RealEventBus} in the wrapper and an {@code ObservableEventBus}
 * in the tests of your new publishing method.
 */
public interface EventBus {

    void publish(Object event);
}
