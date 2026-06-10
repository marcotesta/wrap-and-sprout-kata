package tech.qmates.kata.hr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Skeleton test class. There are intentionally no @Test methods yet — that is
 * your job in this kata.
 *
 * TODO: Once you have applied Wrap Method, drive the new wrapped behaviour
 *       test-first. Verify that {@code publishPromotionEvent} is invoked on a
 *       successful promotion, and — critically — that NO PromotionEvent is
 *       published when the wrapped executePromotion throws a PromotionException.
 *
 * Example shape (commented out on purpose):
 *
 * @Test
 * void publishesPromotionEventOnSuccess() {
 *     // arrange: a seam that lets you observe EventBus.publish(...)
 *     // act:     service.promote("E-1", "Senior Engineer");
 *     // assert:  exactly one PromotionEvent was published
 * }
 */
class EmployeeServiceTest {
}
