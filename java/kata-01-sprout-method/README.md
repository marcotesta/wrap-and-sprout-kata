# Kata 01 — Sprout Method

Add new behaviour to untested legacy code by growing it in a fresh, fully tested method instead of editing the tangle in place.

## Context

Sprout Method, from Michael Feathers' *Working Effectively with Legacy Code* (Ch. 6), is a technique for adding functionality to a method you cannot easily test or fully understand. Rather than weaving new logic into the existing untested code, you write the new behaviour in a brand-new method that you can develop test-first. You then call that new method from the old one at the right place. This lets you add tested code today without first taking on the much larger job of getting the surrounding legacy method under test. It is a pragmatic way to make progress: the sprout is clean and covered even if the host method stays messy.

## When This Technique Is Useful

- You need to add a clearly separable piece of new behaviour to a method you cannot easily test.
- Getting the entire host method under test would be expensive or risky right now.
- The new logic can be expressed as a self-contained method with clear inputs and outputs.
- You want at least the new code to be covered by tests, even if the legacy code is not.
- You want to minimize edits to the existing, fragile method.

## When This Technique Is NOT Useful (or a Code Smell)

- If you are sprouting over and over into the same class, it signals the host class needs real refactoring, not more sprouts.
- When the new behaviour is deeply entangled with the existing logic and cannot be cleanly separated.
- When you could reasonably get the host method under test first (then a normal change is cleaner).
- If the sprout needs so many parameters that it reveals the host method is doing too much.
- When it is used as an excuse to permanently avoid addressing the underlying legacy design.

## Steps to Apply the Technique

1. **Identify where in the existing method you need the change.**
   Find the exact spot in the legacy method where the new behaviour belongs.

   ```java
   for (Employee employee : employees) {
       // <-- new validation behaviour belongs here, before payment
       PaymentGateway gateway = new PaymentGateway();
       gateway.send(employee.getAccountNumber(), netPay);
   }
   ```

2. **Write a commented-out call to a new method that will do the work.**

   ```java
   // List<String> invalid = collectInvalidAccounts(employees);
   ```

3. **Determine which local variables the new method needs and make them parameters.**
   The validation needs the list of employees, so it becomes a parameter.

   ```java
   // List<String> collectInvalidAccounts(List<Employee> employees) { ... }
   ```

4. **Determine whether the new method must return a value.**
   Here the method must return the names of skipped employees, so it returns a `List<String>`.

   ```java
   List<String> collectInvalidAccounts(List<Employee> employees) {
       // returns names with invalid account numbers
   }
   ```

5. **Develop the sprout method test-first (TDD).**
   Write failing tests for the new method, then implement it until they pass.

   ```java
   @Test
   void shouldCollectName_whenAccountNumberIsEmpty() {
       // arrange, act, assert against collectInvalidAccounts(...)
   }
   ```

6. **Uncomment the call.**
   Wire the now-tested sprout into the legacy method.

   ```java
   List<String> invalid = collectInvalidAccounts(employees);
   ```

## The Kata

### Background

A company runs a small payroll system. Every pay cycle, a `PayrollProcessor` takes a list of employees, computes each one's net pay from a tax table, and disburses the money through a banking integration. The code works in production but has no tests.

### Legacy Code Description

`PayrollProcessor.processPayroll(List<Employee> employees)` loops over employees, applies a hardcoded tax table to compute net pay, and pays each one. It is hard to test because it instantiates `new PaymentGateway()` directly inside the method, and that gateway makes real HTTP calls to a banking endpoint — so running the method in a test would hit the network. It also writes a summary line straight to `System.out`. There is no seam to intercept the payment or capture the output.

### Your Task

Before sending any payment, validate that the employee's bank account number matches a simple format rule: non-empty, numeric characters only, and between 6 and 34 digits in length. Employees with invalid account numbers must be **skipped** (no payment sent), and their names must be collected into a `List<String>` that is **returned** by the method. Change the return type of `processPayroll` from `void` to `List<String>`. Implement the validation as a **sprouted, independently-testable method** — develop it test-first and only then wire it into the legacy loop.

### Acceptance Criteria

- Validation is extracted into a new, separate method that can be tested without touching `PaymentGateway` or the network.
- The sprout method is covered by unit tests written test-first.
- Employees with invalid account numbers are skipped and never passed to the gateway.
- `processPayroll` returns the list of names of skipped employees.
- The original tax/payment logic for valid employees remains unchanged.

### Hints

- Test the sprout method in isolation; you should not need to call `processPayroll` (and thus the gateway) to test the validation rule.
- Keep the sprout method's signature simple — pass in only what it needs (an account number, or the employee), and let it return its result.
- Consider package-private visibility for the sprout method so the test in the same package can reach it directly.
