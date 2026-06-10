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

4. **Develop the new behaviour test-first and call it from the new method.** Write the test for the new concern, then add the minimal code to the wrapper.

   ```ts
   promote(employeeId: string, newTitle: string): void {
     this.executePromotion(employeeId, newTitle);
     this.eventBus.publish(/* PromotionEvent */);
   }
   ```

   Keep the signature of `promote` identical at every step so no caller has to change.

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

Make every **successful** promotion publish a `PromotionEvent` to an `EventBus` created with `new EventBus()`, using **Wrap Method**:

1. Rename the existing `promote` to a private `executePromotion` (keep its body unchanged).
2. Add a new public `promote(employeeId: string, newTitle: string): void` wrapper with the identical signature.
3. The wrapper calls `executePromotion` first, then publishes the `PromotionEvent`.

**Added complexity:** if `executePromotion` throws a `PromotionError` (a rule fails) the event must **not** be published. Order the wrapper's calls so that `publish` runs only after the wrapped call succeeds — let the error propagate before reaching the publish step.

### Acceptance Criteria

- The public signature `promote(employeeId: string, newTitle: string): void` is unchanged; existing callers compile without edits.
- A `PromotionEvent` is published exactly once after a successful promotion.
- No event is published when `executePromotion` throws a `PromotionError`.
- The original promotion logic still lives in `executePromotion` and is not modified.
- `npm run typecheck` and `npm test` both pass.

### Hints

- Put the `this.executePromotion(...)` call before `this.eventBus.publish(...)`; a thrown error short-circuits the wrapper before publish runs.
- You do not need a real database to test the wrapper — drive the test through a `promote` call that fails validation early to assert the "no publish" path, and inject/observe the `EventBus` to assert publishing.
- Keep the wrapper tiny: delegate, then publish. All the gnarly rules stay in `executePromotion`.
