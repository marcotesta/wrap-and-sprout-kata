package tech.qmates.kata.payroll;

public class Employee {

    private final String name;
    private final String accountNumber;
    private final double grossPay;

    public Employee(String name, String accountNumber, double grossPay) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.grossPay = grossPay;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getGrossPay() {
        return grossPay;
    }
}
