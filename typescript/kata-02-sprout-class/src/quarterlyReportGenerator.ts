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
  private readonly companyName: string | undefined;
  private readonly languageCode: string | undefined;

  constructor() {
    // Tight coupling to an external dependency, created right here.
    const host = process.env.REPORTING_DB_HOST ?? 'localhost';
    const port = Number(process.env.REPORTING_DB_PORT ?? '5432');
    const database = process.env.REPORTING_DB_NAME ?? 'reporting';
    const user = process.env.REPORTING_DB_USER ?? 'reporter';
    const password = process.env.REPORTING_DB_PASSWORD ?? '';

    this.connection = new DatabaseConnection(host, port, database, user, password);
    this.companyName = process.env.REPORTING_COMPANY_NAME;
    this.languageCode = process.env.REPORTING_LANGUAGE;
  }

  generate(): string {
    const rows = this.connection.query(
      `SELECT department, manager, profit, expenses FROM financials`,
    );

    // Legacy inline localization: report is currently translated right here,
    // in this untestable class. This is exactly the kind of hardcoded logic
    // you cannot easily cover with tests from here — which is why
    // the new header row is grown as a separate, tested sprout class.
    const reportTitle = reportTitleFor(this.languageCode);

    let html = `<html>`;
    html += `<head>`;
    html += `<title>${reportTitle}</title>`;
    html += `</head>`;
    html += `<body>`;
    html += `<h1>${reportTitle}</h1>`;

    if (this.companyName !== undefined && this.companyName.trim() !== '') {
      html += `<h2>${this.companyName}</h2>`;
    }

    html += `<table>`;

    // TODO (kata): a localised header row must be produced here by a sprouted class.
    // The column titles depend on `languageCode`, so pass it to the new class.
    // Today the table jumps straight into the data rows with no header.

    for (const row of rows) {
      html += `<tr>`;
      html += `<td>${row.department}</td>`;
      html += `<td>${row.manager}</td>`;
      html += `<td>${row.profit}</td>`;
      html += `<td>${row.expenses}</td>`;
      html += `</tr>`;
    }

    html += `</table>`;

    if (rows.length === 0) {
      html += `<p>${noResultLabelFor(this.languageCode)}</p>`;
    } else {
      html += `<p>${totalRowsLabelFor(this.languageCode)}${rows.length}</p>`;
    }

    html += `</body>`;
    html += `</html>`;
    return html;
  }
}

function totalRowsLabelFor(languageCode: string | undefined): string {
  if (languageCode?.toLowerCase() === 'it')
    return 'Righe totali: ';

  return 'Total rows: ';
}

function reportTitleFor(languageCode: string | undefined): string {
  if (languageCode?.toLowerCase() === 'it')
    return 'Report Trimestrale';

  return 'Quarterly Report';
}

function noResultLabelFor(languageCode: string | undefined): string {
  if (languageCode?.toLowerCase() === 'it')
    return 'Nessun risultato disponibile per questo trimestre.';

  return 'No results available for this quarter.';
}
