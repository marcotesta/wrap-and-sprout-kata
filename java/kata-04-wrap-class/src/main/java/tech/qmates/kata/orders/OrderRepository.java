package tech.qmates.kata.orders;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Persists orders to a relational database. Real I/O: opens a JDBC connection.
 */
public class OrderRepository {

    private static final String JDBC_URL = "jdbc:postgresql://db.internal.qmates.tech:5432/orders";
    private static final String DB_USER = "orders_app";
    private static final String DB_PASSWORD = "s3cr3t";

    public String save(Order order, double total, double tax) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO orders (order_id, customer_email, total, tax) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, order.getOrderId());
                statement.setString(2, order.getCustomerEmail());
                statement.setDouble(3, total);
                statement.setDouble(4, tax);
                statement.executeUpdate();
            }
            return order.getOrderId();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to persist order " + order.getOrderId(), e);
        }
    }
}
