# Kata 03 — Wrap Method

Add new behaviour around an existing method without changing its callers, using Michael Feathers' Wrap Method technique.

## Context

In *Working Effectively with Legacy Code* (Ch. 6), Feathers describes **Wrap Method** as a way to add behaviour to an existing method when you cannot — or should not — touch its body. You rename the original method, then create a new method with the original name and signature that delegates to the renamed one and layers the new behaviour around the call. Because the public name and signature stay identical, every existing caller keeps working untouched. It is especially valuable when the original method has no tests and you want to add behaviour without risking the logic you do not understand. The new behaviour, by contrast, is written test-first and lives in fresh, well-tested code.

## When This Technique Is Useful

- The existing method has no tests and you are nervous about editing its body.
- The new behaviour is conceptually separate from what the method already does (e.g. publishing an event vs. computing a result).
- You must preserve the exact public signature so existing callers stay untouched.
- You want the new logic to be small, isolated, and easy to test on its own.
- You need to add a before/after concern (logging, events, auditing) around legacy logic.

## When This Technique Is NOT Useful (or a Code Smell)

- The new behaviour is deeply entangled with the original logic and cannot be expressed as a clean before/after step.
- You find yourself wrapping the same method many times — that signals a missing abstraction or a need for a real refactor.
- The original method is already well-tested and easy to change directly; just edit it.
- The wrapper grows its own complex branching, becoming a second hard-to-test method.
- You are using the wrapper to hide a design problem instead of addressing it.

## The Kata

### Background

You are working in an HR system that manages employees, titles, and career levels. A downstream analytics service and a notifications service both want to know whenever someone is promoted, so the team has decided that **every successful promotion must publish a `PromotionEvent` onto an `EventBus`**.

### Legacy Code Description

`EmployeeService.promote()` contains untested business logic. It is hard to test because:

- It loads and saves records through `EmployeeRepository.getInstance()`, a singleton whose `getInstance()` opens a **real database connection** and whose methods issue real SQL.
- The service grabs that singleton directly inside `promote()` — there is no seam to inject a fake.
- Changing the singleton (e.g. to make it injectable) is invasive and touches code you do not yet trust.

You should not refactor the repository for this kata. Instead, add the new event-publishing behaviour around the existing logic.

### Your Task

Make every **successful** promotion publish a `PromotionEvent` to the `EventBus`. `EventBus` is an interface with two implementations: `RealEventBus` (production — its `publish` performs a genuine side effect) and, under `tests/`, `ObservableEventBus` (test-only — it records what was published). Apply **Wrap Method**:

1. Rename the existing `promote` to a private `executePromotion` (keep its body unchanged).
2. Add a new public `promote(employeeId: string, newTitle: string): void` wrapper with the identical signature that calls `executePromotion` and then publishes the event.
3. Put the publishing in its own small method that takes an `EventBus` parameter, so you can develop it **test-first**. In the wrapper, inject a `new RealEventBus()`; in the tests, inject a `new ObservableEventBus()`.

**Added complexity (ordering):** if `executePromotion` throws a `PromotionError` (a rule fails) the event must **not** be published. Put the publish call *after* the wrapped call and let the error propagate: a promotion that fails never reaches the publish line. This is a property of how you order the wrapper, not something you unit-test through the database.

**How to test it (test-first):** do **not** call `promote` in your tests — that runs the legacy code and opens the production database. The new publishing method is self-contained: create an `ObservableEventBus`, pass it in, call the method directly, and assert on its `publishedEvents()`. Constructing `EmployeeService` is cheap (only `promote`/`executePromotion` touch the DB), so you can `new EmployeeService()` in a test and exercise the new method with no database, mocks, or subclassing.

### Acceptance Criteria

- The public signature `promote(employeeId: string, newTitle: string): void` is unchanged; existing callers compile without edits.
- The original promotion logic is preserved verbatim inside a private `executePromotion(...)` method.
- The new publishing behaviour lives in its own method that takes an `EventBus` parameter; the wrapper injects a `RealEventBus`, and the tests inject an `ObservableEventBus`.
- The new method is covered by tests written before its implementation (TDD), with **no database access** — the tests never call `promote` and never trigger `EmployeeRepository.getInstance()`.
- In the wrapper the publish call sits *after* `executePromotion(...)`, so a successful promotion publishes exactly one `PromotionEvent` and a failing one publishes none.
- `npm run typecheck` and `npm test` both pass.

### Hints

- Order matters: call `executePromotion(...)` *first*; publish only on the line after it returns. Because a thrown `PromotionError` skips the rest of the method, placing the publish last gives you "no event on failure" for free — no try/catch needed.
- Make the new method testable by design: accept an `EventBus` parameter instead of constructing one inside it. The wrapper injects a `RealEventBus`; a test injects an `ObservableEventBus` and reads back its `publishedEvents()`.
- Test the new method on a plain `new EmployeeService()`. You do **not** need to call `promote`, mock the repository, or subclass anything.
- The wrapper only sees the method's inputs, not the stored record, so it cannot know the employee's *previous* title — that is why the event's `fromTitle` is left `null`.

## Steps to Apply the Technique

1. **Identify the method to change.** Find the existing public method whose callers must keep working.

   ```ts
   class EmployeeService {
     promote(employeeId: string, newTitle: string): void { /* legacy logic */ }
   }
   ```

2. **Rename it and create a new method with the original name and signature.** Rename the original to a private method (for example `promote` -> `executePromotion`). Then declare a brand-new method that keeps the *exact* original name and signature.

   ```ts
   class EmployeeService {
     private executePromotion(employeeId: string, newTitle: string): void {
       /* unchanged legacy logic */
     }

     // New method: SAME name and signature as before.
     promote(employeeId: string, newTitle: string): void {
       // ...
     }
   }
   ```

3. **Have the new method call the renamed original.** Delegate first so behaviour is preserved.

   ```ts
   promote(employeeId: string, newTitle: string): void {
     this.executePromotion(employeeId, newTitle);
   }
   ```

4. **Develop the new behaviour test-first and call it from the new method.** Add a small, well-named method for the new behaviour and invoke it from the wrapper. Make it testable by passing in what it needs — here, the `EventBus` — instead of constructing collaborators inside it. In the wrapper, inject the production `RealEventBus`. Drive it with tests *before* wiring it in.

   ```ts
   promote(employeeId: string, newTitle: string): void {
     this.executePromotion(employeeId, newTitle);
     this.publishPromotionEvent(new RealEventBus(), employeeId, newTitle);
   }

   publishPromotionEvent(eventBus: EventBus, employeeId: string, newTitle: string): void {
     eventBus.publish({
       type: 'employee.promoted',
       employeeId,
       fromTitle: null, // the wrapper cannot know the previous title
       toTitle: newTitle,
       occurredAt: new Date(),
     });
   }
   ```

   Keep the signature of `promote` identical at every step so no caller has to change.

5. **Test the new method in isolation (test-first).** Never call `promote` in a test — it would run `executePromotion` and hit the database. The new method is self-contained: hand it an `ObservableEventBus`, call it directly, and assert on what it published.

   ```ts
   import { ObservableEventBus } from './observableEventBus';

   it('publishes a PromotionEvent', () => {
     const bus = new ObservableEventBus();
     new EmployeeService().publishPromotionEvent(bus, 'E-1', 'Senior Engineer');
     // assert bus.publishedEvents() holds exactly one PromotionEvent
     // for 'E-1' with toTitle 'Senior Engineer'
   });
   ```
