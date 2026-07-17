package tech.qmates.kata.hr;

/**
 * Domain event published when an employee is successfully promoted.
 */
public class PromotionEvent {

    private final String employeeId;
    private final String newTitle;
    private final boolean newLeadership;

    public PromotionEvent(String employeeId, String newTitle, boolean newLeadership) {
        this.employeeId = employeeId;
        this.newTitle = newTitle;
        this.newLeadership = newLeadership;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public boolean isNewLeadership() {
        return newLeadership;
    }

    @Override
    public String toString() {
        return "PromotionEvent{employeeId='" + employeeId + "', newTitle='" + newTitle
                + "', newLeadership=" + newLeadership + "}";
    }
}
