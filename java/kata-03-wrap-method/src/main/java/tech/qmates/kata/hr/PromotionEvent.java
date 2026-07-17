package tech.qmates.kata.hr;

/**
 * Domain event published when an employee is successfully promoted.
 */
public record PromotionEvent(String employeeId, String newTitle, boolean newLeadership) {
}
