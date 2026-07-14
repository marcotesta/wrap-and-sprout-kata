package tech.qmates.kata.hr;

import java.util.ArrayList;
import java.util.List;

/**
 * Test-only {@link EventBus} that records what was published instead of really
 * publishing it. Inject it into the new publishing method under test, then
 * assert on {@link #publishedEvents()} — no database, no mocking framework
 * required.
 */
public class ObservableEventBus implements EventBus {

    private final List<Object> publishedEvents = new ArrayList<>();

    @Override
    public void publish(Object event) {
        publishedEvents.add(event);
        System.out.println("[ObservableEventBus] recorded: " + event);
    }

    /** Events published through this bus, in order — handy for assertions in tests. */
    public List<Object> publishedEvents() {
        return publishedEvents;
    }
}
