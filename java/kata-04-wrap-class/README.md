# Kata 04 — Wrap Class

Add new behaviour (timing + logging) around an existing, hard-to-test class using the Wrap Class (Decorator) technique.

## Context

In *Working Effectively with Legacy Code* (Ch. 6), Michael Feathers describes **Wrap Class** as a way to add behaviour to a system without editing the class that already holds the behaviour you care about. Instead of cracking open a large, risky class and threading new code through its methods, you create a new class with the same shape, hold an instance of the original inside it, and add your new behaviour before or after delegating to the original. This keeps the two concerns — the existing logic and the new cross-cutting behaviour — physically separate and independently testable. It is the Decorator pattern applied as a legacy-code refactoring move: same interface, wrapped instance, extra behaviour layered on. The technique shines precisely when the original class is too big or too tangled to modify safely.

## When This Technique Is Useful

- You need to add behaviour to *every* call of a method (logging, timing, retries, caching, auditing) without scattering it across the original class.
- The new behaviour is a genuinely separate concern from the original logic and you want to keep it that way.
- The original class is large, risky, or under-tested and you want to avoid editing it directly.
- You want the new behaviour to be opt-in: enabled only where you construct the wrapper, leaving other call sites untouched.
- Multiple, composable behaviours can be stacked as independent wrappers.

## When This Technique Is NOT Useful (or a Code Smell)

- The new behaviour is intrinsic to the class's core logic and belongs *inside* it — wrapping would only hide that.
- You'd end up with many tiny wrappers for one-off tweaks that nobody composes; that is indirection for its own sake.
- The method signatures change frequently, so keeping a delegating wrapper in sync becomes a maintenance burden.
- The class has a huge surface area, making full delegation tedious and error-prone (consider Extract Class or Sprout instead).
- You actually need to change existing behaviour rather than add around it — that is a refactor of the original, not a wrap.

## The Kata

### Background

We run an e-commerce platform. The `OrderProcessor` is the heart of order management: it validates an order, prices it, applies discounts and tax, persists it, reserves inventory and sends a confirmation e-mail. It works in production and we do not want to risk breaking it.

### Legacy Code Description

`OrderProcessor` is hard to test in isolation because it `new`-s **three** external collaborators inside its own constructor — `OrderRepository` (JDBC), `EmailService` (SMTP) and `InventoryService` (HTTP). Every one of them performs real I/O, so simply running `placeOrder` in a test would try to hit a database, a mail server and a warehouse API. There is no seam to swap them out. Worse, `OrderProcessor` implements **no interface**, so you cannot even substitute the whole processor with a test double. Properly breaking those collaborator dependencies (parameterising the constructor, introducing factories, etc.) would realistically take a full day and touch a lot of risky code.

### Your Task

Every call to `placeOrder(Order order)` must be **timed**. If it takes longer than **2000ms**, log a warning via `Logger` (created with `new Logger()` and its `warn(String)` method). This timing/logging must **not** be mixed into `OrderProcessor`.

Apply **Wrap Class**: build a `TimingOrderProcessor` that holds an order processor and decorates `placeOrder`. Because `OrderProcessor` implements no interface, your **first** step is to **Extract an Interface** (e.g. `IOrderProcessor`) from `OrderProcessor` so that both the original and your wrapper implement the same contract — without this, the wrapper has nothing to delegate to and nothing to substitute in tests.

### Acceptance Criteria

- An interface (e.g. `IOrderProcessor`) is extracted, and `OrderProcessor` is changed only to add `implements IOrderProcessor` (no logic changes).
- `TimingOrderProcessor implements IOrderProcessor`, takes an `IOrderProcessor` via its constructor, and delegates every method to it.
- `placeOrder` is timed and a single warning is logged via `Logger.warn` only when it exceeds 2000ms; nothing is logged when it is fast.
- The timing/logging logic lives entirely in the wrapper — `OrderProcessor` contains no timing or `Logger` code.
- The behaviour is proven by tests written test-first, driven against a fake `IOrderProcessor` and a capturing `Logger` (no real DB/SMTP/HTTP).

### Hints

- Extract the interface first; let your IDE generate it from `OrderProcessor`'s public methods, then add `implements IOrderProcessor`. The wrapper depends on the interface, never on the concrete class.
- Make `Logger` injectable into the wrapper (or extract its threshold) so a test can capture warnings; keep the default `new Logger()` for production wiring.
- Test the wrapper against a hand-written slow/fast fake `IOrderProcessor` so you never touch the real `OrderRepository`, `EmailService` or `InventoryService`.

## Steps to Apply the Technique

1. **Identify the method/behaviour you need to add.** Be precise about *where* the new code runs (before, after, or around the target method).

   ```java
   // We want to time placeOrder and warn if it is slow.
   String id = orderProcessor.placeOrder(order);
   ```

2. **If the original class has no interface, first Extract an Interface** so the original and the wrapper share a contract. The wrapper can then stand in anywhere the original is expected.

   ```java
   public interface IOrderProcessor {
       String placeOrder(Order order);
       // ...the other public methods you depend on
   }

   public class OrderProcessor implements IOrderProcessor { /* unchanged */ }
   ```

3. **Create a wrapper class that takes the interface as a constructor argument and delegates all methods to it.**

   ```java
   public class TimingOrderProcessor implements IOrderProcessor {
       private final IOrderProcessor delegate;

       public TimingOrderProcessor(IOrderProcessor delegate) {
           this.delegate = delegate;
       }

       @Override
       public String placeOrder(Order order) {
           return delegate.placeOrder(order); // plain delegation, for now
       }
       // delegate every other method too
   }
   ```

4. **Add the new behaviour around the target method, developed test-first.** Write the failing test that asserts the warning, then make it pass.

   ```java
   @Override
   public String placeOrder(Order order) {
       long start = System.currentTimeMillis();
       String result = delegate.placeOrder(order);
       long elapsed = System.currentTimeMillis() - start;
       if (elapsed > 2000) {
           logger.warn("placeOrder took " + elapsed + "ms");
       }
       return result;
   }
   ```

5. **Instantiate the wrapper where you want the new behaviour enabled.** Other call sites keep using the bare original.

   ```java
   IOrderProcessor processor = new TimingOrderProcessor(new OrderProcessor());
   processor.placeOrder(order); // now timed + logged
   ```
