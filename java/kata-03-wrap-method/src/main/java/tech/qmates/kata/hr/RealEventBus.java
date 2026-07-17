package tech.qmates.kata.hr;

/**
 * Production {@link EventBus}. In a real system {@code publish} fans out to a
 * message broker. That infrastructure is not wired up in this kata, so it throws:
 * reaching it means you are on the real production path. In tests, inject an
 * {@code ObservableEventBus} instead.
 */
public class RealEventBus implements EventBus {

    @Override
    public void publish(Object event) {
        throw new UnsupportedOperationException(
                "RealEventBus is the production bus and is not wired up in this kata. "
                        + "Inject an ObservableEventBus in tests and assert on its publishedEvents().");
    }
}
