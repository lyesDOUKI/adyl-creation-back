package ld.e2e.order;

import io.restassured.response.Response;
import ld.e2e.E2EBase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.hamcrest.Matchers.equalTo;

public class CancelOrderE2ETest extends E2EBase {

    @Test
    void anonymousCannotCancelOrder() {
        var orderId = orders.pending();
        var response = api.anonymous()
                .post("/orders/{id}/cancel", orderId)
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
    void adminCannotCancelOrder() {
        var orderId = orders.pending();
        var response = api.admin()
                .post("/orders/{id}/cancel", orderId)
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
    void customerCanCancelPendingOrder() {
        var orderId = orders.pending();
        var response = api.customer()
                .post("/orders/{id}/cancel", orderId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "orderId": "<uuid-1>",
    "total": 24.9,
    "status": "CANCELLED",
    "cancelledAt": "<date>"
}""");
    }

    @Test
    void customerCannotCancelAcceptedOrder() {
        var orderId = orders.accepted();
        var response = api.customer()
                .post("/orders/{id}/cancel", orderId)
                .then().statusCode(409)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "ACCEPTED",
    "title": "Commande acceptée",
    "status": 409,
    "detail": "La commande a été acceptée ; elle ne peut donc pas être annulée."
}""");
    }

    @Test
    void customerCannotCancelRejectedOrder() {
        var orderId = orders.rejected();
        var response = api.customer()
                .post("/orders/{id}/cancel", orderId)
                .then().statusCode(409)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "REJECTED",
    "title": "Commande rejetée",
    "status": 409,
    "detail": "La commande a été rejetée ; elle ne peut donc pas être annulée."
}""");
    }

    @Test
    void customerCannotCancelDeliveredOrder() {
        var orderId = orders.delivered();
        var response = api.customer()
                .post("/orders/{id}/cancel", orderId)
                .then().statusCode(409)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "DELIVERED",
    "title": "Commande livrée",
    "status": 409,
    "detail": "La commande a été livrée ; elle ne peut donc pas être annulée."
}""");
    }

    @Test
    void customerCannotCancelUnknownOrder() {
        var unknownOrder = UUID.randomUUID();
        var response = api.customer()
                .post("/orders/{id}/cancel", unknownOrder)
                .then().statusCode(404)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "ORDER_NOT_FOUND",
    "title": "Commande introuvable",
    "status": 404,
    "detail": "La commande demandée est introuvable",
    "orderId": "<uuid-1>"
}""");
    }

    @Test
    void invalidOrderIdIsRefused() {
        Response bad = api.customer()
                .post("/orders/{id}/accept", "not-a-uuid")
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
    void customerCanCallCancellationOnOrderAlreadyCancelled() {
        var orderId = orders.cancelled();

        var response = api.customer()
                .post("/orders/{id}/cancel", orderId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "orderId": "<uuid-1>",
    "total": 24.9,
    "status": "CANCELLED",
    "cancelledAt": "<date>"
}""");
    }
}
