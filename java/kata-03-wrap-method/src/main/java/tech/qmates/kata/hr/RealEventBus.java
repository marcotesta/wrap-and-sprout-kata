package tech.qmates.kata.hr;

/**
 * Production {@link EventBus}. In a real system {@code publish} would fan out to
 * subscribers / a message broker — a genuine, irreversible side effect.
 *
 * <p>Here it deliberately throws: if this runs inside a test, you accidentally
 * triggered a real publication. Inject an {@code ObservableEventBus} in tests
 * instead, and keep this one for the wrapper's production wiring.
 */
public class RealEventBus implements EventBus {

    @Override
    public void publish(Object event) {
        throw new UnsupportedOperationException(
                "RealEventBus.publish performed a REAL publication (message broker / subscribers). "
                        + "This must never run in a test — inject an ObservableEventBus and assert on "
                        + "its publishedEvents() instead.");
    }
}
