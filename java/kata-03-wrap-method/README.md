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

Every **successful** promotion must now publish a `PromotionEvent` to the `EventBus`. `EventBus` is an interface with two implementations: `RealEventBus` (production — its `publish` performs a genuine side effect) and, under `src/test`, `ObservableEventBus` (test-only — it records what was published). Apply **Wrap Method**:

1. Rename the existing `promote(String, String)` to a `private executePromotion(String, String)` (leave its body unchanged).
2. Create a new public `promote(String, String)` with the identical signature that calls `executePromotion` and then publishes the event.
3. Put the publishing in its own small method that takes an `EventBus` parameter, so you can develop it **test-first**. In the wrapper, inject a `new RealEventBus()`; in the tests, inject a `new ObservableEventBus()`.

**The behaviour to grow (test-first):** the published event carries a `newLeadership` flag. It is `true` when the new title names a **leadership role** — `newTitle` starts (case-insensitively) with `Head`, `Chief`, or `Manager` — and `false` otherwise. This small rule is the logic you drive out with tests, one branch at a time.

**Added complexity (ordering):** if `executePromotion` throws a `PromotionException`, the event must **NOT** be published. Put the publish call *after* the wrapped call and let the exception propagate: a promotion that fails never reaches the publish line. This is a property of how you order the wrapper, not something you unit-test through the database.

**How to test it (test-first):** do **not** call `promote` in your tests — that runs the legacy code and opens the production database. The new publishing method is self-contained: create an `ObservableEventBus`, pass it in, call the method directly, and assert on its `publishedEvents()`. Constructing `EmployeeService` is cheap (only `promote`/`executePromotion` touch the DB), so you can `new EmployeeService()` in a test and exercise the new method with no database, mocks, or subclassing.

### Acceptance Criteria

- The public `promote(String, String)` keeps its exact original name, parameters, and `throws PromotionException` clause; no caller needs to change.
- The original promotion logic is preserved verbatim inside a `private executePromotion(...)` method.
- The new publishing behaviour lives in its own method (public or package-private) that takes an `EventBus` parameter; the wrapper injects a `RealEventBus`, and the tests inject an `ObservableEventBus`.
- The new method is covered by tests written before its implementation (TDD), with **no database access** — the tests never call `promote` and never trigger `EmployeeRepository.getInstance()`.
- In the wrapper the publish call sits *after* `executePromotion(...)`, so a successful promotion publishes exactly one `PromotionEvent` and a failing one publishes none.
- The published `PromotionEvent` carries a `newLeadership` flag derived from `newTitle` (true when it starts with `Head`, `Chief`, or `Manager`, case-insensitive), grown test-first with a test per branch.

### Hints

- Order matters: call `executePromotion(...)` *first*; publish only on the line after it returns. Because a thrown `PromotionException` skips the rest of the method, placing the publish last gives you "no event on failure" for free — no try/catch needed.
- Make the new method testable by design: accept an `EventBus` parameter instead of constructing one inside it. The wrapper injects a `RealEventBus`; a test injects an `ObservableEventBus` and reads back its `publishedEvents()`.
- Keep the new method package-private so the same-package test can call it directly. You do **not** need to call `promote`, mock the repository, or subclass anything — just test the method on a plain `new EmployeeService()`.
- The `newLeadership` rule is a case-insensitive prefix check against `Head`/`Chief`/`Manager`: write one test for a leadership title, one for a non-leadership title, and one proving the match ignores case.

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
   Add a small, well-named method for the new behaviour and invoke it from the wrapper. Make it testable by passing in what it needs — here, the `EventBus` — instead of constructing collaborators inside it. In the wrapper, inject the production `RealEventBus`. Drive it with tests *before* wiring it in.
   ```java
   public void promote(String employeeId, String newTitle) throws PromotionException {
       executePromotion(employeeId, newTitle);
       publishPromotionEvent(new RealEventBus(), employeeId, newTitle);
   }

   void publishPromotionEvent(EventBus eventBus, String employeeId, String newTitle) {
       boolean newLeadership = /* true when newTitle names a leadership role — see Your Task */;
       eventBus.publish(new PromotionEvent(employeeId, newTitle, newLeadership));
   }
   ```

   **Preserve the original signature.** The public `promote(String, String)` must keep the same name, parameters, return type, and `throws` clause so every existing caller compiles and behaves as before.

5. **Test the new method in isolation (test-first).**
   Never call `promote` in a test — it would run `executePromotion` and hit the database. The new method is self-contained: hand it an `ObservableEventBus`, call it directly, and assert on what it published.
   ```java
   @Test
   void setsLeadershipFlagForLeadershipTitle() {
       ObservableEventBus bus = new ObservableEventBus();
       new EmployeeService().publishPromotionEvent(bus, "E-1", "Head of Sales");
       // assert bus.publishedEvents() holds one PromotionEvent whose newTitle is
       // "Head of Sales" and whose newLeadership flag is true
   }
   // then, test-first, add the other branches:
   //   "Senior Engineer" -> newLeadership false
   //   "chief of staff"  -> newLeadership true (case-insensitive)
   ```
