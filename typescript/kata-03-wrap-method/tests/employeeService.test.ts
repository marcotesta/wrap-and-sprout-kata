import { describe, it } from 'vitest';

describe('promotion event publishing (wrapped method)', () => {
  it.todo('publishes a PromotionEvent for the employee and new title');
  // Drive the NEW publishing method test-first, in isolation. Don't call
  // promote(...) (it hits the database) — test the new method directly: inject an
  // ObservableEventBus and assert on bus.publishedEvents(). `new EmployeeService()`
  // is cheap, so no mocks or subclassing needed.
  //
  // import { EmployeeService } from '../src/employeeService';
  // import { ObservableEventBus } from './observableEventBus';
  //
  // it('publishes a PromotionEvent', () => {
  //   const bus = new ObservableEventBus();
  //   new EmployeeService().publishPromotionEvent(bus, 'E-1', 'Senior Engineer');
  //   // assert bus.publishedEvents() holds one PromotionEvent for 'E-1' / 'Senior Engineer'
  // });
});
