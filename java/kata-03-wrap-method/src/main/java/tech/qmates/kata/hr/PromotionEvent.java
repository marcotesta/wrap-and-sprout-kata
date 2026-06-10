package tech.qmates.kata.hr;

/**
 * Domain event published when an employee is successfully promoted.
 */
public class PromotionEvent {

    private final String employeeId;
    private final String previousTitle;
    private final String newTitle;

    public PromotionEvent(String employeeId, String previousTitle, String newTitle) {
        this.employeeId = employeeId;
        this.previousTitle = previousTitle;
        this.newTitle = newTitle;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getPreviousTitle() {
        return previousTitle;
    }

    public String getNewTitle() {
        return newTitle;
    }

    @Override
    public String toString() {
        return "PromotionEvent{employeeId='" + employeeId + "', previousTitle='"
                + previousTitle + "', newTitle='" + newTitle + "'}";
    }
}
