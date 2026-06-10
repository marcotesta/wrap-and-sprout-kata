package tech.qmates.kata.hr;

/**
 * Legacy HR service. Both {@link #promote(String, String)} and
 * {@link #changeDepartment(String, String)} reach out to the
 * {@link EmployeeRepository} singleton directly, load a record, enforce a set
 * of hardcoded business rules, then persist the mutated record back through the
 * same singleton.
 */
public class EmployeeService {

    public void promote(String employeeId, String newTitle) throws PromotionException {
        EmployeeRepository repository = EmployeeRepository.getInstance();

        EmployeeRecord employee = repository.findById(employeeId);
        if (employee == null) {
            throw new PromotionException("Unknown employee: " + employeeId);
        }

        // Hardcoded business rules.
        if (newTitle == null || newTitle.isBlank()) {
            throw new PromotionException("A new title is required to promote an employee");
        }
        if (newTitle.equalsIgnoreCase(employee.getTitle())) {
            throw new PromotionException("Employee already holds the title: " + newTitle);
        }
        if (employee.getYearsInRole() < 1) {
            throw new PromotionException("Employee must spend at least 1 year in role before promotion");
        }
        if (employee.getPerformanceRating() < 3) {
            throw new PromotionException("Performance rating too low for promotion");
        }
        if (newTitle.startsWith("VP") && employee.getSalary() < 90_000) {
            throw new PromotionException("VP promotions require a base salary of at least 90000");
        }

        // Apply the promotion.
        employee.setTitle(newTitle);
        employee.setYearsInRole(0);
        employee.setSalary((int) Math.round(employee.getSalary() * 1.10));

        repository.save(employee);
    }

    public void changeDepartment(String employeeId, String newDepartment) throws PromotionException {
        EmployeeRepository repository = EmployeeRepository.getInstance();

        EmployeeRecord employee = repository.findById(employeeId);
        if (employee == null) {
            throw new PromotionException("Unknown employee: " + employeeId);
        }

        // Hardcoded business rules.
        if (newDepartment == null || newDepartment.isBlank()) {
            throw new PromotionException("A target department is required");
        }
        if (newDepartment.equalsIgnoreCase(employee.getDepartment())) {
            throw new PromotionException("Employee already works in: " + newDepartment);
        }
        if (employee.getYearsInRole() < 1) {
            throw new PromotionException("Employee must spend at least 1 year in role before transferring");
        }

        // Apply the transfer.
        employee.setDepartment(newDepartment);
        employee.setYearsInRole(0);

        repository.save(employee);
    }
}
