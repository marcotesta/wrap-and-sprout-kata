import { describe, it } from 'vitest';

describe('TimingOrderProcessor (wrap class / decorator)', () => {
  it.todo('logs a warning when placeOrder takes longer than 2000ms');
  it.todo('does not log a warning when placeOrder is fast');
  // Drive the wrapper test-first — no real I/O, no console spying:
  //  - a fake IOrderProcessor (the interface you extract) whose placeOrder you
  //    can make slow or fast on demand,
  //  - a recording Logger you implement here: a tiny class implementing the
  //    Logger interface that stores each warn() message so you can assert on it.
});
