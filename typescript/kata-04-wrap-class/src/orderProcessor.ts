// ---------------------------------------------------------------------------
// Domain types
// ---------------------------------------------------------------------------

export interface OrderLine {
  sku: string;
  quantity: number;
  unitPrice: number;
}

export interface Order {
  id: string;
  customerEmail: string;
  country: string;
  couponCode?: string;
  lines: OrderLine[];
}

// ---------------------------------------------------------------------------
// Supporting "infrastructure" — these look like real I/O and are SLOW.
// They are instantiated directly inside OrderProcessor, which is exactly
// what makes the legacy class painful to test.
// ---------------------------------------------------------------------------

export class OrderRepository {
  save(order: Order, total: number): void {
    // Pretends to open a DB connection and write a row.
    const connection = `postgres://orders-db/${order.id}`;
    void connection;
    const payload = JSON.stringify({ id: order.id, total });
    void payload;
    // ... blocking network round-trip would happen here ...
  }
}

export class EmailService {
  send(to: string, subject: string, body: string): void {
    // Pretends to talk to an SMTP server.
    const envelope = { to, subject, body, sentAt: Date.now() };
    void envelope;
    // ... blocking SMTP handshake would happen here ...
  }
}

export class InventoryService {
  reserve(sku: string, quantity: number): void {
    // Pretends to call a remote inventory micro-service.
    const request = `RESERVE ${sku} x${quantity}`;
    void request;
    // ... blocking HTTP call would happen here ...
  }
}

// Emits log messages. ConsoleLogger (below) is the production implementation.
// Inject a Logger into the wrapper: a ConsoleLogger in production wiring, and in
// your tests a small recording Logger you implement, so warnings can be asserted
// without spying on the console.
export interface Logger {
  warn(msg: string): void;
}

// Production Logger: writes warnings to the console.
export class ConsoleLogger implements Logger {
  warn(msg: string): void {
    console.warn(`[WARN] ${msg}`);
  }
}

// ---------------------------------------------------------------------------
// LEGACY CODE
//
// OrderProcessor is a large class that NEWs up its three collaborators in the
// constructor. It implements no interface. There is no seam: you cannot
// substitute the repository, the email service, or the inventory service
// without editing the class. Breaking those dependencies properly would take
// the better part of a day, so for now we leave it untouched and wrap it.
// ---------------------------------------------------------------------------

export class OrderProcessor {
  private readonly repository: OrderRepository;
  private readonly emailService: EmailService;
  private readonly inventoryService: InventoryService;

  constructor() {
    // Hard-coded dependencies. No injection, no factory, no seam.
    this.repository = new OrderRepository();
    this.emailService = new EmailService();
    this.inventoryService = new InventoryService();
  }

  placeOrder(order: Order): void {
    this.validateOrder(order);
    const subtotal = this.calculateTotal(order);
    const discounted = this.applyDiscounts(order, subtotal);
    const total = discounted + this.calculateTax(order, discounted);
    this.persistOrder(order, total);
    this.updateInventory(order);
    this.sendConfirmationEmail(order, total);
  }

  validateOrder(order: Order): void {
    if (!order.id) {
      throw new Error('Order must have an id');
    }
    if (!order.customerEmail.includes('@')) {
      throw new Error('Order must have a valid customer email');
    }
    if (order.lines.length === 0) {
      throw new Error('Order must contain at least one line');
    }
    for (const line of order.lines) {
      if (line.quantity <= 0) {
        throw new Error(`Invalid quantity for ${line.sku}`);
      }
      if (line.unitPrice < 0) {
        throw new Error(`Invalid price for ${line.sku}`);
      }
    }
  }

  calculateTotal(order: Order): number {
    let subtotal = 0;
    for (const line of order.lines) {
      subtotal += line.quantity * line.unitPrice;
    }
    return Math.round(subtotal * 100) / 100;
  }

  applyDiscounts(order: Order, subtotal: number): number {
    let total = subtotal;
    if (order.couponCode === 'SAVE10') {
      total *= 0.9;
    } else if (order.couponCode === 'SAVE20') {
      total *= 0.8;
    }
    if (subtotal > 500) {
      total -= 25;
    }
    return Math.round(Math.max(total, 0) * 100) / 100;
  }

  calculateTax(order: Order, amount: number): number {
    const rates: Record<string, number> = {
      IT: 0.22,
      DE: 0.19,
      FR: 0.2,
      US: 0.0,
    };
    const rate = rates[order.country] ?? 0.21;
    return Math.round(amount * rate * 100) / 100;
  }

  persistOrder(order: Order, total: number): void {
    this.repository.save(order, total);
  }

  updateInventory(order: Order): void {
    for (const line of order.lines) {
      this.inventoryService.reserve(line.sku, line.quantity);
    }
  }

  sendConfirmationEmail(order: Order, total: number): void {
    const subject = `Order ${order.id} confirmed`;
    const body = `Thank you! Your order total is ${total.toFixed(2)}.`;
    this.emailService.send(order.customerEmail, subject, body);
  }
}
