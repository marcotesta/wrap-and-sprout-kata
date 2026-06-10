package tech.qmates.kata.reporting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hard-to-test external dependency.
 *
 * <p>This class opens a real JDBC connection to a database the moment it is
 * constructed. There is no seam here: any code that creates a
 * {@code DatabaseConnection} will try to reach a live database, which is exactly
 * what makes the surrounding legacy code painful to unit test.
 */
public class DatabaseConnection {

    private final Connection connection;

    public DatabaseConnection(String jdbcUrl, String username, String password) {
        try {
            // Real connection attempt against an external resource.
            this.connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to connect to the reporting database", e);
        }
    }

    public List<DepartmentRow> queryQuarterlyRows() {
        List<DepartmentRow> rows = new ArrayList<>();
        String sql = "SELECT department, manager, profit, expenses FROM quarterly_results";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(new DepartmentRow(
                        resultSet.getString("department"),
                        resultSet.getString("manager"),
                        resultSet.getBigDecimal("profit"),
                        resultSet.getBigDecimal("expenses")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to read quarterly results", e);
        }
        return rows;
    }
}
