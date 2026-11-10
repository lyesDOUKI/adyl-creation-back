package ld.e2e.order;

import ld.bootstrap.request.RejectOrderRequest;
import ld.e2e.E2EBase;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.hamcrest.Matchers.equalTo;

public class RejectOrderE2ETest extends E2EBase {

    @Test
    void adminRejectsPendingOrder() {
        UUID orderId = orders.pending();

        var response = api.admin()
                .body(defaultRejectReason())
                .post("/orders/{id}/reject", orderId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "orderId": "<uuid-1>",
    "reason": "no time",
    "total": 24.9,
    "status": "REJECTED",
    "rejectedAt": "<date>"
}""");

        api.customer().get("/orders/{id}", orderId)
                .then().statusCode(200)
                .body("status", equalTo("REJECTED"));
    }

    @Test
    void invalidOrderIdIsRefused() {
        var response = api.admin()
                .post("/orders/{id}/reject", "bad-uuid")
                .then().statusCode(400)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
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
    void invalidRejectReasonIsRefused() {
        var response = api.admin()
                .body(invalidRejectReason())
                .post("/orders/{id}/reject", UUID.randomUUID())
                .then().statusCode(400)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "reason",
            "message": "Le motif ne doit pas dépasser 500 caractères"
        }
    ]
}""");
    }


    @Test
    void anonymousCannotRejectAnOrder() {
        var orderId = orders.pending();
        var response = api.anonymous()
                .post("/orders/{id}/reject", orderId)
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
    void customerCannotRejectAnOrder() {
        var orderId = orders.pending();

        var response = api.customer()
                .body(defaultRejectReason())
                .post("/orders/{id}/reject", orderId)
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
    void adminCannotRejectUnknownOrder() {
        var response = api.admin()
                .body(defaultRejectReason())
                .post("/orders/{id}/reject", UUID.randomUUID())
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
    void adminCannotRejectDeliveredOrder() {
        var orderId = orders.delivered();
        var response = api.admin()
                .body(defaultRejectReason())
                .post("/orders/{id}/reject", orderId)
                .then().statusCode(409)
                .extract().response();

        expectSelfie(snapshots.of(response)).toBe("""
{
    "code": "ORDER_ALREADY_DELIVERED",
    "title": "Commande livrée",
    "status": 409,
    "detail": "L'opération est impossible sur une commande déjà livrée."
}""");
    }

    private static @NonNull RejectOrderRequest defaultRejectReason() {
        return new RejectOrderRequest("no time");
    }

    private static @NonNull RejectOrderRequest invalidRejectReason() {
        return new RejectOrderRequest("A".repeat(501));
    }
}
