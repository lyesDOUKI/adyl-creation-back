package ld.e2e.product;

import io.restassured.response.Response;
import ld.bootstrap.request.CreateProductRequest;
import ld.e2e.E2EBase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static ld.bootstrap.request.ProductCategoryRequest.CLOTHING;
import static org.hamcrest.Matchers.equalTo;

class CreateProductE2ETest extends E2EBase {

    private static final BigDecimal PRICE = new BigDecimal("24.90");

    @Test
    void adminCreatesAProduct() {
        Response created = api.admin()
                .body(new CreateProductRequest("Bonnet en laine", CLOTHING, List.of("rouge", "bleu"), PRICE))
                .post("/products")
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.of(created)).toBe("""
{
    "productId": "<uuid-1>",
    "name": "Bonnet en laine",
    "price": 24.9
}""");

        String productId = created.path("productId");
        api.anonymous().get("/products/{id}", productId)
                .then().statusCode(200);
    }

    @Test
    void adminCreatesAProductWithoutColors() {
        Response created = api.admin()
                .body(new CreateProductRequest("Echarpe", CLOTHING, null, PRICE))
                .post("/products")
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.of(created)).toBe("""
{
    "productId": "<uuid-1>",
    "name": "Echarpe",
    "price": 24.9
}""");

        Response fetched = api.anonymous()
                .get("/products/{id}", created.<String>path("productId"))
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(fetched)).toBe("""
{
    "productId": "<uuid-1>",
    "productCategory": "CLOTHING",
    "name": "Echarpe",
    "price": 24.9,
    "colors": [
       \s
    ],
    "photosUri": [
       \s
    ],
    "numberOfOrders": 0
}""");
    }

    @Test
    void productNameMustBeUnique() {
        products.named("Bonnet en laine");

        Response conflict = api.admin()
                .body(new CreateProductRequest("Bonnet en laine", CLOTHING, List.of("rouge"), PRICE))
                .post("/products")
                .then().statusCode(409)
                .body("code", equalTo("PRODUCT_ALREADY_EXISTS"))
                .extract().response();

        expectSelfie(snapshots.of(conflict)).toBe("""
{
    "code": "PRODUCT_ALREADY_EXISTS",
    "title": "Produit déjà existant",
    "status": 409,
    "detail": "Un produit portant ce nom existe déjà.",
    "name": "Bonnet en laine"
}""");
    }

    @Test
    void blankNameIsRefused() {
        Response invalid = api.admin()
                .body(new CreateProductRequest("  ", CLOTHING, List.of("rouge"), PRICE))
                .post("/products")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "name",
            "message": "Le nom est obligatoire"
        }
    ]
}""");
    }

    @Test
    void missingCategoryIsRefused() {
        Response invalid = api.admin()
                .body(new CreateProductRequest("Bonnet en laine", null, List.of("rouge"), PRICE))
                .post("/products")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "productCategory",
            "message": "ne doit pas être nul"
        }
    ]
}""");
    }

    @Test
    void priceMustBeStrictlyPositive() {
        Response invalid = api.admin()
                .body(new CreateProductRequest("Bonnet en laine", CLOTHING, List.of("rouge"), BigDecimal.ZERO))
                .post("/products")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "price",
            "message": "doit être supérieur à 0"
        }
    ]
}""");
    }

    @Test
    void unknownCategoryIsRefused() {
        Response invalid = api.admin()
                .body(Map.of("name", "Bonnet en laine", "productCategory", "SHOES", "price", 24.9))
                .post("/products")
                .then().statusCode(400)
                .body("code", equalTo("INVALID_REQUEST_BODY"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "INVALID_REQUEST_BODY",
    "title": "Requête invalide",
    "status": 400,
    "detail": "Le corps de la requête est absent, mal formé ou contient une valeur incompatible avec le format attendu.",
    "errors": [
        {
            "field": "productCategory",
            "message": "Valeur invalide. Valeurs acceptées : ACCESSORIES, CLOTHING."
        }
    ]
}""");
    }

    @Test
    void malformedJsonIsRefused() {
        Response invalid = api.admin()
                .body("{ not json")
                .post("/products")
                .then().statusCode(400)
                .body("code", equalTo("INVALID_REQUEST_BODY"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "INVALID_REQUEST_BODY",
    "title": "Requête invalide",
    "status": 400,
    "detail": "Le corps de la requête est absent, mal formé ou contient une valeur incompatible avec le format attendu.",
    "errors": [
        {
            "field": "body",
            "message": "JSON mal formé."
        }
    ]
}""");
    }

    @Test
    void customerCannotCreateAProduct() {
        api.customer()
                .body(new CreateProductRequest("Bonnet en laine", CLOTHING, List.of("rouge"), PRICE))
                .post("/products")
                .then().statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    void anonymousCannotCreateAProduct() {
        api.anonymous()
                .body(new CreateProductRequest("Bonnet en laine", CLOTHING, List.of("rouge"), PRICE))
                .post("/products")
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }
}