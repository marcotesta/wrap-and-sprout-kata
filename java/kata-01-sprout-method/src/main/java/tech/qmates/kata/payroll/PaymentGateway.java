package tech.qmates.kata.payroll;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * External payment service. This class talks to a real banking endpoint over
 * the network, which is exactly what makes any code that uses it hard to test:
 * you cannot exercise the caller without either hitting the real service or
 * mocking out something that is instantiated with a bare {@code new}.
 */
public class PaymentGateway {

    private static final String ENDPOINT = "https://payments.example.com/api/v1/transfer";

    /**
     * Sends a payment by opening a REAL HTTP connection to the banking endpoint.
     * In production this debits a settlement account and credits the employee.
     */
    public void send(String accountNumber, double amount) {
        try {
            // This opens a real network connection. Running it in a test would
            // either hit the live banking API or fail with a connection error.
            URI uri = URI.create(ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json");

            String payload = "{\"account\":\"" + accountNumber + "\",\"amount\":" + amount + "}";
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("Payment failed with HTTP status " + status);
            }
            connection.disconnect();
        } catch (Exception e) {
            throw new RuntimeException("Unable to reach payment gateway", e);
        }
    }
}
