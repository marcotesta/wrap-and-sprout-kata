# Kata 04 — Wrap Class

Add behaviour around a legacy class without touching it, using the Wrap Class (Decorator) technique.

## Context

In *Working Effectively with Legacy Code* (Ch. 6), Michael Feathers describes **Wrap Class** as a way to add new behaviour to a system without modifying the existing code that already works. Instead of editing a class to bolt on a new responsibility, you create a new class that holds the original (or a contract it satisfies) and delegates to it, layering the extra behaviour around the calls you care about. This is the classic Decorator pattern applied as a legacy-code tactic: the wrapper presents the same interface as the original, so callers can be redirected to it transparently. It keeps cross-cutting concerns (timing, logging, retries, caching, metrics) out of the core domain logic and away from a class that may be too risky or too costly to change. The trade-off is that wrapping only works cleanly when the original code already exposes — or can be made to expose — an interface to delegate through.

## When This Technique Is Useful

- You need to add a cross-cutting concern (timing, logging, auditing, retry, caching) around existing behaviour without changing it.
- The original class works and is risky or expensive to modify directly.
- The new behaviour is conceptually separate from the class's core responsibility and shouldn't pollute it.
- You want the new behaviour to be optional — enabled only where it is wired in.
- The original class already implements (or can cheaply be made to implement) an interface you can delegate through.

## When This Technique Is NOT Useful (or a Code Smell)

- The new behaviour is genuinely part of the class's core domain logic — wrapping just hides it.
- You stack many wrappers until the delegation chain becomes hard to follow.
- The interface is wide and volatile, so every change forces edits across original and wrapper.
- You reach for a wrapper to avoid fixing a class that actually needs refactoring.
- The wrapper needs to peek at the original's private internals — that's a sign the seam is in the wrong place.

## The Kata

### Background

An e-commerce platform processes customer orders: it validates them, calculates totals, discounts and tax, persists them, reserves inventory and emails a confirmation. The heart of this flow is the `OrderProcessor` class.

### Legacy Code Description

`OrderProcessor` (in `src/orderProcessor.ts`) is a large class with eight public methods. It is hard to test because it `new`s up three external services inside itself — `new OrderRepository()`, `new EmailService()`, `new InventoryService()` — all of which look like real I/O. There is no seam to substitute them, and crucially the class **implements no interface**. Properly breaking those dependencies (extracting and injecting each service) would take the better part of a day, so for this kata you must leave `OrderProcessor` untouched.

### Your Task

Every call to `placeOrder(order)` must be **timed**; if it takes longer than **2000ms**, log a warning through `Logger` (`new Logger()`). The timing and logging logic must **not** be mixed into `OrderProcessor`. Apply the **Wrap Class** technique: build a `TimingOrderProcessor` that holds an `IOrderProcessor` and decorates `placeOrder`.

Because `OrderProcessor` implements no interface, your **first** step is to **Extract an Interface** (`IOrderProcessor`) so that the original class and your wrapper share a contract: `class OrderProcessor implements IOrderProcessor` and `class TimingOrderProcessor implements IOrderProcessor`. Only the interface extraction may touch the original file; the behaviour of `OrderProcessor` stays the same.

### Acceptance Criteria

- An `IOrderProcessor` interface is extracted and implemented by `OrderProcessor` (its behaviour unchanged otherwise).
- A `TimingOrderProcessor` implements `IOrderProcessor`, takes an `IOrderProcessor` in its constructor, and delegates every method.
- `placeOrder` is timed; a warning is logged via `Logger` only when it exceeds 2000ms, and not when it is fast.
- No timing or logging code lives inside `OrderProcessor`.
- `npm run typecheck` and `npm test` both pass.

### Hints

- Start by extracting `IOrderProcessor` from `OrderProcessor` — without a shared interface the wrapper has nothing to delegate through.
- Write the test first: inject a fake/slow `IOrderProcessor` and a spy `Logger` so you control timing without real I/O.
- Keep `TimingOrderProcessor` thin — it should only measure and (conditionally) warn; all real work stays behind `this.wrapped`.

## Steps to Apply the Technique

1. **Identify the behaviour to add.** Be precise about which method(s) the new concern wraps.

   ```ts
   // We want to TIME placeOrder and warn if it is slow — nothing else changes.
   ```

2. **If the original class has no interface, first Extract an Interface.** The original and the wrapper must share a contract.

   ```ts
   export interface IOrderProcessor {
     placeOrder(order: Order): void;
     // ...the other public methods callers depend on
   }

   export class OrderProcessor implements IOrderProcessor {
     /* unchanged */
   }
   ```

3. **Create a wrapper class taking the interface in its constructor and delegating all methods.**

   ```ts
   export class TimingOrderProcessor implements IOrderProcessor {
     constructor(private readonly wrapped: IOrderProcessor) {}

     placeOrder(order: Order): void {
       this.wrapped.placeOrder(order);
     }
     // delegate the remaining methods to this.wrapped
   }
   ```

4. **Add the new behaviour around the target method, test-first.**

   ```ts
   placeOrder(order: Order): void {
     const start = Date.now();
     this.wrapped.placeOrder(order);
     const elapsed = Date.now() - start;
     if (elapsed > 2000) {
       new Logger().warn(`placeOrder took ${elapsed}ms for ${order.id}`);
     }
   }
   ```

5. **Instantiate the wrapper where the new behaviour is needed.**

   ```ts
   const processor: IOrderProcessor = new TimingOrderProcessor(new OrderProcessor());
   processor.placeOrder(order);
   ```
