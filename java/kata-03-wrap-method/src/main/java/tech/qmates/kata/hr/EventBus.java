package tech.qmates.kata.hr;

/**
 * A lightweight, directly instantiatable event bus. Unlike the repository it is
 * trivial to construct with {@code new EventBus()}, which makes it a friendly
 * collaborator for new, test-driven behaviour.
 */
public class EventBus {

    public void publish(Object event) {
        // In a real system this would fan out to subscribers / a message broker.
        System.out.println("[EventBus] published: " + event);
    }
}
