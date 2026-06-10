export interface Employee {
  name: string;
  accountNumber: string;
  grossPay: number;
}

/**
 * Hard-to-test external dependency: it makes a real network call to the
 * payments provider. Instantiating this inside PayrollProcessor is exactly
 * what makes the legacy method painful to unit test.
 */
export class PaymentGateway {
  async send(account: string, amount: number): Promise<void> {
    await fetch('https://payments.example.com/api/v1/transfers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ account, amount }),
    });
  }
}

export class PayrollProcessor {
  processPayroll(employees: Employee[]): void {
    let totalPaid = 0;
    let count = 0;

    for (const employee of employees) {
      // Hardcoded progressive tax table.
      let tax: number;
      if (employee.grossPay <= 1000) {
        tax = employee.grossPay * 0.1;
      } else if (employee.grossPay <= 3000) {
        tax = 100 + (employee.grossPay - 1000) * 0.2;
      } else if (employee.grossPay <= 6000) {
        tax = 500 + (employee.grossPay - 3000) * 0.3;
      } else {
        tax = 1400 + (employee.grossPay - 6000) * 0.4;
      }

      const netPay = employee.grossPay - tax;

      // Direct instantiation of the external service inside the method.
      const gateway = new PaymentGateway();
      gateway.send(employee.accountNumber, netPay);

      totalPaid += netPay;
      count += 1;
    }

    console.log(`Payroll complete: paid ${count} employees, total net ${totalPaid}`);
  }
}
