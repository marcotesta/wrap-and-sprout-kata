package tech.qmates.kata.orders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Skeleton test for the timing/logging wrapper you will build in this kata.
 *
 * <p>There are intentionally NO @Test methods yet: writing them, test-first,
 * is your job.
 */
class TimingOrderProcessorTest {

    // TODO: Write a test that proves the wrapper logs a warning (via Logger.warn)
    //       when placeOrder takes longer than 2000ms, and logs nothing when it is fast.
    //       Drive a fake IOrderProcessor (the interface you extract from OrderProcessor)
    //       and a capturing Logger so the timing/logging behaviour can be asserted
    //       WITHOUT touching the real OrderRepository / EmailService / InventoryService.
    //
    // @Test
    // void logs_a_warning_when_placeOrder_exceeds_the_threshold() {
    //     // given a slow IOrderProcessor and a TimingOrderProcessor wrapping it...
    //     // when placeOrder is called...
    //     // then Logger.warn was invoked exactly once with a timing message.
    //     assertTrue(false, "not implemented yet");
    // }
}
