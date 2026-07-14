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
    private final String languageCode;

    public QuarterlyReportGenerator() {
        // Configuration is read from the environment at construction time.
        String jdbcUrl = System.getenv("REPORTING_DB_URL");
        String username = System.getenv("REPORTING_DB_USER");
        String password = System.getenv("REPORTING_DB_PASSWORD");
        this.companyName = System.getenv("REPORTING_COMPANY_NAME");
        this.languageCode = System.getenv("REPORTING_LANGUAGE");

        // A real database connection is created right here, in the constructor.
        this.databaseConnection = new DatabaseConnection(jdbcUrl, username, password);
    }

    public String generate() {
        List<DepartmentRow> rows = databaseConnection.queryQuarterlyRows();

        // Legacy inline localization: report is currently translated right here,
        // in this untestable class. This exactly the kind  of hardcoded logic
        // you cannot easily cover with tests from here — which is why
        // the new header row is grown as a separate, tested sprout class.
        String reportTitle = reportTitleFor(languageCode);

        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<title>").append(reportTitle).append("</title>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>").append(reportTitle).append("</h1>");

        if (companyName != null && !companyName.isBlank()) {
            html.append("<h2>").append(companyName).append("</h2>");
        }

        html.append("<table>");

        // TODO (kata): a localised header row must be produced here by a sprouted class.
        // The column titles depend on `languageCode`, so pass it to the new class.
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
            html.append("<p>").append(noResultLabelFor(languageCode)).append("</p>");
        } else {
            html.append("<p>").append(totalRowsLabelFor(languageCode)).append(rows.size()).append("</p>");
        }

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private static String totalRowsLabelFor(String languageCode) {
        if ("it".equalsIgnoreCase(languageCode))
            return "Righe totali: ";

        return "Total rows: ";
    }

    private static String reportTitleFor(String languageCode) {
        if ("it".equalsIgnoreCase(languageCode))
            return "Report Trimestrale";

        return "Quarterly Report";
    }

    private static String noResultLabelFor(String languageCode) {
        if ("it".equalsIgnoreCase(languageCode))
            return "Nessun risultato disponibile per questo trimestre.";

        return "No results available for this quarter.";
    }

}
