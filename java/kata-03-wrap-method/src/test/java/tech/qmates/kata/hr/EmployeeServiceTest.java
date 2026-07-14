package tech.qmates.kata.hr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Skeleton test class — no @Test methods yet; that is your job.
 *
 * TODO: Drive the NEW publishing method test-first, in isolation. Don't call
 *       promote(...) (it hits the database) — test the new method directly:
 *       inject an ObservableEventBus and assert on bus.publishedEvents().
 *       `new EmployeeService()` is cheap, so no mocks or subclassing needed.
 *
 * // @Test
 * // void publishesPromotionEvent() {
 * //     ObservableEventBus bus = new ObservableEventBus();
 * //     new EmployeeService().publishPromotionEvent(bus, "E-1", "Senior Engineer");
 * //     // assert bus.publishedEvents() holds one PromotionEvent for "E-1" / "Senior Engineer"
 * // }
 */
class EmployeeServiceTest {
}
