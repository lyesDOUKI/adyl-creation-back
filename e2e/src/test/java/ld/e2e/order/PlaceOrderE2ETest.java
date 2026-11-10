package ld.e2e.order;

import io.restassured.response.Response;
import ld.bootstrap.request.CreateOrderRequest;
import ld.bootstrap.request.CreateOrderRequest.ItemOrderRequest;
import ld.e2e.E2EBase;
import ld.e2e.Snapshots;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.hamcrest.Matchers.equalTo;

class PlaceOrderE2ETest extends E2EBase {

    private final Snapshots snapshots = new Snapshots();

    @Test
    void customerPlacesAnOrder() {
        UUID productId = products.bonnet();
        customers.register();
        Response created = api.customer()
                .body(orderOf(new ItemOrderRequest(productId, 2, "rouge")))
                .post("/orders")
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.of(created)).toBe("""
{
    "orderId": "<uuid-1>",
    "total": 49.8
}""");

        String orderId = created.path("orderId");
        Response fetched = api.customer()
                .get("/orders/{id}", orderId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(fetched)).toBe("""
{
    "orderId": "<uuid-1>",
    "customerId": "<uuid-2>",
    "orderReference": "<reference>",
    "customerMessage": "Merci de livrer le matin",
    "createdAt": "<date>",
    "updatedAt": "<date>",
    "total": 49.8,
    "status": "PENDING",
    "lines": [
        {
            "productId": "<uuid-3>",
            "productName": "Bonnet en laine",
            "productCategory": "CLOTHING",
            "quantity": 2,
            "unitPrice": 24.9,
            "chosenColor": "ROUGE",
            "subtotalBeforeDiscount": 49.8,
            "discountRate": 0.0,
            "discountAmount": 0.0,
            "totalAmount": 49.8
        }
    ],
    "lineCount": 1,
    "totalQuantity": 2
}""");
    }

    @Test
    void orderWithUnavailableColorIsRefused() {
        UUID productId = products.bonnet();
        customers.register();
        Response refused = api.customer()
                .body(orderOf(new ItemOrderRequest(productId, 1, "vert")))
                .post("/orders")
                .then().statusCode(422)
                .body("code", equalTo("PRODUCT_COLOR_NOT_AVAILABLE"))
                .extract().response();

        expectSelfie(snapshots.of(refused)).toBe("""
{
    "code": "PRODUCT_COLOR_NOT_AVAILABLE",
    "title": "Couleur demandée non disponible",
    "status": 422,
    "detail": "La couleur 'vert' n'est pas disponible pour le produit <uuid-1>.",
    "productId": "<uuid-1>",
    "requestedColor": "vert",
    "availableColors": [
        "BLEU",
        "ROUGE"
    ]
}""");
    }

    @Test
    void anonymousCannotPlaceAnOrder() {
        api.anonymous().body(orderOf(new ItemOrderRequest(UUID.randomUUID(), 1, "rouge")))
                .post("/orders")
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }

    private static CreateOrderRequest orderOf(ItemOrderRequest... items) {
        return new CreateOrderRequest(
                "12 rue des Lilas", "Avignon", "Merci de livrer le matin", List.of(items));
    }
}