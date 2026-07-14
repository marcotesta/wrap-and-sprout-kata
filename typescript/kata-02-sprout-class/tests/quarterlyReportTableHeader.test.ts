import { describe, it } from 'vitest';

describe('QuarterlyReportTableHeader (sprouted class)', () => {
  it.todo('renders localised column titles based on the language code');
  // The header is localised: its column titles depend on the language code
  // passed in by QuarterlyReportGenerator. Drive that logic out test-first.
  //
  // Examples (uncomment and implement test-first):
  // it('renders English column titles', () => {
  //   const header = new QuarterlyReportTableHeader('en');
  //   expect(header.generate()).toBe(
  //     '<tr><th>Department</th><th>Manager</th><th>Profit</th><th>Expenses</th></tr>',
  //   );
  // });
  //
  // it('renders Italian column titles', () => {
  //   ....
  // });
  //
  // it('falls back to English for an unknown language', () => {
  //   ....
  // });
});
