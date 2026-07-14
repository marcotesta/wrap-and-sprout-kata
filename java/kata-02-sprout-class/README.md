# Kata 02 — Sprout Class

Add new behaviour to untestable legacy code by growing it inside a brand new, independently testable class.

## Context

The **Sprout Class** technique comes from Michael Feathers' *Working Effectively with Legacy Code* (Chapter 6). You reach for it when you have to add new functionality to a class that is so entangled with hard dependencies that you cannot get it under test in any reasonable amount of time. Instead of writing the new code inside that untestable class, you create a brand new class for the new behaviour, develop it test-first in full isolation, and then call it from the legacy code. The legacy class stays as messy as it was, but the *new* logic is born clean, covered by tests, and easy to reason about. Sprout Class is the heavier sibling of Sprout Method: you choose a class (not just a method) when the new work needs its own state, when the host class is impossible to instantiate in a test, or when the new responsibility deserves a name of its own.

## When This Technique Is Useful

- You must add new behaviour to a class that cannot be instantiated under test (its constructor reaches out to databases, networks, the file system, or the environment).
- The new behaviour is cohesive enough to deserve its own name and identity.
- You want the new code covered by tests immediately, without first paying the cost of breaking every dependency in the legacy class.
- The new logic needs its own state or a few collaborators, which would be awkward to bolt onto an existing method.
- You want to make progress today while leaving the larger refactoring of the legacy class for a safer moment.

## When This Technique Is NOT Useful (or a Code Smell)

- A simple **Sprout Method** would do the job — extracting a single static or instance method is lighter than introducing a whole class.
- You create a class with a single trivial method and no state just for the sake of it, adding ceremony without value.
- You could cheaply break the dependency and test the host class directly; sprouting then just hides the real problem.
- Sprouting becomes a habit and the legacy class slowly turns into a hollow shell delegating to a swarm of tiny classes, with the real coupling never addressed.
- The new class needs so much of the legacy class's private state that passing it all in is more painful than fixing the seam.

## Steps to Apply the Technique

1. **Identify where you need the change.** Find the exact line in the legacy code where the new behaviour belongs.

   ```java
   html.append("<table>");
   // <-- the new header row belongs right here, before the data rows
   for (DepartmentRow row : rows) {
       // ...
   }
   ```

2. **Imagine a good class name and write the call commented-out, in place.** Pretend the class already exists and write how you would use it.

   ```java
   html.append("<table>");
   // QuarterlyReportTableHeader header = new QuarterlyReportTableHeader(languageCode);
   // html.append(header.generate());
   for (DepartmentRow row : rows) {
       // ...
   }
   ```

3. **Determine which local variables the new class needs and pass them as constructor args.** The header is localised, so it depends on the report's language. That value already lives in the legacy class (read from the environment in the constructor) — gather it and pass it into the sprout.

   ```java
   // The header's column titles depend on the configured language:
   // QuarterlyReportTableHeader header = new QuarterlyReportTableHeader(languageCode);
   ```

4. **Decide whether the sprout must return a value; if so, add a method that supplies it and a call to receive it.** Here the header must hand back an HTML fragment.

   ```java
   // String headerRow = new QuarterlyReportTableHeader(languageCode).generate();
   // html.append(headerRow);
   ```

5. **Develop the sprout class test-first (TDD).** Write the failing tests — one per language plus the fallback — then the minimal implementation that satisfies them.

   ```java
   @Test
   void shouldRenderEnglishColumnTitles() {
       QuarterlyReportTableHeader header = new QuarterlyReportTableHeader("en");
       assertEquals(
           "<tr><th>Department</th><th>Manager</th><th>Profit</th><th>Expenses</th></tr>",
           header.generate());
   }

   @Test
   void shouldRenderItalianColumnTitles() {
       // ...
       // "<tr><th>Reparto</th><th>Responsabile</th><th>Profitto</th><th>Spese</th></tr>"
   }

   @Test
   void shouldFallBackToEnglishForUnknownLanguage() {
       // ...
   }
   ```

6. **Uncomment the creation/use lines.** Wire the now-tested class into the legacy code.

   ```java
   html.append("<table>");
   html.append(new QuarterlyReportTableHeader(languageCode).generate());
   for (DepartmentRow row : rows) {
       // ...
   }
   ```

## The Kata

### Background

A quarterly reporting system produces an HTML report summarising each department's results for the quarter: department name, manager, profit, and expenses. The report is generated by `QuarterlyReportGenerator.generate()`, which pulls rows from the company database and renders them into an HTML `<table>`. The report is localised: a configured language decides which language the labels are written in.

### Legacy Code Description

`QuarterlyReportGenerator` is hard to test. Its constructor reads configuration directly from `System.getenv(...)` — including the report language from `REPORTING_LANGUAGE` — and then immediately `new`s a real `DatabaseConnection`, which opens a live JDBC connection via `DriverManager`. There is no dependency injection and no factory seam, so simply constructing the class in a unit test tries to reach an external database. You cannot easily exercise `generate()` in isolation, and you should not try to refactor all of that away just to add one small feature.

The report is already localised, but in the worst possible way: the title, heading, and footer messages are translated by **hardcoded, inline logic inside `generate()`** (anything that is not Italian falls back to English). That inline translation is untestable from here for the very same reason — you cannot instantiate the class. This is the wart you must *not* copy when you add the header: instead of piling more inline localisation into the legacy method, you grow the header's localisation as a clean, independently tested sprout class.

### Your Task

The HTML table must now include a header row whose **column titles are written in the report's configured language**. English (`"en"`) and Italian (`"it"`) must be supported, and any unknown, blank, or `null` language must fall back to English.

```
en → <tr><th>Department</th><th>Manager</th><th>Profit</th><th>Expenses</th></tr>
it → <tr><th>Reparto</th><th>Responsabile</th><th>Profitto</th><th>Spese</th></tr>
```

Rather than adding this logic inside the untestable `QuarterlyReportGenerator`, **sprout a new class** named `QuarterlyReportTableHeader`. It takes the language code as a constructor argument and exposes a single `String generate()` method that returns the localised header row. Develop it test-first in `QuarterlyReportTableHeaderTest`, then call it from `QuarterlyReportGenerator.generate()` — passing the language read in the legacy constructor — so the header appears immediately after the opening `<table>` tag and before the first data row.

### Acceptance Criteria

- A new class `QuarterlyReportTableHeader` exists, taking a language code as a constructor argument and exposing a single `String generate()` method.
- `generate()` returns the header row with column titles localised to the given language: English and Italian are supported, and unknown / blank / `null` languages fall back to English.
- The language-selection logic (including the fallback) is driven out **test-first (TDD)**, with tests covering each supported language and the fallback, and with no dependency on `DatabaseConnection` or the environment.
- `QuarterlyReportGenerator.generate()` passes the language it read from the environment into the new class, so the localised header row sits between `<table>` and the first `<tr><td>...</td></tr>` data row.
- `QuarterlyReportGenerator` is not given dependency injection or a factory; the legacy constructor is left as-is.

### Hints

- The sprout's only input is the language code — pass it as a constructor argument. Keep the class as small as the task allows while still owning the language logic.
- Write the tests first: one asserting the exact English row, one for Italian, and one proving an unknown language falls back to English. Watch them fail, then write the minimal `generate()` that passes.
- A small map or switch from language code to the four column titles is enough — resist adding languages or configuration the acceptance criteria don't ask for.
- Do not try to instantiate `QuarterlyReportGenerator` in a test; that is the whole point of sprouting — test the new class, not the legacy one.
