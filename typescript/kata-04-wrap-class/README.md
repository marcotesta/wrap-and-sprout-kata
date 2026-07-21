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

Every call to `placeOrder(order)` must be **timed**. When it takes longer than **2000ms**, log a warning through the `Logger`; when it is fast, log nothing. This timing-and-logging concern must **not** be mixed into `OrderProcessor` — its existing logic stays untouched.

Add the behaviour *around* `OrderProcessor` with the **Wrap Class** technique (a decorator), and grow it **test-first**. The mechanics should be extract an interface for the _OrderProcessor_, develop a wrapper implementation of it growing the new behaviour **test-first**, keeping the warning logs observable in tests.

### Acceptance Criteria

- `OrderProcessor` is left unchanged (aside from the interface it now shares with the wrapper).
- A decorator adds timing around `placeOrder`: it warns when the call exceeds 2000ms and stays silent when fast.
- No timing or logging code lives inside `OrderProcessor`.
- The behaviour is driven **test-first** using test doubles for the processor and the logger — no real I/O, no console spying.
- `npm run typecheck` and `npm test` pass.

### Hints

- Start by extracting `IOrderProcessor` from `OrderProcessor` — without a shared interface the wrapper has nothing to delegate through.
- Write the test first: inject a fake/slow `IOrderProcessor` and a recording `Logger` — a tiny class implementing the `Logger` interface that stores each `warn` message — then assert on the captured messages. No console spying, no real I/O.
- Keep `TimingOrderProcessor` thin — it should only measure and (conditionally) warn; all real work stays behind `this.wrapped`.

## Steps to Apply the Technique

The `Logger` is an interface with a `ConsoleLogger` implementation for production. Inject a `Logger` into the wrapper — a `ConsoleLogger` in production wiring, and in your tests a small recording `Logger` you implement, so warnings can be asserted without spying on the console.

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
     constructor(
       private readonly wrapped: IOrderProcessor,
       private readonly logger: Logger,
     ) {}

     placeOrder(order: Order): void {
       this.wrapped.placeOrder(order);
     }
     // delegate the remaining methods to this.wrapped
   }
   ```

4. **Add the new behaviour around the target method, test-first.** Warn through the *injected* `logger`, never a `new Logger()` inside the method — that is what lets a test observe it.

   ```ts
   placeOrder(order: Order): void {
     const start = Date.now();
     this.wrapped.placeOrder(order);
     const elapsed = Date.now() - start;
     if (elapsed > 2000) {
       this.logger.warn(`placeOrder took ${elapsed}ms for ${order.id}`);
     }
   }
   ```

   In the test, inject a fake `IOrderProcessor` and your own recording `Logger`, then assert on the captured warnings:

   ```ts
   class RecordingLogger implements Logger {
     readonly warnings: string[] = [];
     warn(msg: string): void {
       this.warnings.push(msg);
     }
   }
   ```

5. **Instantiate the wrapper where the new behaviour is needed.** Inject the production `ConsoleLogger` here.

   ```ts
   const processor: IOrderProcessor = new TimingOrderProcessor(new OrderProcessor(), new ConsoleLogger());
   processor.placeOrder(order);
   ```
