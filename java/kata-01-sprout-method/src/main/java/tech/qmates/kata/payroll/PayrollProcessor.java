package tech.qmates.kata.payroll;

import java.util.List;

public class PayrollProcessor {

    public void processPayroll(List<Employee> employees) {
        int paidCount = 0;
        double totalPaid = 0.0;

        for (Employee employee : employees) {
            double gross = employee.getGrossPay();

            // Hardcoded tax table with a few brackets.
            double taxRate;
            if (gross <= 1000.0) {
                taxRate = 0.10;
            } else if (gross <= 3000.0) {
                taxRate = 0.20;
            } else if (gross <= 6000.0) {
                taxRate = 0.30;
            } else {
                taxRate = 0.40;
            }

            double netPay = gross - (gross * taxRate);

            // The gateway is instantiated directly inside the method and makes
            // real HTTP calls. This is the dependency that makes the method hard
            // to test in isolation.
            PaymentGateway gateway = new PaymentGateway();
            gateway.send(employee.getAccountNumber(), netPay);

            paidCount++;
            totalPaid += netPay;

            System.out.println("Paid " + employee.getName() + ": " + netPay);
        }

        // Summary line written straight to System.out.
        System.out.println("Payroll complete. Employees paid: " + paidCount
                + ", total disbursed: " + totalPaid);
    }
}
