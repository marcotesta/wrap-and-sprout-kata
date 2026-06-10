# Kata 01 — Sprout Method

Add new behavior to untestable legacy code by growing it as a fresh, fully tested method instead of editing the tangle in place.

## Context

The Sprout Method is a technique from Michael Feathers' *Working Effectively with Legacy Code* (Ch. 6). When you must add functionality to a method that has no tests and is awkward to change, you avoid weaving new logic into the existing mess. Instead you write the new behavior as a separate method — the "sprout" — develop it test-first, and then call it from the legacy method at a single, well-chosen point. The old code stays largely untouched, while the new code is born clean and covered by tests. Over time these sprouts become the seams that let you tame the legacy code.

## When This Technique Is Useful

- The change is clearly new behavior that can be expressed as its own method.
- The surrounding method is hard or risky to test, but you still want the new logic under test.
- You want to make progress now without first paying for a large refactoring.
- The new logic needs only a handful of inputs (good candidates for parameters).
- You want a clear, reviewable boundary between "old untested code" and "new tested code".

## When This Technique Is NOT Useful (or a Code Smell)

- The new logic is deeply entangled with the existing code and cannot be cleanly extracted.
- You find yourself sprouting method after method into the same monster, which only hides the rot.
- The host method is small and already easy to test — just edit it directly.
- The sprout would need so many parameters that the call site becomes unreadable.
- The change is really a modification of existing behavior, not genuinely new behavior.

## Steps to Apply the Technique

1. **Identify where in the method you need the change.** Find the single point where the new behavior belongs.

   ```ts
   for (const employee of employees) {
     // <-- validation belongs here, before payment
     const gateway = new PaymentGateway();
     gateway.send(employee.accountNumber, netPay);
   }
   ```

2. **Write a commented-out call to a new method that will do the work.** Sketch the call before it exists.

   ```ts
   // const invalid = this.collectInvalidAccountHolders(employees);
   ```

3. **Determine which local variables it needs and make them parameters.** Pass in exactly what the sprout reads.

   ```ts
   // collectInvalidAccountHolders(employees: Employee[]): ...
   ```

4. **Determine whether it must return a value.** Here the names of skipped employees must flow back out.

   ```ts
   collectInvalidAccountHolders(employees: Employee[]): string[];
   ```

5. **Develop the sprout method test-first (TDD).** Write failing tests, then the smallest implementation that passes.

   ```ts
   it('flags empty account numbers as invalid', () => {
     // arrange employees, act, assert names are collected
   });
   ```

6. **Uncomment the call.** Wire the now-tested sprout into the legacy method.

   ```ts
   const invalid = this.collectInvalidAccountHolders(employees);
   ```

## The Kata

### Background

A payroll system computes each employee's net pay from a hardcoded tax table and sends the payment to their bank account through a payments provider. It runs as a batch over a list of employees and prints a summary when it finishes.

### Legacy Code Description

`PayrollProcessor.processPayroll` is hard to test:

- It creates `new PaymentGateway()` directly inside the loop, and that gateway makes a real network call via `fetch(...)` to the payments URL. Running the method in a test would fire live HTTP requests.
- It writes a summary line with `console.log`, an untestable side effect on stdout.
- The tax brackets are hardcoded inline, so there are no seams to substitute behavior.

Because of these dependencies, you should not try to test `processPayroll` as a whole. Instead, grow the new behavior as a sprouted, independently testable method.

### Your Task

Before sending any payment, validate the employee's bank account number: it must be non-empty, contain numeric characters only, and be between 6 and 34 digits long. Skip employees with an invalid account (do not send their payment) and collect their names into a `string[]`. Change `processPayroll`'s return type from `void` to `string[]` so it returns the names of the skipped employees. The validation must live in a sprouted function or method that is tested in isolation.

### Acceptance Criteria

- A new sprouted function/method performs the account-number validation and is covered by its own unit tests.
- `processPayroll` returns a `string[]` containing the names of employees whose account number is invalid.
- Employees with invalid account numbers are skipped and never passed to the payment gateway.
- Employees with valid account numbers are processed exactly as before.
- The build stays green: `npm test` and `npm run typecheck` both pass.

### Hints

- Start with the skeleton in `tests/payrollProcessor.test.ts`: replace the `it.todo` with real, failing tests for the sprout, then make them pass.
- Test the sprout directly — it should take plain inputs and return data, with no `PaymentGateway` or `console.log` involved.
- A regular expression such as `/^\d{6,34}$/` captures "numeric only, 6 to 34 digits" in one check.
