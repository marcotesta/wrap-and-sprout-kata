package tech.qmates.kata.hr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Hard-to-test singleton repository. Every accessor reaches out to a real
 * database connection, which makes any code that depends on it painful to
 * exercise in isolation.
 */
public final class EmployeeRepository {

    private static final String JDBC_URL = "jdbc:postgresql://hr-prod-db.internal:5432/hr";
    private static final String DB_USER = "hr_service";
    private static final String DB_PASSWORD = "s3cr3t-prod-password";

    private static EmployeeRepository instance;

    private final Connection connection;

    private EmployeeRepository() {
        try {
            // Opens a real connection against the production HR database.
            this.connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to the HR database", e);
        }
    }

    public static synchronized EmployeeRepository getInstance() {
        if (instance == null) {
            instance = new EmployeeRepository();
        }
        return instance;
    }

    public EmployeeRecord findById(String employeeId) {
        String sql = "SELECT id, title, department, salary, years_in_role, performance_rating "
                + "FROM employees WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employeeId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                EmployeeRecord record = new EmployeeRecord(rs.getString("id"));
                record.setTitle(rs.getString("title"));
                record.setDepartment(rs.getString("department"));
                record.setSalary(rs.getInt("salary"));
                record.setYearsInRole(rs.getInt("years_in_role"));
                record.setPerformanceRating(rs.getInt("performance_rating"));
                return record;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load employee " + employeeId, e);
        }
    }

    public void save(EmployeeRecord record) {
        String sql = "UPDATE employees SET title = ?, department = ?, salary = ?, "
                + "years_in_role = ?, performance_rating = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, record.getTitle());
            statement.setString(2, record.getDepartment());
            statement.setInt(3, record.getSalary());
            statement.setInt(4, record.getYearsInRole());
            statement.setInt(5, record.getPerformanceRating());
            statement.setString(6, record.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to persist employee " + record.getId(), e);
        }
    }
}
