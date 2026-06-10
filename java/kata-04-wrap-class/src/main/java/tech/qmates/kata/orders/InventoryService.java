package tech.qmates.kata.orders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Reserves and decrements stock by calling the warehouse REST API. Real I/O: HTTP.
 */
public class InventoryService {

    private static final String INVENTORY_API = "https://warehouse.internal.qmates.tech/api/v1";

    public void reserve(String sku, int quantity) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(INVENTORY_API + "/stock/" + sku + "/reserve?qty=" + quantity))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Inventory reservation failed for " + sku
                        + " (HTTP " + response.statusCode() + ")");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to reserve stock for " + sku, e);
        }
    }
}
