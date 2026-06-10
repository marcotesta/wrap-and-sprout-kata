package tech.qmates.kata.reporting;

import java.math.BigDecimal;

/**
 * Small read-model carrying a single row of quarterly results.
 */
public record DepartmentRow(String department, String manager, BigDecimal profit, BigDecimal expenses) {
}
