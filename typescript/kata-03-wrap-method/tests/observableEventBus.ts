import type { EventBus } from '../src/employeeService';

// Test-only EventBus that records what was published instead of really
// publishing it. Inject it into the new publishing method under test, then
// assert on publishedEvents() — no database, no mocking framework required.
export class ObservableEventBus implements EventBus {
  private readonly events: unknown[] = [];

  publish(event: unknown): void {
    this.events.push(event);
    console.log('[ObservableEventBus] recorded:', event);
  }

  // Events published through this bus, in order — handy for assertions in tests.
  publishedEvents(): unknown[] {
    return this.events;
  }
}
