package tech.qmates.kata.hr;

/**
 * Domain event published when an employee is successfully promoted.
 */
public class PromotionEvent {

    private final String employeeId;
    private final String newTitle;

    public PromotionEvent(String employeeId, String newTitle) {
        this.employeeId = employeeId;
        this.newTitle = newTitle;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getNewTitle() {
        return newTitle;
    }

    @Override
    public String toString() {
        return "PromotionEvent{employeeId='" + employeeId + "', newTitle='" + newTitle + "'}";
    }
}
