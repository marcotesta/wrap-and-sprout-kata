# Wrap & Sprout Kata

A hands-on kata repository for practising four dependency-breaking techniques from
Michael C. Feathers' *Working Effectively with Legacy Code* — specifically Chapter 6,
*"I Don't Have Much Time and I Have to Change It"*.

Each kata gives you a realistic, **intentionally hard-to-test** legacy class and asks
you to add a new feature. The catch: you must add that feature in a way that keeps the
*new* code separately and easily testable, **without** first untangling the existing
code's dependencies. That is the whole point of these techniques — they let you make a
change safely when you don't have time to get the surrounding legacy code under test.

## The Four Techniques

### Sprout Method
When you need to add new behaviour to an existing method, write that behaviour in a new
method instead of inline. Call the new method from the old one. The new method is
developed test-first and lives independently of the legacy method's hidden dependencies.
Use it when the new logic is a clean, self-contained computation.

### Sprout Class
Like Sprout Method, but the new behaviour goes into a brand-new class rather than a new
method. Use it when the new responsibility is large enough to deserve its own class, or
when the host class is so entangled that you cannot even add a testable method to it
without dragging in its dependencies.

### Wrap Method
When you need behaviour to run *around* an existing method (before or after it), rename
the original method and create a new method with the original name and signature. The new
method calls the renamed original plus your new, test-driven behaviour. Use it for
cross-cutting concerns where you must not touch the original logic.

### Wrap Class
The class-level equivalent of Wrap Method — a Decorator. Create a new class that takes the
original as a constructor argument and delegates to it, adding new behaviour around one or
more methods. Use it when the wrapping concern is too big for a single method, or applies
across several methods.

## The Katas

| # | Technique | Java | TypeScript |
|---|-----------|------|------------|
| 01 | Sprout Method | [java/kata-01-sprout-method](java/kata-01-sprout-method) | [typescript/kata-01-sprout-method](typescript/kata-01-sprout-method) |
| 02 | Sprout Class | [java/kata-02-sprout-class](java/kata-02-sprout-class) | [typescript/kata-02-sprout-class](typescript/kata-02-sprout-class) |
| 03 | Wrap Method | [java/kata-03-wrap-method](java/kata-03-wrap-method) | [typescript/kata-03-wrap-method](typescript/kata-03-wrap-method) |
| 04 | Wrap Class | [java/kata-04-wrap-class](java/kata-04-wrap-class) | [typescript/kata-04-wrap-class](typescript/kata-04-wrap-class) |

Each kata folder contains its own `README.md` with full instructions, the legacy code, a
skeleton test file for you to fill in, and a self-contained build.

## Important: the legacy code is hard to test on purpose

In every kata the existing class is wired to real dependencies — services instantiated
directly with `new`, singletons reached through static accessors, environment-variable
reads, console output, and so on. **Do not** refactor those dependencies away (that would
take more time than you have, and it defeats the exercise). Your job is to introduce the
new feature so that the new code is separately testable while leaving the legacy code's
structure alone.

## Suggested Reading

Michael C. Feathers, *Working Effectively with Legacy Code* — **Chapter 6: "I Don't Have
Much Time and I Have to Change It."**
