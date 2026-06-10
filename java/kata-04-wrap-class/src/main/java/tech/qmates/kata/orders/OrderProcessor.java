package tech.qmates.kata.orders;

/**
 * Processes customer orders end to end: validation, pricing, persistence,
 * confirmation e-mail and inventory updates.
 *
 * <p>NOTE FOR THE KATA: this class implements <em>no</em> interface and
 * directly instantiates its three external collaborators
 * ({@link OrderRepository}, {@link EmailService}, {@link InventoryService})
 * inside its own constructor. That makes it very hard to test in isolation.
 */
public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final InventoryService inventoryService;

    public OrderProcessor() {
        // Collaborators are created here. There is no seam to replace them.
        this.orderRepository = new OrderRepository();
        this.emailService = new EmailService();
        this.inventoryService = new InventoryService();
    }

    /**
     * Orchestrates the full order pipeline. This is the method the kata
     * asks you to time.
     */
    public String placeOrder(Order order) {
        validateOrder(order);
        double subtotal = calculateTotal(order);
        double discounted = applyDiscounts(order, subtotal);
        double tax = calculateTax(order, discounted);
        double grandTotal = discounted + tax;
        String persistedId = persistOrder(order, grandTotal, tax);
        updateInventory(order);
        sendConfirmationEmail(order, grandTotal);
        return persistedId;
    }

    public void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        if (order.getOrderId() == null || order.getOrderId().isBlank()) {
            throw new IllegalArgumentException("Order id is required");
        }
        if (order.getCustomerEmail() == null || !order.getCustomerEmail().contains("@")) {
            throw new IllegalArgumentException("A valid customer e-mail is required");
        }
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item");
        }
        for (Order.LineItem item : order.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for SKU " + item.getSku());
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Unit price must not be negative for SKU " + item.getSku());
            }
        }
    }

    public double calculateTotal(Order order) {
        double total = 0.0;
        for (Order.LineItem item : order.getItems()) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        return total;
    }

    public double applyDiscounts(Order order, double subtotal) {
        double result = subtotal;
        if (order.isLoyaltyMember()) {
            result = result * 0.95; // 5% loyalty discount
        }
        if (subtotal > 500.0) {
            result = result - 25.0; // bulk rebate
        }
        return Math.max(result, 0.0);
    }

    public double calculateTax(Order order, double taxableAmount) {
        double rate;
        switch (order.getCustomerCountry()) {
            case "IT" -> rate = 0.22;
            case "DE" -> rate = 0.19;
            case "FR" -> rate = 0.20;
            default -> rate = 0.0;
        }
        return taxableAmount * rate;
    }

    public String persistOrder(Order order, double grandTotal, double tax) {
        return orderRepository.save(order, grandTotal, tax);
    }

    public void updateInventory(Order order) {
        for (Order.LineItem item : order.getItems()) {
            inventoryService.reserve(item.getSku(), item.getQuantity());
        }
    }

    public void sendConfirmationEmail(Order order, double grandTotal) {
        emailService.sendOrderConfirmation(order.getCustomerEmail(), order.getOrderId(), grandTotal);
    }
}
