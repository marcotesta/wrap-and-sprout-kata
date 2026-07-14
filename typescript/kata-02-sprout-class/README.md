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
   // html += new QuarterlyReportTableHeader(this.languageCode).generate();
   ```

3. **Determine which local variables the new class needs and pass them via the constructor.** The header is localised, so it depends on the report's language. That value already lives in the legacy class (read from the environment in the constructor) — gather it and pass it into the sprout.

   ```ts
   // The header's column titles depend on the configured language:
   // html += new QuarterlyReportTableHeader(this.languageCode).generate();
   ```

4. **Determine whether it must return a value; if so add a method that supplies it and a call to receive it.** The header is needed as a string, so the sprout exposes `generate(): string` whose result is appended.

   ```ts
   // const headerHtml = new QuarterlyReportTableHeader(this.languageCode).generate();
   // html += headerHtml;
   ```

5. **Develop the sprout class test-first (TDD).** Write the failing tests — one per language plus the fallback — then the minimal implementation that satisfies them.

   ```ts
   it('renders English column titles', () => {
     const header = new QuarterlyReportTableHeader('en');
     expect(header.generate()).toBe(
       '<tr><th>Department</th><th>Manager</th><th>Profit</th><th>Expenses</th></tr>',
     );
   });

   it('renders Italian column titles', () => {
     // ...
     // '<tr><th>Reparto</th><th>Responsabile</th><th>Profitto</th><th>Spese</th></tr>'
   });

   it('falls back to English for an unknown language', () => {
     // ...
   });
   ```

6. **Uncomment the creation/use lines.** Wire the now-tested sprout into the legacy `generate()` and run the build.

   ```ts
   let html = `<table>`;
   html += new QuarterlyReportTableHeader(this.languageCode).generate();
   ```

## The Kata

### Background

A quarterly reporting system produces an HTML financial report for each department: profit and expenses per manager, rendered as an HTML `<table>`. The report is localised: a configured language decides which language the labels are written in.

### Legacy Code Description

`QuarterlyReportGenerator` in `src/quarterlyReportGenerator.ts` is hard to test. Its constructor directly `new`s a real `DatabaseConnection` (which opens a live connection) and reads configuration from `process.env` — including the report language from `REPORT_LANGUAGE`. There is no seam to inject a fake, so instantiating it in a unit test would hit the real database. Its `generate()` method queries the connection and builds the table rows, but currently emits **no header row**.

The report is already localised, but in the worst possible way: the title, heading, and footer messages are translated by **hardcoded, inline logic inside `generate()`** (anything that is not Italian falls back to English). That inline translation is untestable from here for the very same reason — you cannot instantiate the class. This is the wart you must *not* copy when you add the header: instead of piling more inline localisation into the legacy method, you grow the header's localisation as a clean, independently tested sprout class.

### Your Task

The HTML table must now include a header row whose **column titles are written in the report's configured language**. English (`"en"`) and Italian (`"it"`) must be supported, and any unknown, blank, or `undefined` language must fall back to English.

```
en → <tr><th>Department</th><th>Manager</th><th>Profit</th><th>Expenses</th></tr>
it → <tr><th>Reparto</th><th>Responsabile</th><th>Profitto</th><th>Spese</th></tr>
```

Rather than adding this logic inside the untestable `QuarterlyReportGenerator`, **sprout a new class** named `QuarterlyReportTableHeader`. It takes the language code as a constructor argument and exposes a single `generate(): string` method that returns the localised header row. Develop it test-first in `tests/quarterlyReportTableHeader.test.ts`, then call it from `QuarterlyReportGenerator.generate()` — passing the language read in the legacy constructor — so the header appears immediately after the opening `<table>` tag and before the first data row.

### Acceptance Criteria

- A new class `QuarterlyReportTableHeader` exists, taking a language code as a constructor argument and exposing a single `generate(): string` method.
- `generate()` returns the header row with column titles localised to the given language: English and Italian are supported, and unknown / blank / `undefined` languages fall back to English.
- The language-selection logic (including the fallback) is driven out **test-first (TDD)**, with tests covering each supported language and the fallback, and with no dependency on `DatabaseConnection` or the environment.
- `QuarterlyReportGenerator.generate()` passes the language it read from the environment into the new class, so the localised header row sits between `<table>` and the first `<tr><td>...</td></tr>` data row.
- `npm run typecheck` and `npm test` both pass; the legacy constructor is left untouched (no dependency injection or factory added).

### Hints

- The sprout's only input is the language code — pass it as a constructor argument. Keep the class as small as the task allows while still owning the language logic.
- Start from the `it.todo` in `tests/quarterlyReportTableHeader.test.ts`: write the tests first — one asserting the exact English row, one for Italian, and one proving an unknown language falls back to English. Watch them fail, then write the minimal `generate()` that passes.
- A small map or switch from language code to the four column titles is enough — resist adding languages or configuration the acceptance criteria don't ask for.
- You never need to instantiate `QuarterlyReportGenerator` (and thus the database) to test the header — that is the whole point of sprouting.
