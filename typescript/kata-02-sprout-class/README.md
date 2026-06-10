# Kata 02 — Sprout Class

Practice Michael Feathers' **Sprout Class** technique by adding new behaviour to an untestable legacy report generator without unravelling its tangled dependencies.

## Context

In *Working Effectively with Legacy Code* (Ch. 6) Feathers asks: how do you add a feature to code you cannot easily get under test? Sometimes the method you must change lives inside a class that is itself almost impossible to instantiate in a test — it builds heavyweight collaborators in its constructor, reaches into global state, or talks to the network. Rather than fight that class, you **sprout a new class** that holds the new logic. The new class is small, dependency-free, and fully test-driven, and the legacy class merely creates an instance and calls it. The result keeps the new code clean and tested even when the surrounding code stays stubbornly legacy.

## When This Technique Is Useful

- The change is a coherent chunk of behaviour that is natural to name and own as its own class.
- The host class is hard to instantiate (heavy constructor, real DB/network, global config) so you cannot easily Sprout a *Method* and test it there.
- You want the new logic under test today, without first paying for a large refactor of the legacy class.
- The new responsibility is reusable or likely to grow, so a dedicated class earns its keep.
- You want to start carving a seam: the sprouted class can later become an injected collaborator.

## When This Technique Is NOT Useful (or a Code Smell)

- The new logic is trivial and the host class *is* testable — a **Sprout Method** would suffice and a whole new class is overkill.
- You can already get the host class under test cheaply; then refactor in place instead of sprouting.
- The sprout would need so much of the host's private state that the "class" is really just a disguised method with awkward plumbing.
- Sprouting becomes a habit that scatters one concept across many tiny classes, hiding the real design.
- You are using it to avoid ever addressing the legacy dependencies — sprouting should reduce, not excuse, the tech debt.

## Steps to Apply the Technique

1. **Identify where you need the change.** Find the exact spot in the legacy code where the new behaviour belongs.

   ```ts
   generate(): string {
     let html = `<table>`;
     // <-- the new header row needs to appear here
     for (const row of rows) { /* ... */ }
   }
   ```

2. **Think of a good class name; write commented-out code creating an instance and calling a method in that spot.**

   ```ts
   let html = `<table>`;
   // html += new QuarterlyReportTableHeader().generate();
   ```

3. **Determine which local variables it needs and pass them via the constructor.** (Here the header is static, so none are required; if it depended on, say, the quarter, you would pass it in.)

   ```ts
   // new QuarterlyReportTableHeader(/* e.g. this.quarter */)
   ```

4. **Determine whether it must return a value; if so add a method that supplies it and a call to receive it.** The header is needed as a string, so the sprout exposes `generate(): string` whose result is appended.

   ```ts
   // const headerHtml = new QuarterlyReportTableHeader().generate();
   // html += headerHtml;
   ```

5. **Develop the sprout class test-first.** Write the failing test in `tests/`, then implement `QuarterlyReportTableHeader` until it passes.

   ```ts
   it('generates a header row containing all four columns', () => {
     expect(new QuarterlyReportTableHeader().generate()).toContain('<th>Department</th>');
   });
   ```

6. **Uncomment the creation/use lines.** Wire the now-tested sprout into the legacy `generate()` and run the build.

   ```ts
   let html = `<table>`;
   html += new QuarterlyReportTableHeader().generate();
   ```

## The Kata

### Background

A quarterly reporting system produces an HTML financial report for each department: profit and expenses per manager, rendered as an HTML `<table>`.

### Legacy Code Description

`QuarterlyReportGenerator` in `src/quarterlyReportGenerator.ts` is hard to test. Its constructor directly `new`s a real `DatabaseConnection` (which opens a live connection) and reads configuration from `process.env`. There is no seam to inject a fake, so instantiating it in a unit test would hit the real database. Its `generate()` method queries the connection and builds the table rows, but currently emits **no header row**.

### Your Task

Add a header row to the table without untangling the legacy constructor. Create a NEW, separately-testable class `QuarterlyReportTableHeader` with a single `generate(): string` method (built test-first) that returns exactly:

```html
<tr><th>Department</th><th>Manager</th><th>Profit</th><th>Expenses</th></tr>
```

Then call it from `QuarterlyReportGenerator.generate()` so the header appears immediately after the opening `<table>` tag.

### Acceptance Criteria

- A new class `QuarterlyReportTableHeader` exists with a single `generate(): string` method.
- `QuarterlyReportTableHeader.generate()` returns exactly `<tr><th>Department</th><th>Manager</th><th>Profit</th><th>Expenses</th></tr>`.
- The sprouted class is covered by tests that were written before its implementation (TDD).
- `QuarterlyReportGenerator.generate()` calls the sprouted class and inserts the header after `<table>`.
- `npm run typecheck` and `npm test` both pass; the legacy constructor is left untouched.

### Hints

- You never need to instantiate `QuarterlyReportGenerator` (and thus the database) to test the header — that is the whole point of sprouting.
- Start from the `it.todo` in `tests/quarterlyReportTableHeader.test.ts`: turn it into a real failing test first, then implement.
- Keep `QuarterlyReportTableHeader` dependency-free — no constructor arguments are needed for a static header.
