package ld.e2e.product.photo;

import io.restassured.response.Response;
import ld.e2e.E2EBase;
import ld.e2e.support.Images;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.diffplug.selfie.Selfie.expectSelfie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

class DownloadProductPhotoE2ETest extends E2EBase {

    @Test
    void anyoneDownloadsTheExactFileThatWasUploaded() {
        UUID productId = products.bonnet();
        byte[] uploaded = Images.red();
        String fileName = photos.add(productId, "bonnet.png", uploaded);

        byte[] downloaded = api.anonymous()
                .get("/products/{id}/photos/{fileName}", productId, fileName)
                .then().statusCode(200)
                .header("Cache-Control", containsString("max-age=604800"))
                .extract().asByteArray();

        assertThat(downloaded).isEqualTo(uploaded);
    }

    @Test
    void eachPhotoIsServedWithItsOwnContent() {
        UUID productId = products.bonnet();
        String redName = photos.add(productId, "face.png", Images.red());
        String blueName = photos.add(productId, "dos.png", Images.blue());

        byte[] blue = api.anonymous()
                .get("/products/{id}/photos/{fileName}", productId, blueName)
                .then().statusCode(200)
                .extract().asByteArray();
        byte[] red = api.anonymous()
                .get("/products/{id}/photos/{fileName}", productId, redName)
                .then().statusCode(200)
                .extract().asByteArray();

        assertThat(blue).isEqualTo(Images.blue());
        assertThat(red).isEqualTo(Images.red());
    }

    @Test
    void unknownFileIsNotFound() {
        UUID productId = products.bonnet();

        Response notFound = api.anonymous()
                .get("/products/{id}/photos/{fileName}", productId, "inconnue.png")
                .then().statusCode(404)
                .body("code", equalTo("PHOTO_NOT_FOUND"))
                .extract().response();

        expectSelfie(snapshots.of(notFound)).toBe("""
{
    "code": "PHOTO_NOT_FOUND",
    "title": "Photo introuvable",
    "status": 404,
    "detail": "La photo demandée est introuvable",
    "fileName": "inconnue.png",
    "productId": "<uuid-1>"
}""");
    }

    @Test
    void aPhotoIsNotServedUnderAnotherProduct() {
        UUID productId = products.named("Bonnet");
        UUID otherProductId = products.named("Echarpe");
        String fileName = photos.add(productId, "bonnet.png", Images.red());

        api.anonymous()
                .get("/products/{id}/photos/{fileName}", otherProductId, fileName)
                .then().statusCode(404)
                .body("code", equalTo("PHOTO_NOT_FOUND"));
    }

    @Test
    void cannotEscapeTheStorageDirectory() {
        UUID productId = products.bonnet();

        api.anonymous()
                .get("/products/{id}/photos/{fileName}", productId, "..%2F..%2Fetc%2Fpasswd")
                .then().statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)));
    }

    @Test
    void invalidProductIdIsRefused() {
        Response bad = api.anonymous()
                .get("/products/{id}/photos/{fileName}", "not-a-uuid", "bonnet.png")
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
}