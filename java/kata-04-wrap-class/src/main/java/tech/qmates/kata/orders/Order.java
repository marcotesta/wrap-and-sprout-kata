package tech.qmates.kata.orders;

import java.util.ArrayList;
import java.util.List;

/**
 * A plain data object representing a customer order.
 */
public class Order {

    private final String orderId;
    private final String customerEmail;
    private final List<LineItem> items = new ArrayList<>();
    private String customerCountry = "IT";
    private boolean loyaltyMember = false;

    public Order(String orderId, String customerEmail) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<LineItem> getItems() {
        return items;
    }

    public void addItem(LineItem item) {
        items.add(item);
    }

    public String getCustomerCountry() {
        return customerCountry;
    }

    public void setCustomerCountry(String customerCountry) {
        this.customerCountry = customerCountry;
    }

    public boolean isLoyaltyMember() {
        return loyaltyMember;
    }

    public void setLoyaltyMember(boolean loyaltyMember) {
        this.loyaltyMember = loyaltyMember;
    }

    public static class LineItem {
        private final String sku;
        private final int quantity;
        private final double unitPrice;

        public LineItem(String sku, int quantity, double unitPrice) {
            this.sku = sku;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getSku() {
            return sku;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }
    }
}
