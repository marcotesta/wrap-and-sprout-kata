# Kata 03 — Wrap Method

Add new behaviour to an existing method without changing its callers, by wrapping the original logic behind a method of the same name.

## Context

Wrap Method is one of the techniques Michael Feathers describes in *Working Effectively with Legacy Code* (Ch. 6). You reach for it when you need to add behaviour that should run alongside an existing method, but the existing method is long, tangled, or hard to safely modify in place. Instead of editing the method body, you rename the original, give the new behaviour its own method, and create a wrapper that keeps the original public name and signature. Callers never notice the change — they keep calling the same method — while the new behaviour lives in a small, testable unit. It is the technique of choice when the new behaviour is *temporally coupled* (must run before/after the old behaviour) but not logically intertwined with it.

## When This Technique Is Useful

- You need to add behaviour that runs before or after an existing method, without entangling it in the old logic.
- The original method is long or risky to edit directly, so you want to leave its body untouched.
- The new behaviour is genuinely separate (logging, notifications, events, metrics) and deserves its own name and tests.
- You want to keep the original method's signature stable so existing callers require zero changes.
- You want to develop the new behaviour test-first in isolation.

## When This Technique Is NOT Useful (or a Code Smell)

- The new behaviour is deeply intertwined with the existing logic and cannot be cleanly separated — Extract Method may fit better.
- You find yourself wrapping the same method many times, creating onion-layers of indirection that obscure intent.
- The original method is already small and easy to change safely — just edit it.
- The new behaviour must run conditionally based on internals of the old method (a wrapper only sees inputs/outputs/exceptions, not internal state).
- Wrapping is being used to avoid understanding the legacy method rather than to manage genuine risk.

## The Kata

### Background

You are working on the HR system of a mid-sized company. `EmployeeService` handles promotions, demotions and transfers. It is a long-lived class that many teams depend on.

### Legacy Code Description

`EmployeeService.promote(String, String)` is hard to test. It loads and saves employees through `EmployeeRepository.getInstance()` — a singleton whose constructor opens a real connection to the production HR database. There is no way to inject a fake: calling `promote` triggers a real DB round-trip. Mocking the singleton would be invasive (you would have to reach into static state). For this kata, **do not** add dependency injection for the repository. Treat the existing `promote` logic as legacy code you must not rewrite; your job is to add behaviour *around* it.

### Your Task

Every **successful** promotion must now publish a `PromotionEvent` to an `EventBus` (constructed with `new EventBus()`). Apply **Wrap Method**:

1. Rename the existing `promote(String, String)` to a `private executePromotion(String, String)` (leave its body unchanged).
2. Create a new public `promote(String, String)` with the identical signature that wraps `executePromotion`.
3. Publish the `PromotionEvent` from the wrapper.

**Added complexity:** if `executePromotion` throws a `PromotionException`, the event must **NOT** be published. You therefore have to order the wrapper's calls so the publish happens only after the wrapped call returns successfully.

### Acceptance Criteria

- The public `promote(String, String)` keeps its exact original name, parameters, and `throws PromotionException` clause; no caller needs to change.
- The original promotion logic is preserved verbatim inside a `private executePromotion(...)` method.
- A `PromotionEvent` is published to an `EventBus` after every successful promotion.
- When `executePromotion` throws `PromotionException`, no `PromotionEvent` is published and the exception propagates unchanged.
- The new event-publishing behaviour is covered by tests written test-first.

### Hints

- Order matters: call `executePromotion(...)` *first*; only publish the event on the line after it returns — never wrap the publish in a way that runs before the rules are checked or when they fail.
- Avoid catch-and-rethrow if you can: simply letting the exception escape before reaching the publish call gives you the "no event on failure" guarantee for free.
- To make the new behaviour testable without touching the database singleton, introduce a seam around event publishing (e.g. a `publishPromotionEvent(...)` method or an overridable `EventBus` factory) so a test can observe what was published.

## Steps to Apply the Technique

1. **Identify the method you need to change.**
   ```java
   public void promote(String employeeId, String newTitle) throws PromotionException {
       // ... existing legacy logic ...
   }
   ```

2. **Rename the original and create a new method with the original name and signature.**
   Make the renamed original `private` so it cannot be called from outside, and keep the *exact* original signature on the new public method.
   ```java
   private void executePromotion(String employeeId, String newTitle) throws PromotionException {
       // ... the unchanged legacy logic ...
   }

   public void promote(String employeeId, String newTitle) throws PromotionException {
       // wrapper goes here
   }
   ```

3. **Have the new method call the renamed original.**
   ```java
   public void promote(String employeeId, String newTitle) throws PromotionException {
       executePromotion(employeeId, newTitle);
   }
   ```

4. **Develop the new behaviour test-first and call it from the new method.**
   Add a small, well-named method for the new behaviour and invoke it from the wrapper. Drive it with tests *before* wiring it in.
   ```java
   public void promote(String employeeId, String newTitle) throws PromotionException {
       executePromotion(employeeId, newTitle);
       publishPromotionEvent(employeeId, newTitle);
   }

   private void publishPromotionEvent(String employeeId, String newTitle) {
       new EventBus().publish(new PromotionEvent(employeeId, /* previousTitle */ null, newTitle));
   }
   ```

   **Preserve the original signature.** The public `promote(String, String)` must keep the same name, parameters, return type, and `throws` clause so every existing caller compiles and behaves as before.
