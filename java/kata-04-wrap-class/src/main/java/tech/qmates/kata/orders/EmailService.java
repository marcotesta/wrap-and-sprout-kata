package tech.qmates.kata.orders;

import java.util.Properties;

/**
 * Sends transactional e-mails. Real I/O: connects to an SMTP server.
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.internal.qmates.tech";
    private static final int SMTP_PORT = 587;

    public void sendOrderConfirmation(String toAddress, String orderId, double total) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        properties.put("mail.smtp.auth", "true");

        // Pretend to open an SMTP session and transmit the message.
        String body = "Thank you! Your order " + orderId
                + " has been received. Total charged: EUR " + String.format("%.2f", total);
        transmit(toAddress, "Order confirmation " + orderId, body, properties);
    }

    private void transmit(String to, String subject, String body, Properties properties) {
        // A real implementation would block on network I/O against the SMTP host.
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Missing recipient address");
        }
        System.out.println("Sending mail to " + to + " via " + properties.get("mail.smtp.host"));
    }
}
