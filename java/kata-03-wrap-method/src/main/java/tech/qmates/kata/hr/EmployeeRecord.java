package tech.qmates.kata.hr;

/**
 * Plain data holder for an employee row.
 */
public class EmployeeRecord {

    private final String id;
    private String title;
    private String department;
    private int salary;
    private int yearsInRole;
    private int performanceRating;

    public EmployeeRecord(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public int getYearsInRole() {
        return yearsInRole;
    }

    public void setYearsInRole(int yearsInRole) {
        this.yearsInRole = yearsInRole;
    }

    public int getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(int performanceRating) {
        this.performanceRating = performanceRating;
    }
}
