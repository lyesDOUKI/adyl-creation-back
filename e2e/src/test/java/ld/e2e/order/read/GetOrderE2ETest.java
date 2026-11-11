package ld.e2e.order.read;

import io.restassured.response.Response;
import ld.e2e.E2EBase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.hamcrest.Matchers.equalTo;

public class GetOrderE2ETest extends E2EBase {

    @Test
    void anonymousCannotGetOrder() {
        var orderId = orders.pending();
        var response = api.anonymous()
                .get("/orders/{id}", orderId)
                .then().statusCode(401)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "UNAUTHENTICATED",
    "title": "Non authentifié",
    "status": 401,
    "detail": "Authentification manquante ou invalide."
}""");
    }

    @Test
    void adminCannotGetOrder() {
        var orderId = orders.pending();
        var response = api.admin()
                .get("/orders/{id}", orderId)
                .then().statusCode(403)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "FORBIDDEN",
    "title": "Accès refusé",
    "status": 403,
    "detail": "Vous n'avez pas les droits pour accéder à cette ressource."
}""");
    }

    @Test
    void customerCannotGetUnknownOrder() {
        var response = api.customer()
                .get("/orders/{id}", UUID.randomUUID())
                .then().statusCode(404)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "ORDER_NOT_FOUND",
    "title": "Commande introuvable",
    "status": 404,
    "detail": "La commande est introuvable.",
    "orderId": "<uuid-1>"
}""");
    }

    @Test
    void invalidOrderIdIsRefused() {
        Response bad = api.customer()
                .get("/orders/{id}", "not-a-uuid")
                .then().statusCode(400)
                .body("code", equalTo("INVALID_PARAMETER"))
                .extract().response();

        expectSelfie(snapshots.of(bad)).toBe("""
{
    "code": "INVALID_PARAMETER",
    "title": "Paramètre invalide",
    "status": 400,
    "detail": "Le paramètre 'id' est invalide : valeur incompatible avec le type attendu.",
    "parameter": "id",
    "reason": "valeur incompatible avec le type attendu."
}""");
    }

    @Test
    void customerCanGetPendingOrderWithLinks() {
        var pendingOrder = orders.pending();

        var getPendingOrder = api.customer()
                .get("/orders/{id}", pendingOrder)
                .then().statusCode(200)
                .extract().response();


        expectSelfie(snapshots.of(getPendingOrder)).toBe("""
{
    "_links": {
        "cancel": {
            "href": "/api/orders/<uuid-1>/cancel"
        }
    },
    "orderId": "<uuid-1>",
    "customerId": "<uuid-2>",
    "orderReference": "<reference>",
    "customerMessage": "Merci de livrer le matin",
    "createdAt": "<date>",
    "updatedAt": "<date>",
    "total": 24.9,
    "status": "PENDING",
    "lines": [
        {
            "productId": "<uuid-3>",
            "productName": "Bonnet en laine",
            "productCategory": "CLOTHING",
            "quantity": 1,
            "unitPrice": 24.9,
            "chosenColor": "ROUGE",
            "subtotalBeforeDiscount": 24.9,
            "discountRate": 0.0,
            "discountAmount": 0.0,
            "totalAmount": 24.9
        }
    ],
    "lineCount": 1,
    "totalQuantity": 1
}""");
    }

    @Test
    void customerCanGetAcceptedOrderWithoutLinks() {
        var acceptedOrder = orders.accepted();


        var getAcceptedOrder = api.customer()
                .get("/orders/{id}", acceptedOrder)
                .then().statusCode(200)
                .extract().response();
        
        expectSelfie(snapshots.of(getAcceptedOrder)).toBe("""
{
    "orderId": "<uuid-1>",
    "customerId": "<uuid-2>",
    "orderReference": "<reference>",
    "customerMessage": "Merci de livrer le matin",
    "createdAt": "<date>",
    "updatedAt": "<date>",
    "total": 24.9,
    "status": "ACCEPTED",
    "lines": [
        {
            "productId": "<uuid-3>",
            "productName": "Bonnet en laine",
            "productCategory": "CLOTHING",
            "quantity": 1,
            "unitPrice": 24.9,
            "chosenColor": "ROUGE",
            "subtotalBeforeDiscount": 24.9,
            "discountRate": 0.0,
            "discountAmount": 0.0,
            "totalAmount": 24.9
        }
    ],
    "lineCount": 1,
    "totalQuantity": 1
}""");
    }

}
