package ld.e2e.product.photo;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import ld.e2e.E2EBase;
import ld.e2e.support.Images;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.hamcrest.Matchers.equalTo;

class AddProductPhotosE2ETest extends E2EBase {

    private static final int MAX_PHOTO_SIZE_BYTES = 8 * 1024 * 1024;

    @Test
    void adminAddsOnePhoto() {
        UUID productId = products.bonnet();

        Response added = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "bonnet.png", Images.red(), "image/png")
                .post("/products/{id}/photos", productId)
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.of(added)).toBe("""
{
    "productId": "<uuid-1>",
    "photos": [
        {
            "id": "<uuid-2>",
            "url": "/api/products/<uuid-1>/photos/<uuid-3>.png",
            "position": 0
        }
    ]
}""");

        Response product = api.anonymous().get("/products/{id}", productId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(product)).toBe("""
{
    "productId": "<uuid-1>",
    "productCategory": "CLOTHING",
    "name": "Bonnet en laine",
    "price": 24.9,
    "colors": [
        "BLEU",
        "ROUGE"
    ],
    "photosUri": [
        "/api/products/<uuid-1>/photos/<uuid-3>.png"
    ],
    "numberOfOrders": 0
}""");
    }

    @Test
    void adminAddsSeveralPhotosAtOnce() {
        UUID productId = products.bonnet();

        Response added = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "face.png", Images.red(), "image/png")
                .multiPart("photos", "dos.png", Images.blue(), "image/png")
                .post("/products/{id}/photos", productId)
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.of(added)).toBe("""
{
    "productId": "<uuid-1>",
    "photos": [
        {
            "id": "<uuid-2>",
            "url": "/api/products/<uuid-1>/photos/<uuid-3>.png",
            "position": 0
        },
        {
            "id": "<uuid-4>",
            "url": "/api/products/<uuid-1>/photos/<uuid-5>.png",
            "position": 1
        }
    ]
}""");
    }

    @Test
    void newPhotosAreAddedAfterTheExistingOnes() {
        UUID productId = products.bonnet();
        photos.add(productId, "face.png", Images.red());

        Response added = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "dos.png", Images.blue(), "image/png")
                .post("/products/{id}/photos", productId)
                .then().statusCode(201)
                .extract().response();

        expectSelfie(snapshots.of(added)).toBe("""
{
    "productId": "<uuid-1>",
    "photos": [
        {
            "id": "<uuid-2>",
            "url": "/api/products/<uuid-1>/photos/<uuid-3>.png",
            "position": 0
        },
        {
            "id": "<uuid-4>",
            "url": "/api/products/<uuid-1>/photos/<uuid-5>.png",
            "position": 1
        }
    ]
}""");
    }

    @Test
    void unknownProductIsNotFound() {
        Response notFound = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "bonnet.png", Images.red(), "image/png")
                .post("/products/{id}/photos", UUID.randomUUID())
                .then().statusCode(404)
                .body("code", equalTo("PRODUCT_NOT_FOUND"))
                .extract().response();

        expectSelfie(snapshots.of(notFound)).toBe("""
{
    "code": "PRODUCT_NOT_FOUND",
    "title": "Produit introuvable",
    "status": 404,
    "detail": "Le produit est introuvable.",
    "productId": "<uuid-1>"
}""");
    }

    @Test
    void emptyFileIsRefused() {
        UUID productId = products.bonnet();

        Response invalid = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "vide.png", new byte[0], "image/png")
                .post("/products/{id}/photos", productId)
                .then().statusCode(422)
                .body("code", equalTo("PHOTO_SIZE_ZERO"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "PHOTO_SIZE_ZERO",
    "title": "Image vide",
    "status": 422,
    "detail": "La taille de l'image est à 0 (fichier : vide.png).",
    "fileName": "vide.png"
}""");
    }

    @Test
    void forbiddenExtensionIsRefused() {
        UUID productId = products.bonnet();

        Response invalid = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "notes.txt", "pas une image".getBytes(), "text/plain")
                .post("/products/{id}/photos", productId)
                .then().statusCode(422)
                .body("code", equalTo("PHOTO_EXTENSION_NOT_ALLOWED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "PHOTO_EXTENSION_NOT_ALLOWED",
    "title": "Extension incorrecte",
    "status": 422,
    "detail": "L'extension de la photo n'est pas autorisée (fichier : notes.txt, autorisées : [.jpeg, .jpg, .png, .webp]).",
    "fileName": "notes.txt",
    "allowedExtensions": [
        ".jpeg",
        ".jpg",
        ".png",
        ".webp"
    ]
}""");
    }

    @Test
    void tooLargeFileIsRefused() {
        UUID productId = products.bonnet();

        Response invalid = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "enorme.png", new byte[MAX_PHOTO_SIZE_BYTES + 1], "image/png")
                .post("/products/{id}/photos", productId)
                .then().statusCode(422)
                .body("code", equalTo("PHOTO_MAX_SIZE_EXCEEDED"))
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "PHOTO_MAX_SIZE_EXCEEDED",
    "title": "Image trop volumineuse",
    "status": 422,
    "detail": "La taille de l'image dépasse le maximum autorisé (8388608 octets, fichier : enorme.png).",
    "fileName": "enorme.png",
    "sizeBytes": 8388609,
    "maxSizeBytes": 8388608
}""");
    }

    @Test
    void oneInvalidPhotoRejectsTheWholeBatch() {
        UUID productId = products.bonnet();

        api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "face.png", Images.red(), "image/png")
                .multiPart("photos", "notes.txt", "pas une image".getBytes(), "text/plain")
                .post("/products/{id}/photos", productId)
                .then().statusCode(422);

        Response product = api.anonymous().get("/products/{id}", productId)
                .then().statusCode(200)
                .extract().response();

        expectSelfie(snapshots.of(product)).toBe("""
{
    "productId": "<uuid-1>",
    "productCategory": "CLOTHING",
    "name": "Bonnet en laine",
    "price": 24.9,
    "colors": [
        "BLEU",
        "ROUGE"
    ],
    "photosUri": [
       \s
    ],
    "numberOfOrders": 0
}""");
    }

    @Test
    void missingPhotosPartIsRefused() {
        UUID productId = products.bonnet();

        Response invalid = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("other", "x")
                .post("/products/{id}/photos", productId)
                .then().statusCode(400)
                .extract().response();

        expectSelfie(snapshots.of(invalid)).toBe("""
{
    "code": "VALIDATION_FAILED",
    "title": "Validation échouée",
    "status": 400,
    "detail": "Un ou plusieurs champs sont invalides.",
    "errors": [
        {
            "field": "photos",
            "message": "Partie obligatoire manquante."
        }
    ]
}""");
    }

    @Test
    void invalidProductIdIsRefused() {
        Response bad = api.admin()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "bonnet.png", Images.red(), "image/png")
                .post("/products/{id}/photos", "not-a-uuid")
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
    void customerCannotAddPhotos() {
        UUID productId = products.bonnet();

        api.customer()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "bonnet.png", Images.red(), "image/png")
                .post("/products/{id}/photos", productId)
                .then().statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    void anonymousCannotAddPhotos() {
        api.anonymous()
                .contentType(ContentType.MULTIPART)
                .multiPart("photos", "bonnet.png", Images.red(), "image/png")
                .post("/products/{id}/photos", UUID.randomUUID())
                .then().statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }
}