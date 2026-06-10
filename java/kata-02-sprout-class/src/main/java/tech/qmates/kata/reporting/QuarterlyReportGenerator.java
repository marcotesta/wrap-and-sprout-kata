package tech.qmates.kata.reporting;

import java.util.List;

/**
 * Legacy quarterly report generator.
 *
 * <p>This class is deliberately hard to test:
 * <ul>
 *   <li>Its constructor directly {@code new}s a real {@link DatabaseConnection},
 *       which opens a live JDBC connection.</li>
 *   <li>It reads its configuration straight from {@link System#getenv(String)}.</li>
 * </ul>
 *
 * <p>There is no dependency injection and no factory: instantiating this class
 * tries to reach an external database. That is exactly the situation in which
 * the Sprout Class technique shines — you add new behaviour in a brand new,
 * independently testable class rather than fighting this untestable code.
 */
public class QuarterlyReportGenerator {

    private final DatabaseConnection databaseConnection;
    private final String companyName;

    public QuarterlyReportGenerator() {
        // Configuration is read from the environment at construction time.
        String jdbcUrl = System.getenv("REPORTING_DB_URL");
        String username = System.getenv("REPORTING_DB_USER");
        String password = System.getenv("REPORTING_DB_PASSWORD");
        this.companyName = System.getenv("REPORTING_COMPANY_NAME");

        // A real database connection is created right here, in the constructor.
        this.databaseConnection = new DatabaseConnection(jdbcUrl, username, password);
    }

    public String generate() {
        List<DepartmentRow> rows = databaseConnection.queryQuarterlyRows();

        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<title>Quarterly Report</title>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>Quarterly Report</h1>");

        if (companyName != null && !companyName.isBlank()) {
            html.append("<h2>").append(companyName).append("</h2>");
        }

        html.append("<table>");

        // TODO (kata): a header row must be produced here by a sprouted class.
        // Today the table jumps straight into the data rows with no header.

        for (DepartmentRow row : rows) {
            html.append("<tr>");
            html.append("<td>").append(row.department()).append("</td>");
            html.append("<td>").append(row.manager()).append("</td>");
            html.append("<td>").append(row.profit()).append("</td>");
            html.append("<td>").append(row.expenses()).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");

        if (rows.isEmpty()) {
            html.append("<p>No results available for this quarter.</p>");
        } else {
            html.append("<p>Total rows: ").append(rows.size()).append("</p>");
        }

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
