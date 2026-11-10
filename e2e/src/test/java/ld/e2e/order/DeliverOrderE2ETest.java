package ld.e2e.order;

import ld.bootstrap.request.DeliverOrderRequest;
import ld.bootstrap.request.DeliveryMethodRequest;
import ld.e2e.E2EBase;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;

public class DeliverOrderE2ETest extends E2EBase {

    @Test
    void adminDeliversAcceptedOrder() {
        var orderId = orders.accepted();

        var response = api.admin()
                .body(createOrderRequest())
                .post("/orders/{id}/deliver", orderId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "orderId": "<uuid-1>",
    "observation": "Thanks!",
    "deliveryMethod": "HAND_DELIVERY",
    "status": "DELIVERED",
    "deliveredAt": "<date>"
}""");
    }

    @Test
    void invalidOrderIdIsRefused() {
        var response = api.admin()
                .post("/orders/{id}/deliver", UUID.randomUUID())
                .then().statusCode(400)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "INVALID_REQUEST_BODY",
    "title": "Requête invalide",
    "status": 400,
    "detail": "Le corps de la requête est absent, mal formé ou contient une valeur incompatible avec le format attendu.",
    "errors": [
        {
            "field": "body",
            "message": "Corps de requête absent ou illisible."
        }
    ]
}""");
    }

    @Test
    void anonymousCannotDeliverAnOrder() {
        var orderId = orders.accepted();
        var response = api.anonymous()
                .body(createOrderRequest())
                .post("/orders/{id}/deliver", orderId)
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
    void customerCannotDeliverAnOrder() {
        var orderId = orders.delivered();
        var response = api.customer()
                .body(createOrderRequest())
                .post("/orders/{id}/deliver", orderId)
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
    void adminCannotDeliverUnknownOrder() {
        var response = api.admin()
                .body(createOrderRequest())
                .post("/orders/{id}/deliver", UUID.randomUUID())
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
    void adminCannotDeliverRejectedOrder() {
        var orderId = orders.rejected();

        var response = api.admin()
                .body(createOrderRequest())
                .post("/orders/{id}/deliver", orderId)
                .then().statusCode(409)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "ORDER_ALREADY_REJECTED",
    "title": "Commande rejetée",
    "status": 409,
    "detail": "La commande a été rejetée, elle ne peut plus être livrée."
}""");
    }

    @Test
    void adminCannotDeliverPendingOrder() {
        var orderId = orders.rejected();

        var response = api.admin()
                .body(createOrderRequest())
                .post("/orders/{id}/deliver", orderId)
                .then().statusCode(409)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "ORDER_ALREADY_REJECTED",
    "title": "Commande rejetée",
    "status": 409,
    "detail": "La commande a été rejetée, elle ne peut plus être livrée."
}""");
    }
    private static @NonNull DeliverOrderRequest createOrderRequest() {
        return new DeliverOrderRequest(DeliveryMethodRequest.HAND_DELIVERY, "Thanks!");
    }
}
