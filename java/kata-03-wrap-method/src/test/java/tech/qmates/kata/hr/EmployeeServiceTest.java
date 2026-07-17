package tech.qmates.kata.hr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Skeleton test class — no @Test methods yet; that is your job.
 *
 * TODO: Drive the NEW publishing method test-first, in isolation. Don't call
 *       promote(...) (it hits the database) — test the method directly: inject
 *       an ObservableEventBus and assert on bus.publishedEvents().
 *       `new EmployeeService()` is cheap, so no mocks or subclassing needed.
 *
 *       Grow the newLeadership rule one test at a time: it is true when newTitle
 *       starts (case-insensitively) with "Head", "Chief" or "Manager".
 *         - "Head of Sales"   -> newLeadership true
 *         - "Senior Engineer" -> newLeadership false
 *         - "chief of staff"  -> newLeadership true (case-insensitive)
 *
 * // @Test
 * // void setsLeadershipFlagForLeadershipTitle() {
 * //     ObservableEventBus bus = new ObservableEventBus();
 * //     new EmployeeService().publishPromotionEvent(bus, "E-1", "Head of Sales");
 * //     // assert one PromotionEvent whose newTitle is "Head of Sales" and newLeadership is true
 * // }
 */
class EmployeeServiceTest {
}
