// LEGACY CODE — do not refactor the parts you are not told to touch.
//
// This generator is hard to test: its constructor opens a real database
// connection and reads configuration straight from process.env. There is no
// seam to substitute a fake. We will leave all of that in place and instead
// SPROUT a new, separately-testable class for the new behaviour.

// Minimal ambient declaration so `process.env` type-checks without pulling in
// the full @types/node package. The real Node runtime supplies `process`.
declare const process: { env: Record<string, string | undefined> };

export interface DepartmentRow {
  department: string;
  manager: string;
  profit: number;
  expenses: number;
}

/**
 * A hard-to-test external dependency: a real database connection.
 * Constructing it actually opens a network socket to the database server,
 * which is exactly why code that `new`s it inline is painful to unit test.
 */
export class DatabaseConnection {
  private readonly connected: boolean;

  constructor(host: string, port: number, database: string, user: string, password: string) {
    // In a real system this opens a live TCP connection to the DB server.
    // (Simulated here — but treat it as a genuine side effect.)
    if (!host || !database) {
      throw new Error('Cannot connect to database: missing host or database name');
    }
    this.connected = true;
    // e.g. driver.connect(`postgres://${user}:${password}@${host}:${port}/${database}`)
    void password;
    void port;
    void user;
  }

  query(sql: string): DepartmentRow[] {
    if (!this.connected) {
      throw new Error('Not connected to the database');
    }
    // In a real system this round-trips to the database and returns live rows.
    void sql;
    return [];
  }
}

export class QuarterlyReportGenerator {
  private readonly connection: DatabaseConnection;
  private readonly quarter: string;

  constructor() {
    // Tight coupling to an external dependency, created right here.
    const host = process.env.DB_HOST ?? 'localhost';
    const port = Number(process.env.DB_PORT ?? '5432');
    const database = process.env.DB_NAME ?? 'reporting';
    const user = process.env.DB_USER ?? 'reporter';
    const password = process.env.DB_PASSWORD ?? '';

    this.connection = new DatabaseConnection(host, port, database, user, password);
    this.quarter = process.env.REPORT_QUARTER ?? 'Q1';
  }

  generate(): string {
    const rows = this.connection.query(
      `SELECT department, manager, profit, expenses FROM financials WHERE quarter = '${this.quarter}'`,
    );

    let html = `<table>`;
    // TODO (kata): a header row produced by the sprouted
    // QuarterlyReportTableHeader class must be inserted here, e.g.:
    // html += new QuarterlyReportTableHeader().generate();
    for (const row of rows) {
      html += `<tr>`;
      html += `<td>${row.department}</td>`;
      html += `<td>${row.manager}</td>`;
      html += `<td>${row.profit}</td>`;
      html += `<td>${row.expenses}</td>`;
      html += `</tr>`;
    }
    html += `</table>`;
    return html;
  }
}
