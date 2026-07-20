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
    //       and a Logger you can observe — a recording subclass, or a recording
    //       implementation of an interface you extract for Logger — injected into the
    //       wrapper, so the behaviour can be asserted WITHOUT real I/O or console capturing.
    //
    // @Test
    // void logs_a_warning_when_placeOrder_exceeds_the_threshold() {
    //     // given a slow IOrderProcessor and a recording Logger, wrapped by a TimingOrderProcessor...
    //     // when placeOrder is called...
    //     // then the recording Logger captured exactly one timing warning.
    // }
}
