package ld.e2e.support;

import io.restassured.specification.RequestSpecification;
import ld.bootstrap.request.CreateOrderRequest;
import ld.bootstrap.request.CreateOrderRequest.ItemOrderRequest;
import ld.bootstrap.request.DeliverOrderRequest;
import ld.bootstrap.request.DeliveryMethodRequest;
import ld.bootstrap.request.RejectOrderRequest;

import java.util.List;
import java.util.UUID;

public final class OrderFixtures {

    private final Api api;
    private final ProductFixtures products;
    private final CustomerFixtures customers;

    public OrderFixtures(Api api, ProductFixtures products, CustomerFixtures customers) {
        this.api = api;
        this.products = products;
        this.customers = customers;
    }

    /** Une ligne, quantité 1, couleur rouge, pour le client donné. */
    public UUID place(RequestSpecification customer, UUID productId) {
        String id = customer
                .body(new CreateOrderRequest("12 rue des Lilas", "Avignon", "Merci de livrer le matin",
                        List.of(new ItemOrderRequest(productId, 1, "rouge"))))
                .post("/orders")
                .then().statusCode(201)
                .extract().path("orderId");
        return UUID.fromString(id);
    }

    // --- États de départ : produit + client + commande déjà dans l'état voulu ---
    public UUID pending() {
        UUID productId = products.bonnet();
        customers.register();
        return place(api.customer(), productId);
    }

    public UUID accepted() {
        UUID orderId = pending();
        accept(orderId);
        return orderId;
    }

    public UUID rejected() {
        UUID orderId = pending();
        reject(orderId);
        return orderId;
    }

    public UUID delivered() {
        UUID orderId = accepted();
        deliver(orderId);
        return orderId;
    }

    // --- Transitions faites par l'admin (attendent un 200) ---
    public void accept(UUID orderId) {
        api.admin().post("/orders/{id}/accept", orderId)
                .then().statusCode(200);
    }

    public void reject(UUID orderId) {
        api.admin().body(new RejectOrderRequest("Produit épuisé"))
                .post("/orders/{id}/reject", orderId)
                .then().statusCode(200);
    }

    public void deliver(UUID orderId) {
        api.admin().body(new DeliverOrderRequest(DeliveryMethodRequest.HAND_DELIVERY, null))
                .post("/orders/{id}/deliver", orderId)
                .then().statusCode(200);
    }
}