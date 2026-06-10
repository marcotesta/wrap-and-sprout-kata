// Legacy HR system code.
//
// This module is intentionally hard to test:
//  - EmployeeRepository is a SINGLETON whose getInstance() opens a real
//    database connection and whose methods issue real SQL.
//  - EmployeeService reaches for that singleton directly inside its methods,
//    so there is no seam to inject a fake repository.
//
// Do NOT refactor the repository into an injected dependency for this kata.
// The point is to add new behaviour using the Wrap Method technique without
// touching the existing, untested business logic.

export type EmployeeRecord = {
  id: string;
  name: string;
  title: string;
  level: number;
  yearsInRole: number;
  lastReviewScore: number; // 1..5
  onPerformanceImprovementPlan: boolean;
};

export type PromotionEvent = {
  type: 'employee.promoted';
  employeeId: string;
  fromTitle: string;
  toTitle: string;
  occurredAt: Date;
};

export class PromotionError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'PromotionError';
    Object.setPrototypeOf(this, PromotionError.prototype);
  }
}

// An event bus that can be created directly with `new EventBus()`.
export class EventBus {
  publish(event: PromotionEvent): void {
    // In production this would push to a message broker / outbox.
    console.log(`[EventBus] published ${event.type} for ${event.employeeId}`);
  }
}

// Hard-to-test singleton: getInstance() and its methods clearly hit a real DB.
export class EmployeeRepository {
  private static instance: EmployeeRepository | null = null;

  private constructor() {
    // Opens a real database connection on first construction.
    // e.g. this.connection = mysql.createConnection(process.env.HR_DB_URL!);
    console.log('[EmployeeRepository] opening live database connection...');
  }

  static getInstance(): EmployeeRepository {
    if (EmployeeRepository.instance === null) {
      EmployeeRepository.instance = new EmployeeRepository();
    }
    return EmployeeRepository.instance;
  }

  findById(employeeId: string): EmployeeRecord {
    // Real query: SELECT * FROM employees WHERE id = ?
    throw new Error(
      `[EmployeeRepository] DB not available: cannot SELECT employee ${employeeId}`,
    );
  }

  save(record: EmployeeRecord): void {
    // Real query: UPDATE employees SET ... WHERE id = ?
    throw new Error(
      `[EmployeeRepository] DB not available: cannot UPDATE employee ${record.id}`,
    );
  }
}

export class EmployeeService {
  promote(employeeId: string, newTitle: string): void {
    const repo = EmployeeRepository.getInstance();
    const employee = repo.findById(employeeId);

    // Hardcoded promotion rules.
    if (employee.onPerformanceImprovementPlan) {
      throw new PromotionError(
        `Employee ${employeeId} is on a performance improvement plan and cannot be promoted`,
      );
    }
    if (employee.yearsInRole < 1) {
      throw new PromotionError(
        `Employee ${employeeId} must spend at least 1 year in role before promotion`,
      );
    }
    if (employee.lastReviewScore < 3) {
      throw new PromotionError(
        `Employee ${employeeId} needs a review score of at least 3 to be promoted`,
      );
    }
    if (newTitle.trim() === '' || newTitle === employee.title) {
      throw new PromotionError(
        `Invalid new title "${newTitle}" for employee ${employeeId}`,
      );
    }

    employee.title = newTitle;
    employee.level = employee.level + 1;
    employee.yearsInRole = 0;

    repo.save(employee);
  }

  demote(employeeId: string, newTitle: string): void {
    const repo = EmployeeRepository.getInstance();
    const employee = repo.findById(employeeId);

    // Hardcoded demotion rules.
    if (employee.level <= 1) {
      throw new PromotionError(
        `Employee ${employeeId} is already at the lowest level and cannot be demoted`,
      );
    }
    if (newTitle.trim() === '' || newTitle === employee.title) {
      throw new PromotionError(
        `Invalid new title "${newTitle}" for employee ${employeeId}`,
      );
    }

    employee.title = newTitle;
    employee.level = employee.level - 1;
    employee.yearsInRole = 0;

    repo.save(employee);
  }
}
